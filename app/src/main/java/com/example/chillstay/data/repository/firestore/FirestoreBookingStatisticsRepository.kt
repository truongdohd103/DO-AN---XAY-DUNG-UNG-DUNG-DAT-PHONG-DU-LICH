package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingStatistics
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelBookingStats
import com.example.chillstay.domain.repository.BookingStatisticsRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBookingStatisticsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingStatisticsRepository {

    // Cache để tránh load lại hotels
    private val hotelCache = ConcurrentHashMap<String, Hotel>()

    companion object {
        private const val TAG = "BookingStatsRepo"
        private const val WHERE_IN_MAX = 10

        private val monthFormatCache = ThreadLocal<SimpleDateFormat>()
        private val yearFormatCache = ThreadLocal<SimpleDateFormat>()

        private fun getMonthFormat(): SimpleDateFormat {
            return monthFormatCache.get() ?: SimpleDateFormat("MMM yyyy", Locale.getDefault()).also {
                monthFormatCache.set(it)
            }
        }

        private fun getYearFormat(): SimpleDateFormat {
            return yearFormatCache.get() ?: SimpleDateFormat("yyyy", Locale.getDefault()).also {
                yearFormatCache.set(it)
            }
        }
    }

    override suspend fun getBookingStatistics(
        country: String?,
        city: String?,
        year: Int?,
        quarter: Int?,
        month: Int?
    ): BookingStatistics = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Loading statistics: country=$country, city=$city, year=$year, quarter=$quarter, month=$month")
            val startTime = System.currentTimeMillis()

            // BƯỚC 1: Load tất cả bookings
            val bookings = withContext(Dispatchers.IO) { loadAllBookings() }
            Log.d(TAG, "Loaded ${bookings.size} bookings")

            if (bookings.isEmpty()) {
                return@withContext createEmptyStatistics(year, quarter, month)
            }

            // BƯỚC 2: Lấy unique hotelIds từ bookings
            val hotelIds = bookings.map { it.hotelId }.distinct()
            Log.d(TAG, "Need to load ${hotelIds.size} unique hotels")

            // BƯỚC 3: Load CHỈ những hotels cần thiết (song song theo chunks)
            val hotels = withContext(Dispatchers.IO) {
                loadHotelsOnDemand(hotelIds)
            }
            Log.d(TAG, "Loaded ${hotels.size} hotels")

            // BƯỚC 4: Filter bookings
            val filteredBookings = filterBookingsOptimized(
                bookings, hotels, country, city, year, quarter, month
            )
            Log.d(TAG, "Filtered to ${filteredBookings.size} bookings")

            if (filteredBookings.isEmpty()) {
                return@withContext createEmptyStatistics(year, quarter, month)
            }

            // BƯỚC 5: Calculate metrics
            val metrics = calculateMetricsOptimized(filteredBookings, hotels)

            // BƯỚC 6: Generate period revenue
            val (revenueMap, labels) = generatePeriodRevenue(
                filteredBookings, year, quarter, month
            )

            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Completed in ${endTime - startTime}ms")

            BookingStatistics(
                totalRevenue = metrics.totalRevenue,
                totalBookings = metrics.totalBookings,
                cancellationRate = metrics.cancellationRate,
                bookingsByHotel = metrics.hotelStats,
                periodRevenue = revenueMap,
                periodLabels = labels
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics", e)
            createEmptyStatistics(year, quarter, month)
        }
    }

    private suspend fun loadAllBookings(): List<Booking> {
        return try {
            val snapshot = firestore.collection("bookings").get().await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing booking ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading bookings: ${e.message}")
            emptyList()
        }
    }

    /**
     * Load CHỈ những hotels cần thiết theo chunks với WHERE_IN
     * Nhanh hơn nhiều so với load ALL hotels
     */
    private suspend fun loadHotelsOnDemand(hotelIds: List<String>): Map<String, Hotel> {
        if (hotelIds.isEmpty()) return emptyMap()

        // Kiểm tra cache trước
        val idsToFetch = hotelIds.filterNot { hotelCache.containsKey(it) }

        if (idsToFetch.isEmpty()) {
            // Tất cả đã có trong cache
            return hotelCache.filterKeys { it in hotelIds }
        }

        // Chia thành chunks để dùng WHERE_IN (max 10 IDs/query)
        val chunks = idsToFetch.chunked(WHERE_IN_MAX)

        supervisorScope {
            chunks.map { chunk ->
                async {
                    try {
                        val snapshot = firestore.collection("hotels")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        snapshot.documents.forEach { doc ->
                            val hotel = mapHotelDocument(doc)
                            if (hotel != null) {
                                hotelCache[hotel.id] = hotel
                            }
                        }

                        // Đánh dấu hotels không tồn tại
                        val returnedIds = snapshot.documents.map { it.id }.toSet()
                        chunk.forEach { id ->
                            if (!returnedIds.contains(id) && !hotelCache.containsKey(id)) {
                                hotelCache[id] = Hotel(
                                    id = id,
                                    name = "Unknown Hotel",
                                    city = "",
                                    country = "",
                                    minPrice = null
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading hotel chunk: ${e.message}")
                        // Fallback: tạo placeholder hotels
                        chunk.forEach { id ->
                            if (!hotelCache.containsKey(id)) {
                                hotelCache[id] = Hotel(
                                    id = id,
                                    name = "Unknown Hotel",
                                    city = "",
                                    country = "",
                                    minPrice = null
                                )
                            }
                        }
                    }
                }
            }.forEach { it.await() }
        }

        return hotelCache.filterKeys { it in hotelIds }
    }

    private fun mapHotelDocument(doc: com.google.firebase.firestore.DocumentSnapshot): Hotel? {
        val data = doc.data ?: return null
        val name = data["name"] as? String ?: return null

        return Hotel(
            id = doc.id,
            name = name,
            city = data["city"] as? String ?: "",
            country = data["country"] as? String ?: "",
            minPrice = (data["minPrice"] as? Number)?.toDouble()
        )
    }

    private fun filterBookingsOptimized(
        bookings: List<Booking>,
        hotels: Map<String, Hotel>,
        country: String?,
        city: String?,
        year: Int?,
        quarter: Int?,
        month: Int?
    ): List<Booking> {
        if (bookings.isEmpty()) return emptyList()

        val (startTime, endTime) = calculateDateRange(year, quarter, month)

        return bookings.filter { booking ->
            // Date filter
            if (startTime != null && endTime != null) {
                val bookingTime = booking.createdAt.seconds * 1000
                if (bookingTime < startTime || bookingTime > endTime) return@filter false
            }

            // Location filters
            if (!country.isNullOrBlank() || !city.isNullOrBlank()) {
                val hotel = hotels[booking.hotelId] ?: return@filter false

                if (!country.isNullOrBlank() && !hotel.country.equals(country, ignoreCase = true)) {
                    return@filter false
                }

                if (!city.isNullOrBlank() && !hotel.city.equals(city, ignoreCase = true)) {
                    return@filter false
                }
            }

            true
        }
    }

    private data class MetricsResult(
        val totalRevenue: Double,
        val totalBookings: Int,
        val cancellationRate: Double,
        val hotelStats: Map<String, HotelBookingStats>
    )

    private data class MutableHotelMetrics(
        val hotelId: String,
        var bookings: Int = 0,
        var revenue: Double = 0.0,
        var cancelled: Int = 0
    )

    private fun calculateMetricsOptimized(
        bookings: List<Booking>,
        hotels: Map<String, Hotel>
    ): MetricsResult {
        var totalRevenue = 0.0
        var totalCancelled = 0
        val hotelMetrics = ConcurrentHashMap<String, MutableHotelMetrics>()

        bookings.forEach { booking ->
            val isCancelled = booking.status == BookingStatus.CANCELLED

            if (!isCancelled) {
                totalRevenue += booking.totalPrice
            }
            if (isCancelled) {
                totalCancelled++
            }

            val hotelMetric = hotelMetrics.getOrPut(booking.hotelId) {
                MutableHotelMetrics(booking.hotelId)
            }
            hotelMetric.bookings++
            if (!isCancelled) {
                hotelMetric.revenue += booking.totalPrice
            }
            if (isCancelled) {
                hotelMetric.cancelled++
            }
        }

        val hotelStats = hotelMetrics.mapValues { (hotelId, metrics) ->
            val hotel = hotels[hotelId]
            val cancellationRate = if (metrics.bookings > 0) {
                (metrics.cancelled.toDouble() / metrics.bookings) * 100
            } else 0.0

            HotelBookingStats(
                hotelId = hotelId,
                hotelName = hotel?.name ?: "Unknown Hotel",
                bookings = metrics.bookings,
                revenue = metrics.revenue,
                cancellationRate = cancellationRate
            )
        }

        val totalBookings = bookings.size
        val cancellationRate = if (totalBookings > 0) {
            (totalCancelled.toDouble() / totalBookings) * 100
        } else 0.0

        return MetricsResult(
            totalRevenue = totalRevenue,
            totalBookings = totalBookings,
            cancellationRate = cancellationRate,
            hotelStats = hotelStats
        )
    }

    private fun generatePeriodRevenue(
        bookings: List<Booking>,
        year: Int?,
        quarter: Int?,
        month: Int?
    ): Pair<Map<String, Double>, List<String>> {
        return when {
            year == null -> calculateYearlyRevenue(bookings)
            quarter != null -> calculateQuarterMonthsRevenue(bookings, year, quarter)
            month != null -> calculateMonthWeeksRevenue(bookings, year, month)
            else -> calculateYearMonthsRevenue(bookings, year)
        }
    }

    private fun calculateYearlyRevenue(bookings: List<Booking>): Pair<Map<String, Double>, List<String>> {
        val yearFormat = getYearFormat()
        val map = mutableMapOf<String, Double>()
        val labels = listOf("2024", "2025", "2026")

        labels.forEach { map[it] = 0.0 }

        bookings.filter { it.status != BookingStatus.CANCELLED }.forEach { booking ->
            try {
                val bookingDate = Date(booking.createdAt.seconds * 1000)
                val yearLabel = yearFormat.format(bookingDate)
                if (yearLabel in map) {
                    map[yearLabel] = (map[yearLabel] ?: 0.0) + booking.totalPrice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error processing booking date: ${e.message}")
            }
        }

        return map to labels
    }

    private fun calculateYearMonthsRevenue(
        bookings: List<Booking>,
        year: Int
    ): Pair<Map<String, Double>, List<String>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, 0, 1, 0, 0, 0)

        val monthFormat = getMonthFormat()
        val map = mutableMapOf<String, Double>()
        val labels = mutableListOf<String>()

        for (m in 0..11) {
            calendar.set(Calendar.MONTH, m)
            val label = monthFormat.format(calendar.time)
            map[label] = 0.0
            labels.add(label)
        }

        bookings.filter { it.status != BookingStatus.CANCELLED }.forEach { booking ->
            try {
                val bookingDate = Date(booking.createdAt.seconds * 1000)
                val label = monthFormat.format(bookingDate)
                if (label in map) {
                    map[label] = (map[label] ?: 0.0) + booking.totalPrice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error processing booking date: ${e.message}")
            }
        }

        return map to labels
    }

    private fun calculateQuarterMonthsRevenue(
        bookings: List<Booking>,
        year: Int,
        quarter: Int
    ): Pair<Map<String, Double>, List<String>> {
        val startMonth = (quarter - 1) * 3
        val calendar = Calendar.getInstance()
        calendar.set(year, startMonth, 1, 0, 0, 0)

        val monthFormat = getMonthFormat()
        val map = mutableMapOf<String, Double>()
        val labels = mutableListOf<String>()

        for (i in 0..2) {
            calendar.set(Calendar.MONTH, startMonth + i)
            val label = monthFormat.format(calendar.time)
            map[label] = 0.0
            labels.add(label)
        }

        bookings.filter { it.status != BookingStatus.CANCELLED }.forEach { booking ->
            try {
                val bookingDate = Date(booking.createdAt.seconds * 1000)
                val label = monthFormat.format(bookingDate)
                if (label in map) {
                    map[label] = (map[label] ?: 0.0) + booking.totalPrice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error processing booking date: ${e.message}")
            }
        }

        return map to labels
    }

    private fun calculateMonthWeeksRevenue(
        bookings: List<Booking>,
        year: Int,
        month: Int
    ): Pair<Map<String, Double>, List<String>> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)

        val map = mutableMapOf<String, Double>()
        val labels = mutableListOf<String>()

        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val numWeeks = (maxDay + 6) / 7

        for (week in 1..numWeeks) {
            val label = "Week $week"
            map[label] = 0.0
            labels.add(label)
        }

        bookings.filter { it.status != BookingStatus.CANCELLED }.forEach { booking ->
            try {
                val bookingDate = Date(booking.createdAt.seconds * 1000)
                val bookingCal = Calendar.getInstance()
                bookingCal.time = bookingDate

                if (bookingCal.get(Calendar.YEAR) == year &&
                    bookingCal.get(Calendar.MONTH) == month - 1) {
                    val dayOfMonth = bookingCal.get(Calendar.DAY_OF_MONTH)
                    val weekNum = ((dayOfMonth - 1) / 7) + 1
                    val label = "Week $weekNum"

                    if (label in map) {
                        map[label] = (map[label] ?: 0.0) + booking.totalPrice
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error processing booking date: ${e.message}")
            }
        }

        return map to labels
    }

    private fun calculateDateRange(
        year: Int?,
        quarter: Int?,
        month: Int?
    ): Pair<Long?, Long?> {
        if (year == null) return null to null

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when {
            month != null -> {
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startTime = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                val endTime = calendar.timeInMillis

                return startTime to endTime
            }
            quarter != null -> {
                val startMonth = (quarter - 1) * 3
                calendar.set(Calendar.MONTH, startMonth)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startTime = calendar.timeInMillis

                calendar.add(Calendar.MONTH, 3)
                calendar.add(Calendar.MILLISECOND, -1)
                val endTime = calendar.timeInMillis

                return startTime to endTime
            }
            else -> {
                calendar.set(Calendar.MONTH, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val startTime = calendar.timeInMillis

                calendar.set(Calendar.MONTH, 11)
                calendar.set(Calendar.DAY_OF_MONTH, 31)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val endTime = calendar.timeInMillis

                return startTime to endTime
            }
        }
    }

    private fun createEmptyStatistics(
        year: Int?,
        quarter: Int?,
        month: Int?
    ): BookingStatistics {
        val (emptyMap, labels) = when {
            year == null -> {
                val labels = listOf("2024", "2025", "2026")
                labels.associateWith { 0.0 } to labels
            }
            quarter != null -> {
                val labels = mutableListOf<String>()
                val startMonth = (quarter - 1) * 3
                val calendar = Calendar.getInstance()
                calendar.set(year, startMonth, 1)
                val monthFormat = getMonthFormat()
                for (i in 0..2) {
                    calendar.set(Calendar.MONTH, startMonth + i)
                    labels.add(monthFormat.format(calendar.time))
                }
                labels.associateWith { 0.0 } to labels
            }
            month != null -> {
                val calendar = Calendar.getInstance()
                calendar.set(year, month - 1, 1)
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val numWeeks = (maxDay + 6) / 7
                val labels = (1..numWeeks).map { "Week $it" }
                labels.associateWith { 0.0 } to labels
            }
            else -> {
                val labels = mutableListOf<String>()
                val calendar = Calendar.getInstance()
                calendar.set(year, 0, 1)
                val monthFormat = getMonthFormat()
                for (m in 0..11) {
                    calendar.set(Calendar.MONTH, m)
                    labels.add(monthFormat.format(calendar.time))
                }
                labels.associateWith { 0.0 } to labels
            }
        }

        return BookingStatistics(
            totalRevenue = 0.0,
            totalBookings = 0,
            cancellationRate = 0.0,
            bookingsByHotel = emptyMap(),
            periodRevenue = emptyMap,
            periodLabels = labels
        )
    }

    // FirestoreBookingStatisticsRepository.kt - THÊM method implementation
    override suspend fun getBookingStatisticsByDateRange(
        country: String?,
        city: String?,
        dateFrom: Long?,
        dateTo: Long?
    ): BookingStatistics = withContext(Dispatchers.Default) {
        try {
            Log.d(TAG, "Loading statistics by date range: country=$country, city=$city, dateFrom=$dateFrom, dateTo=$dateTo")
            val startTime = System.currentTimeMillis()

            // BƯỚC 1: Load tất cả bookings
            val bookings = withContext(Dispatchers.IO) { loadAllBookings() }
            Log.d(TAG, "Loaded ${bookings.size} bookings")

            if (bookings.isEmpty()) {
                return@withContext createEmptyStatisticsByDateRange(dateFrom, dateTo)
            }

            // BƯỚC 2: Lấy unique hotelIds
            val hotelIds = bookings.map { it.hotelId }.distinct()
            Log.d(TAG, "Need to load ${hotelIds.size} unique hotels")

            // BƯỚC 3: Load hotels
            val hotels = withContext(Dispatchers.IO) {
                loadHotelsOnDemand(hotelIds)
            }
            Log.d(TAG, "Loaded ${hotels.size} hotels")

            // BƯỚC 4: Filter bookings by date range and location
            val filteredBookings = filterBookingsByDateRange(
                bookings, hotels, country, city, dateFrom, dateTo
            )
            Log.d(TAG, "Filtered to ${filteredBookings.size} bookings")

            if (filteredBookings.isEmpty()) {
                return@withContext createEmptyStatisticsByDateRange(dateFrom, dateTo)
            }

            // BƯỚC 5: Calculate metrics
            val metrics = calculateMetricsOptimized(filteredBookings, hotels)

            // BƯỚC 6: Generate period revenue (daily breakdown)
            val (revenueMap, labels) = generateDailyRevenue(
                filteredBookings, dateFrom, dateTo
            )

            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Completed in ${endTime - startTime}ms")

            BookingStatistics(
                totalRevenue = metrics.totalRevenue,
                totalBookings = metrics.totalBookings,
                cancellationRate = metrics.cancellationRate,
                bookingsByHotel = metrics.hotelStats,
                periodRevenue = revenueMap,
                periodLabels = labels
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics by date range", e)
            createEmptyStatisticsByDateRange(dateFrom, dateTo)
        }
    }

    private fun filterBookingsByDateRange(
        bookings: List<Booking>,
        hotels: Map<String, Hotel>,
        country: String?,
        city: String?,
        dateFrom: Long?,
        dateTo: Long?
    ): List<Booking> {
        if (bookings.isEmpty()) return emptyList()

        return bookings.filter { booking ->
            val bookingTime = booking.createdAt.seconds * 1000

            // ✅ Date range filter
            if (dateFrom != null && bookingTime < dateFrom) {
                return@filter false
            }
            if (dateTo != null && bookingTime > dateTo) {
                return@filter false
            }

            // Location filters
            if (!country.isNullOrBlank() || !city.isNullOrBlank()) {
                val hotel = hotels[booking.hotelId] ?: return@filter false

                if (!country.isNullOrBlank() && !hotel.country.equals(country, ignoreCase = true)) {
                    return@filter false
                }

                if (!city.isNullOrBlank() && !hotel.city.equals(city, ignoreCase = true)) {
                    return@filter false
                }
            }

            true
        }
    }

    /**
     * Generate daily revenue breakdown for date range
     */
    private fun generateDailyRevenue(
        bookings: List<Booking>,
        dateFrom: Long?,
        dateTo: Long?
    ): Pair<Map<String, Double>, List<String>> {
        if (dateFrom == null || dateTo == null) {
            // No date range specified, return empty
            return emptyMap<String, Double>() to emptyList()
        }

        val dayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val map = mutableMapOf<String, Double>()
        val labels = mutableListOf<String>()

        // Generate all days in range
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dateFrom
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = dateTo
        endCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endCalendar.set(Calendar.MINUTE, 59)
        endCalendar.set(Calendar.SECOND, 59)

        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            val label = dayFormat.format(calendar.time)
            map[label] = 0.0
            labels.add(label)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // ✅ Aggregate bookings by day
        bookings.filter { it.status != BookingStatus.CANCELLED }.forEach { booking ->
            try {
                val bookingDate = Date(booking.createdAt.seconds * 1000)
                val label = dayFormat.format(bookingDate)
                if (label in map) {
                    map[label] = (map[label] ?: 0.0) + booking.totalPrice
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error processing booking date: ${e.message}")
            }
        }

        return map to labels
    }

    private fun createEmptyStatisticsByDateRange(
        dateFrom: Long?,
        dateTo: Long?
    ): BookingStatistics {
        // khai báo rõ ràng để tránh inference / shadowing lỗi
        val periodRevenueMap: Map<String, Double>
        val labels: List<String>

        if (dateFrom != null && dateTo != null) {
            val dayFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val tempLabels = mutableListOf<String>()

            val calendar = Calendar.getInstance().apply { timeInMillis = dateFrom }
            val endCalendar = Calendar.getInstance().apply { timeInMillis = dateTo }

            // dùng calendar.after() để tránh vòng lặp vô hạn nếu timeInMillis bằng
            while (!calendar.after(endCalendar)) {
                tempLabels.add(dayFormat.format(calendar.time))
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            periodRevenueMap = tempLabels.associateWith { 0.0 } // Map<String, Double>
            labels = tempLabels
        } else {
            periodRevenueMap = emptyMap() // emptyMap<String, Double>() nếu muốn rõ kiểu
            labels = emptyList()
        }

        return BookingStatistics(
            totalRevenue = 0.0,
            totalBookings = 0,
            cancellationRate = 0.0,
            bookingsByHotel = emptyMap(),     // <-- CHÚ Ý: đảm bảo kiểu này khớp với BookingStatistics
            periodRevenue = periodRevenueMap,
            periodLabels = labels
        )
    }

}