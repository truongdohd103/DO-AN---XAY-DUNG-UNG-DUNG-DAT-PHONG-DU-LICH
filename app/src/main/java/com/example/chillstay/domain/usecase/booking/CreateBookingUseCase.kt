package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result
import java.time.LocalDate
import java.time.Instant
class CreateBookingUseCase(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        userId: String,
        roomId: String,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        guests: Int,
        price: Double,
        appliedVouchers: List<String> = emptyList()
    ): Result<Booking> {
        return try {
            // Validate booking parameters
            if (dateFrom.isAfter(dateTo)) {
                return Result.failure(Exception("Check-in date cannot be after check-out date"))
            }
            
            if (dateFrom.isBefore(LocalDate.now())) {
                return Result.failure(Exception("Check-in date cannot be in the past"))
            }
            
            if (guests <= 0) {
                return Result.failure(Exception("Number of guests must be greater than 0"))
            }
            
            if (price <= 0) {
                return Result.failure(Exception("Price must be greater than 0"))
            }
            
            val booking = Booking(
                id = "", // Will be set by repository
                userId = userId,
                roomId = roomId,
                dateFrom = dateFrom,
                dateTo = dateTo,
                guests = guests,
                price = price,
                status = "PENDING",
                createdAt = Instant.now(),
                appliedVouchers = emptyList() // Will be populated by repository
            )
            
            val createdBooking = bookingRepository.createBooking(booking)
            Result.success(createdBooking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
