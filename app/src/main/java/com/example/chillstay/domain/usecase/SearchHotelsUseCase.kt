package com.example.chillstay.domain.usecase

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository

class SearchHotelsUseCase(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(query: String): List<Hotel> = hotelRepository.searchHotels(query)
}


