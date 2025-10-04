package com.example.chillstay.domain.usecase

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository

class GetHotelsUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(): List<Hotel> = hotelRepository.getHotels()
}


