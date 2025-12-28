package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UpdateHotelUseCase(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotel: Hotel): Flow<Result<Boolean>> = flow {
        val success = hotelRepository.updateHotel(hotel)
        if (success) {
            emit(Result.success(true))
        } else {
            emit(Result.failure(Exception("Failed to update hotel")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}
