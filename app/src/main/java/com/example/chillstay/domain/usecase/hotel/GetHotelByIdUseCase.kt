package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetHotelByIdUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(hotelId: String): Flow<Result<Hotel>> = flow {
        val hotel = hotelRepository.getHotelById(hotelId) ?: throw IllegalStateException("Hotel not found")
        emit(Result.success(hotel))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
