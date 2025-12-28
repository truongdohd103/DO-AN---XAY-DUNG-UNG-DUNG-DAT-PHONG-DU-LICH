package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UpdateRoomUseCase(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(room: Room): Flow<Result<Boolean>> = flow {
        val success = hotelRepository.updateRoom(room)
        if (success) {
            emit(Result.success(true))
        } else {
            emit(Result.failure(Exception("Failed to update room")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}
