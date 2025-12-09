package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetHotelRoomsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(
        hotelId: String,
        checkIn: String? = null,
        checkOut: String? = null,
        guests: Int? = null
    ): Flow<Result<List<Room>>> = flow {
        val rooms = hotelRepository.getHotelRooms(
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
