package com.example.chillstay.domain.usecase.room

import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UpdateRoomUseCase (
    private val roomRepository: RoomRepository
) {
    operator fun invoke(room: Room): Flow<Result<Unit>> = flow {
        roomRepository.updateRoom(room)
        emit(Result.success(Unit))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}