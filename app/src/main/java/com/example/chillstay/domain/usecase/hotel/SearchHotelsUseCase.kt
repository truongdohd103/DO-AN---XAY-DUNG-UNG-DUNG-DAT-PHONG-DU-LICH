package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class SearchHotelsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(
        query: String,
        country: String? = null,
        city: String? = null,
        minRating: Double? = null,
        maxPrice: Double? = null
    ): Flow<Result<List<Hotel>>> = flow {
        val hotels = hotelRepository.searchHotels(
            query = query,
            country = country,
            city = city,
            minRating = minRating,
            maxPrice = maxPrice
        )
        emit(Result.success(hotels))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
