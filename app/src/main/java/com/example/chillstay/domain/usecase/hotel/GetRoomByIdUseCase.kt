package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetRoomByIdUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(roomId: String): Flow<Result<Room>> = flow {
        val room = hotelRepository.getRoomById(roomId) ?: throw IllegalStateException("Room not found")
        emit(Result.success(room))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
