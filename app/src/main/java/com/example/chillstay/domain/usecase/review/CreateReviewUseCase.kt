package com.example.chillstay.domain.usecase.review

import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.core.common.Result
import java.time.LocalDate


class CreateReviewUseCase constructor(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        userId: String,
        hotelId: String,
        text: String,
        rating: Int
    ): Result<Review> {
        return try {
            // Validate inputs
            if (text.isBlank()) {
                return Result.failure(Exception("Review text cannot be empty"))
            }
            
            if (rating < 1 || rating > 5) {
                return Result.failure(Exception("Rating must be between 1 and 5"))
            }
            
            // Check if user already reviewed this hotel
            val existingReview = reviewRepository.getUserReviewForHotel(userId, hotelId)
            if (existingReview != null) {
                return Result.failure(Exception("You have already reviewed this hotel"))
            }
            
            val review = Review(
                id = "", // Will be set by repository
                userId = userId,
                hotelId = hotelId,
                text = text.trim(),
                rating = rating,
                created = LocalDate.now()
            )
            
            val createdReview = reviewRepository.createReview(review)
            Result.success(createdReview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

