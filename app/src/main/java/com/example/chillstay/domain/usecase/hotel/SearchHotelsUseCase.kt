package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.core.common.Result


class SearchHotelsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(
        query: String,
        country: String? = null,
        city: String? = null,
        minRating: Double? = null,
        maxPrice: Double? = null
    ): Result<List<Hotel>> {
        return try {
            val hotels = hotelRepository.searchHotels(
                query = query,
                country = country,
                city = city,
                minRating = minRating,
                maxPrice = maxPrice
            )
            Result.success(hotels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

