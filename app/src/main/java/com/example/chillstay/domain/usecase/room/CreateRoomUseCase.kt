package com.example.chillstay.domain.usecase.room

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CreateRoomUseCase(
    private val roomRepository: RoomRepository) {
    operator fun invoke(room: Room): Flow<Result<String>> = flow {
        val newId = roomRepository.createRoom(room)
        emit(Result.success(newId))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}