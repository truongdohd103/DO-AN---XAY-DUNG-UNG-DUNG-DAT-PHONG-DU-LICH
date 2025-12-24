package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.RoomRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(bookingId: String): Result<Boolean> {
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
            val success = bookingRepository.cancelBooking(bookingId)
            if (success) {
                if (booking != null && booking.inventoryReserved) {
                    roomRepository.releaseRoomUnits(booking.roomId, booking.rooms)
                }
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to cancel booking"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
