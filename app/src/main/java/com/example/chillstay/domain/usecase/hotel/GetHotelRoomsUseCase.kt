package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.core.common.Result


class GetHotelRoomsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(
        hotelId: String,
        checkIn: String? = null,
        checkOut: String? = null,
        guests: Int? = null
    ): Result<List<Room>> {
        return try {
            val rooms = hotelRepository.getHotelRooms(
                hotelId = hotelId,
                checkIn = checkIn,
                checkOut = checkOut,
                guests = guests
            )
            Result.success(rooms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


