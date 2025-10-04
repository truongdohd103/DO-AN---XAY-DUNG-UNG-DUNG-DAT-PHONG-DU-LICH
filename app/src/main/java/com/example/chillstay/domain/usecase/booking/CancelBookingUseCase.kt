package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result


class CancelBookingUseCase constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Boolean> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
            if (booking == null) {
                return Result.failure(Exception("Booking not found"))
            }
            
            // Check if booking can be cancelled
            if (booking.status == "CANCELLED") {
                return Result.failure(Exception("Booking is already cancelled"))
            }
            
            if (booking.status == "COMPLETED") {
                return Result.failure(Exception("Cannot cancel completed booking"))
            }
            
            val success = bookingRepository.cancelBooking(bookingId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to cancel booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
