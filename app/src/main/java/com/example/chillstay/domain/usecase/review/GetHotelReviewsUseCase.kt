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
            val reviews = reviewRepository.getHotelReviews(hotelId, limit, offset)
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

