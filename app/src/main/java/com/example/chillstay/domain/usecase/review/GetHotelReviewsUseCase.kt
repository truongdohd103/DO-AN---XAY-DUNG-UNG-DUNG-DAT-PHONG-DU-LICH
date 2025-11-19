package com.example.chillstay.domain.usecase.review

import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.core.common.Result


class GetHotelReviewsUseCase constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        hotelId: String,
        limit: Int? = null,
        offset: Int = 0
    ): Result<List<Review>> {
        return try {
            // Repository không còn có limit parameter, chỉ dùng offset
            val reviews = reviewRepository.getHotelReviews(hotelId, offset)
            // Apply limit trong memory nếu có
            val limitedReviews = if (limit != null && limit > 0) {
                reviews.take(limit)
            } else {
                reviews
            }
            Result.success(limitedReviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


