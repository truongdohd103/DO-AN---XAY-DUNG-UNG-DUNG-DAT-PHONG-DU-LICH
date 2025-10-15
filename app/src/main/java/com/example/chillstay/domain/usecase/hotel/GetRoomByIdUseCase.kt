package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.core.common.Result


class GetRoomByIdUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(roomId: String): Result<Room> {
        return try {
            val room = hotelRepository.getRoomById(roomId)
            if (room != null) {
                Result.success(room)
            } else {
                Result.failure(Exception("Room not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}



