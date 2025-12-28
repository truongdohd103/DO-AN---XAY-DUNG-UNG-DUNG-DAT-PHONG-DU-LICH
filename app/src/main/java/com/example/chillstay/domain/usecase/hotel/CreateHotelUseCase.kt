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
        val id = hotelRepository.createHotel(hotel)
        if (id.isNotEmpty()) {
            emit(Result.success(id))
        } else {
            emit(Result.failure(Exception("Failed to create hotel")))
        }
    }.catch { e ->
        emit(Result.failure(e))
    }
}
