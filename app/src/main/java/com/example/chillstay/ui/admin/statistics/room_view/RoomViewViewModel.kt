package com.example.chillstay.ui.admin.statistics.room_view

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.usecase.booking.GetBookingsByRoomIdUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingStatisticsByDateRangeUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.room.GetRoomByIdUseCase
import com.example.chillstay.domain.usecase.user.GetUserByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class RoomViewViewModel(
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getBookingStatisticsByDateRangeUseCase: GetBookingStatisticsByDateRangeUseCase,
    private val getBookingsByRoomIdUseCase: GetBookingsByRoomIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase
) : BaseViewModel<RoomViewUiState, RoomViewIntent, RoomViewEffect>(
    RoomViewUiState()
) {

    companion object {
        private const val TAG = "RoomViewVM"
    }

    val uiState = state

    // Cache for user names to avoid repeated queries
    private val userNameCache = mutableMapOf<String, String>()

    override fun onEvent(event: RoomViewIntent) {
        when (event) {
            is RoomViewIntent.LoadRoomStatistics -> {
                _state.value = _state.value.copy(roomId = event.roomId)
                loadRoomData(event.roomId)
                loadStatistics()
            }
            is RoomViewIntent.ApplyFilters -> {
                loadStatistics()
            }
            is RoomViewIntent.DateFromChanged -> {
                _state.value = _state.value.copy(
                    dateFrom = event.dateMillis,
                    showDateFromPicker = false
                )
            }
            is RoomViewIntent.DateToChanged -> {
                _state.value = _state.value.copy(
                    dateTo = event.dateMillis,
                    showDateToPicker = false
                )
            }
            is RoomViewIntent.ToggleDateFromPicker -> {
                _state.value = _state.value.copy(
                    showDateFromPicker = !_state.value.showDateFromPicker,
                    showDateToPicker = false
                )
            }
            is RoomViewIntent.ToggleDateToPicker -> {
                _state.value = _state.value.copy(
                    showDateToPicker = !_state.value.showDateToPicker,
                    showDateFromPicker = false
                )
            }
            is RoomViewIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.copy(currentPage = event.page)
                }
            }
            is RoomViewIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.copy(currentPage = currentPage + 1)
                }
            }
            is RoomViewIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.copy(currentPage = currentPage - 1)
                }
            }
            is RoomViewIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { RoomViewEffect.NavigateBack }
                }
            }
        }
    }

    private fun loadRoomData(roomId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🏛️ Loading room data for ID: $roomId")

                val roomResult = getRoomByIdUseCase(roomId).first()

                when (roomResult) {
                    is Result.Success -> {
                        val room = roomResult.data
                        Log.d(TAG, "✅ Room loaded: ${room.name}")

                        // Load hotel name
                        val hotelResult = getHotelByIdUseCase(room.hotelId).first()
                        val hotelName = if (hotelResult is Result.Success) {
                            hotelResult.data.name
                        } else ""

                        _state.value = _state.value.copy(
                            room = room,
                            hotelName = hotelName
                        )
                    }
                    is Result.Error -> {
                        Log.e(TAG, "❌ Error loading room: ${roomResult.throwable.message}")
                        sendEffect {
                            RoomViewEffect.ShowError(
                                roomResult.throwable.message ?: "Failed to load room"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading room: ${e.message}", e)
                sendEffect {
                    RoomViewEffect.ShowError(e.message ?: "Failed to load room")
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val roomId = _state.value.roomId
            val room = _state.value.room

            if (roomId.isEmpty() || room == null) {
                Log.e(TAG, "❌ Room ID or room is empty")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "📊 Loading statistics for room: ${room.name}")

                // ✅ Load bookings by roomId with date filter
                val bookingsResult = getBookingsByRoomIdUseCase(
                    roomId = roomId,
                    dateFrom = _state.value.dateFrom,
                    dateTo = _state.value.dateTo
                ).first()

                when (bookingsResult) {
                    is Result.Success -> {
                        val bookings = bookingsResult.data
                        Log.d(TAG, "✅ Loaded ${bookings.size} bookings for room")

                        // Get hotel for country/city filter (for chart data)
                        val hotelResult = getHotelByIdUseCase(room.hotelId).first()
                        val hotel = if (hotelResult is Result.Success) {
                            hotelResult.data
                        } else null

                        // Load statistics for chart
                        val statsResult = getBookingStatisticsByDateRangeUseCase(
                            country = hotel?.country,
                            city = hotel?.city,
                            dateFrom = _state.value.dateFrom,
                            dateTo = _state.value.dateTo
                        ).first()

                        if (statsResult is Result.Success) {
                            val stats = statsResult.data
                            processAndUpdateState(
                                bookings = bookings,
                                periodRevenue = stats.periodRevenue,
                                periodLabels = stats.periodLabels
                            )
                        } else {
                            // If stats fail, still show bookings
                            processAndUpdateState(
                                bookings = bookings,
                                periodRevenue = emptyMap(),
                                periodLabels = emptyList()
                            )
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "❌ Error loading bookings: ${bookingsResult.throwable.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = bookingsResult.throwable.message ?: "Failed to load bookings"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading statistics: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }

    private suspend fun processAndUpdateState(
        bookings: List<Booking>,
        periodRevenue: Map<String, Double>,
        periodLabels: List<String>
    ) {
        Log.d(TAG, "🔄 Processing room statistics...")

        // ✅ Calculate real statistics from bookings
        val totalRevenue = bookings
            .filter { it.status != BookingStatus.CANCELLED && it.status != BookingStatus.REFUNDED }
            .sumOf { it.totalPrice }

        val totalBookings = bookings.size

        val cancelledBookings = bookings.count {
            it.status == BookingStatus.CANCELLED || it.status == BookingStatus.REFUNDED
        }
        val cancellationRate = if (totalBookings > 0) {
            (cancelledBookings.toDouble() / totalBookings) * 100
        } else 0.0

        // ✅ Load user names for all bookings in parallel
        val userNamesMap = loadUserNamesForBookings(bookings)

        // ✅ Convert bookings to BookingInfo for display
        val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val recentBookings = bookings.map { booking ->
            BookingInfo(
                bookingId = booking.id,
                guestName = userNamesMap[booking.userId] ?: formatGuestNameFallback(booking),
                nights = calculateNights(booking.dateFrom, booking.dateTo),
                amount = booking.totalPrice,
                checkInDate = formatDate(booking.dateFrom, outputDateFormat),
                status = formatStatus(booking.status)
            )
        }

        // Create chart data
        val chartData = periodLabels.map { label ->
            label to (periodRevenue[label] ?: 0.0)
        }

        val maxRevenue = periodRevenue.values.maxOrNull() ?: 0.0

        withContext(Dispatchers.Main) {
            _state.value = _state.value.copy(
                isLoading = false,
                totalRevenue = totalRevenue,
                totalBookings = totalBookings,
                cancellationRate = cancellationRate,
                chartData = chartData,
                maxRevenueValue = maxRevenue,
                recentBookings = recentBookings,
                currentPage = 1,
                error = null
            )
            Log.d(TAG, "✅ State updated successfully with ${recentBookings.size} bookings")
        }
    }

    /**
     * Load user names for all bookings in parallel
     * Uses cache to avoid repeated queries
     */
    private suspend fun loadUserNamesForBookings(bookings: List<Booking>): Map<String, String> =
        withContext(Dispatchers.IO) {
            val userIds = bookings.map { it.userId }.distinct()
            Log.d(TAG, "👥 Loading names for ${userIds.size} unique users...")

            // Load users in parallel
            val userNameResults = userIds.map { userId ->
                async {
                    // Check cache first
                    if (userNameCache.containsKey(userId)) {
                        userId to userNameCache[userId]!!
                    } else {
                        // Load from repository
                        try {
                            val userResult = getUserByIdUseCase(userId).first()
                            val userName = if (userResult is Result.Success) {
                                userResult.data.fullName.ifBlank {
                                    "Guest ${userId.takeLast(4)}"
                                }
                            } else {
                                "Guest ${userId.takeLast(4)}"
                            }

                            // Cache the result
                            userNameCache[userId] = userName
                            userId to userName
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to load user $userId: ${e.message}")
                            val fallback = "Guest ${userId.takeLast(4)}"
                            userNameCache[userId] = fallback
                            userId to fallback
                        }
                    }
                }
            }.awaitAll()

            userNameResults.toMap()
        }

    /**
     * Fallback method to format guest name when user loading fails
     */
    private fun formatGuestNameFallback(booking: Booking): String {
        return "Guest ${booking.userId.takeLast(4)}"
    }

    /**
     * Format booking status for display
     */
    private fun formatStatus(status: BookingStatus): String {
        return when (status) {
            BookingStatus.PENDING -> "Pending"
            BookingStatus.CONFIRMED -> "Confirmed"
            BookingStatus.CHECKED_IN -> "Checked In"
            BookingStatus.CHECKED_OUT -> "Checked Out"
            BookingStatus.COMPLETED -> "Completed"
            BookingStatus.CANCELLED -> "Cancelled"
            BookingStatus.REFUNDED -> "Refunded"
        }
    }

    /**
     * Format date string for display
     * Tries multiple formats and returns formatted string or original if parsing fails
     */
    private fun formatDate(dateString: String, outputFormat: SimpleDateFormat): String {
        try {
            // Try ISO date format first (yyyy-MM-dd)
            val isoFormat = DateTimeFormatter.ISO_LOCAL_DATE
            val date = LocalDate.parse(dateString, isoFormat)
            val calendar = Calendar.getInstance().apply {
                set(date.year, date.monthValue - 1, date.dayOfMonth)
            }
            return outputFormat.format(calendar.time)
        } catch (_: Exception) {
            // If parsing fails, try other common formats
            val inputFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
                SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            )

            for (format in inputFormats) {
                try {
                    val parsedDate = format.parse(dateString)
                    if (parsedDate != null) {
                        return outputFormat.format(parsedDate)
                    }
                } catch (_: Exception) {
                    continue
                }
            }

            // If all parsing fails, return original string
            Log.w(TAG, "Could not parse date: $dateString")
            return dateString
        }
    }

    /**
     * Calculate number of nights between check-in and check-out dates
     */
    private fun calculateNights(dateFrom: String, dateTo: String): Int {
        try {
            val formatter = DateTimeFormatter.ISO_LOCAL_DATE
            val fromDate = LocalDate.parse(dateFrom, formatter)
            val toDate = LocalDate.parse(dateTo, formatter)
            val nights = ChronoUnit.DAYS.between(fromDate, toDate).toInt()
            return nights.coerceAtLeast(1) // Minimum 1 night
        } catch (e: Exception) {
            Log.w(TAG, "Could not calculate nights from $dateFrom to $dateTo: ${e.message}")
            return 1 // Default to 1 night if calculation fails
        }
    }
}