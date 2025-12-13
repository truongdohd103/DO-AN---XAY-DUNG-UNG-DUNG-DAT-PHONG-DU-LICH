package com.example.chillstay.domain.usecase.review

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.ReviewRepository

class DeleteReviewUseCase constructor(
    private val reviewRepository: ReviewRepository,
    private val aggregateHotelRatingForHotel: AggregateHotelRatingForHotelUseCase
) {
    suspend operator fun invoke(reviewId: String, hotelId: String): Result<Boolean> {
        return try {
            val ok = reviewRepository.deleteReview(reviewId)
            if (!ok) return Result.failure(IllegalStateException("Failed to delete review"))
            aggregateHotelRatingForHotel(hotelId)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

