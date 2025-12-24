package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.RoomRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(booking: Booking): Result<Booking> {
        return try {
            // Validate booking parameters
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateFrom = LocalDate.parse(booking.dateFrom, formatter)
            val dateTo = LocalDate.parse(booking.dateTo, formatter)
            
            if (dateFrom.isAfter(dateTo)) {
                return Result.failure(Exception("Check-in date cannot be after check-out date"))
            }
            
            if (dateFrom.isBefore(LocalDate.now())) {
                return Result.failure(Exception("Check-in date cannot be in the past"))
            }
            
            if (booking.guests <= 0) {
                return Result.failure(Exception("Number of guests must be greater than 0"))
            }
            
            if (booking.price <= 0) {
                return Result.failure(Exception("Price must be greater than 0"))
            }
            
            // Check and reserve room availability atomically
            val reserved = roomRepository.reserveRoomUnits(booking.roomId, booking.rooms)
            if (!reserved) {
                return Result.failure(Exception("Room not available for the requested quantity"))
            }

            try {
                val createdBooking = bookingRepository.createBooking(
                    booking.copy(inventoryReserved = true)
                )
                Result.success(createdBooking)
            } catch (e: Exception) {
                // Rollback reservation if booking creation fails
                roomRepository.releaseRoomUnits(booking.roomId, booking.rooms)
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
