package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.core.common.Result


class GetHotelByIdUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<Hotel> {
        return try {
            val hotel = hotelRepository.getHotelById(hotelId)
            if (hotel != null) {
                Result.success(hotel)
            } else {
                Result.failure(Exception("Hotel not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


