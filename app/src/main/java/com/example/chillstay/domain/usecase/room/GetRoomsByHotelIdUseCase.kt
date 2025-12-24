package com.example.chillstay.domain.usecase.room

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetRoomsByHotelIdUseCase constructor(
    private val roomRepository: RoomRepository
) {
    operator fun invoke(
        hotelId: String,
        checkIn: String? = null,
        checkOut: String? = null,
        guests: Int? = null
    ): Flow<Result<List<Room>>> = flow {
        val rooms = roomRepository.getRoomsByHotelId(
            hotelId = hotelId,
            checkIn = checkIn,
            checkOut = checkOut,
            guests = guests
        )
        emit(Result.success(rooms))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
