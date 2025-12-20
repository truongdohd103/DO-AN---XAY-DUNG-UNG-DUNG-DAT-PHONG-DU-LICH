package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CreateHotelUseCase(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotel: Hotel): Flow<Result<String>> = flow {
        val newId = hotelRepository.createHotel(hotel)
        emit(Result.success(newId))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}