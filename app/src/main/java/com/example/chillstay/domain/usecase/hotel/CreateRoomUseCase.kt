package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CreateRoomUseCase(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(room: Room): Flow<Result<String>> = flow {
        val id = hotelRepository.createRoom(room)
        if (id.isNotEmpty()) {
            emit(Result.success(id))
        } else {
            emit(Result.failure(Exception("Failed to create room")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}
