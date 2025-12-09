package com.example.chillstay.domain.usecase.hotel

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelCategory
import com.example.chillstay.domain.model.HotelListFilter
import com.example.chillstay.domain.repository.HotelRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetHotelsUseCase constructor(
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(filter: HotelListFilter = HotelListFilter()): Flow<Result<List<Hotel>>> = flow {
        val hotels = hotelRepository.getHotels()
        val filtered = hotels.asSequence()
            .filter { filter.country?.let { country -> it.country.equals(country, ignoreCase = true) } ?: true }
            .filter { filter.city?.let { city -> it.city.equals(city, ignoreCase = true) } ?: true }
            .filter { filter.minRating?.let { rating -> it.rating >= rating } ?: true }
            .let { sequence ->
                when (filter.category) {
                    HotelCategory.RECOMMENDED -> sequence.sortedByDescending { it.numberOfReviews }
                    HotelCategory.TRENDING -> sequence.sortedByDescending {
                        val roomMinPrice = it.rooms.minOfOrNull { room -> room.price }
                        val baselinePrice = roomMinPrice ?: it.minPrice ?: 0.0
                        baselinePrice
                    }
                    else -> sequence.sortedByDescending { it.rating }
                }
            }
            .let { sequence -> filter.limit?.let { sequence.take(it) } ?: sequence }
            .toList()
        emit(Result.success(filtered))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
