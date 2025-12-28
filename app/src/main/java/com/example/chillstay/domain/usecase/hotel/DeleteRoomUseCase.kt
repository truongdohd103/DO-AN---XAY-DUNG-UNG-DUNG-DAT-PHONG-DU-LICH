package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.HotelRepository

class DeleteRoomUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(roomId: String): Result<Boolean> {
        return try {
            val success = hotelRepository.deleteRoom(roomId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete room"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
