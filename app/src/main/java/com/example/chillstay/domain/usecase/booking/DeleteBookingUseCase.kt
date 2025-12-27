package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.RoomRepository

class DeleteBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(booking: Booking): Result<Boolean> {
        return try {
            if (booking.inventoryReserved) {
                val released = roomRepository.releaseRoomUnits(booking.roomId, booking.rooms)
                if (!released) {
                    return Result.failure(Exception("Failed to restore room availability"))
                }
            }
            val deleted = bookingRepository.deleteBooking(booking.id)
            if (deleted) Result.success(true) else Result.failure(Exception("Failed to delete booking"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
