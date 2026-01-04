package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class GetBookingsByRoomIdUseCase(private val bookingRepository: BookingRepository) {

    operator fun invoke(roomId: String, dateFrom: Long? = null, dateTo: Long? = null): Flow<Result<List<Booking>>> = flow {
        try {
            val allBookings = bookingRepository.getAllBookings()

            val bookingWithMillis = allBookings.mapNotNull { booking ->
                val fromMillis = parseDateStringToMillis(booking.dateFrom)
                val toMillis = parseDateStringToMillis(booking.dateTo)
                // Nếu không parse được -> bỏ qua (an toàn), bạn có thể đổi chính sách này nếu muốn
                if (fromMillis == null || toMillis == null) null else Triple(booking, fromMillis, toMillis)
            }

            val filteredBookings = bookingWithMillis
                .filter { (b, fromMillis, toMillis) ->
                    b.roomId == roomId &&
                            (dateFrom == null || fromMillis >= dateFrom) &&
                            (dateTo == null || toMillis <= dateTo)
                }
                .sortedByDescending { it.second }
                .map { it.first }

            emit(Result.success(filteredBookings))
        } catch (t: Throwable) {
            emit(Result.failure(t))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseDateStringToMillis(s: String?): Long? {
        if (s == null) return null
        val trimmed = s.trim()
        // nếu là epoch millis string
        trimmed.toLongOrNull()?.let { return it }

        // danh sách formatter thử theo thứ tự ưu tiên
        val formattersInstant = listOf(
            DateTimeFormatter.ISO_INSTANT,           // 2023-01-01T00:00:00Z
            DateTimeFormatter.ISO_OFFSET_DATE_TIME   // 2023-01-01T07:00:00+07:00
        )

        val formattersLocal = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,  // 2023-01-01T08:00:00
            DateTimeFormatter.ISO_LOCAL_DATE        // 2023-01-01
        )

        // cố gắng parse Instant-like
        for (fmt in formattersInstant) {
            try {
                val instant = Instant.from(fmt.parse(trimmed))
                return instant.toEpochMilli()
            } catch (_: DateTimeParseException) { /* tiếp tục */ }
        }

        // ISO_LOCAL_DATE_TIME -> coi theo system default zone
        try {
            val ldt = LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) { /* tiếp tục */ }

        // ISO_LOCAL_DATE -> start of day system zone
        try {
            val ld = LocalDate.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE)
            return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (_: DateTimeParseException) { /* tiếp tục */ }

        // thử một vài pattern phổ biến (dd/MM/yyyy, dd-MM-yyyy, yyyy/MM/dd)
        val extraPatterns = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy/MM/dd")
        for (p in extraPatterns) {
            try {
                val fmt = DateTimeFormatter.ofPattern(p, Locale.getDefault())
                val ld = LocalDate.parse(trimmed, fmt)
                return ld.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {  }
        }

        // không parse được
        return null
    }
}
