package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.core.common.Result


class GetHotelsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(): Result<List<Hotel>> {
        return try {
            val hotels = hotelRepository.getHotels()
            Result.success(hotels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

