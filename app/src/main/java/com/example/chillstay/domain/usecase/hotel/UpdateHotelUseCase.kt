package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UpdateHotelUseCase(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotel: Hotel): Flow<Result<Unit>> = flow {
        hotelRepository.updateHotel(hotel)
        emit(Result.success(Unit))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}