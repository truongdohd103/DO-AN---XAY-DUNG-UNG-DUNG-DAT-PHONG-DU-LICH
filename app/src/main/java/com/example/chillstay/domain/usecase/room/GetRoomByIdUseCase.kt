package com.example.chillstay.domain.usecase.room

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetRoomByIdUseCase constructor(
    private val roomRepository: RoomRepository
) {
    operator fun invoke(roomId: String): Flow<Result<Room>> = flow {
        val room = roomRepository.getRoomById(roomId) ?: throw IllegalStateException("Room not found")
        emit(Result.success(room))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
