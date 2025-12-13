package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result

class GetBookingByIdUseCase constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Booking?> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
            Result.success(booking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

