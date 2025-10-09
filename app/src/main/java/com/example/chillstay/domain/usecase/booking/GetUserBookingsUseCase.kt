package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result


class GetUserBookingsUseCase constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        userId: String,
        status: String? = null
    ): Result<List<Booking>> {
        return try {
            val bookings = bookingRepository.getUserBookings(userId, status)
            Result.success(bookings)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

