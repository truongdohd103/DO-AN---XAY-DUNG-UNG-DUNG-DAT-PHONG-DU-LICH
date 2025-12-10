package com.example.chillstay.domain.usecase.review

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository

class UpdateReviewUseCase constructor(
    private val reviewRepository: ReviewRepository,
    private val aggregateHotelRatingForHotel: AggregateHotelRatingForHotelUseCase
) {
    suspend operator fun invoke(review: Review): Result<Review> {
        return try {
            if (review.comment.isBlank()) {
                return Result.failure(IllegalArgumentException("Review text cannot be empty"))
            }
            if (review.rating < 1 || review.rating > 5) {
                return Result.failure(IllegalArgumentException("Rating must be between 1 and 5"))
            }
            val updated = reviewRepository.updateReview(review)
            aggregateHotelRatingForHotel(review.hotelId)
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

