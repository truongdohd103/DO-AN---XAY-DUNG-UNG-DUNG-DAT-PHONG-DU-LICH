package com.example.chillstay.domain.usecase.review

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.HotelRepository
import kotlin.math.round

class AggregateHotelRatingForHotelUseCase constructor(
    private val reviewRepository: ReviewRepository,
    private val hotelRepository: HotelRepository
) {
    suspend operator fun invoke(hotelId: String): Result<Pair<Double, Int>> {
        return try {
            val reviews = reviewRepository.getHotelReviews(hotelId, offset = 0)
            val count = reviews.size
            val avg = if (count > 0) reviews.sumOf { it.rating }.toDouble() / count else 0.0
            val rounded = round(avg * 10.0) / 10.0
            val updated = hotelRepository.updateHotelAggregation(hotelId, rounded, count)
            if (!updated) {
                return Result.failure(Exception("Failed to update hotel aggregation"))
            }
            Result.success(rounded to count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

