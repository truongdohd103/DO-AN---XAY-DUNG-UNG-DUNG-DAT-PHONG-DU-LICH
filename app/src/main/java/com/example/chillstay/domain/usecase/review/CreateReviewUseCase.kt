package com.example.chillstay.domain.usecase.review

import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.model.Booking


class CreateReviewUseCase constructor(
    private val reviewRepository: ReviewRepository,
    private val getUserBookings: GetUserBookingsUseCase,
    private val aggregateHotelRatingForHotel: AggregateHotelRatingForHotelUseCase
) {
    suspend operator fun invoke(
        userId: String,
        hotelId: String,
        comment: String,
        rating: Int
    ): Result<Review> {
        return try {
            // Validate inputs
            if (comment.isBlank()) {
                return Result.failure(Exception("Review text cannot be empty"))
            }
            
            if (rating < 1 || rating > 5) {
                return Result.failure(Exception("Rating must be between 1 and 5"))
            }
            
            // Check eligibility: user must have a COMPLETED booking for this hotel
            when (val bookingsResult = getUserBookings(userId, "COMPLETED")) {
                is Result.Success -> {
                    val hasCompleted = bookingsResult.data.any { it.hotelId == hotelId }
                    if (!hasCompleted) {
                        return Result.failure(Exception("You can only review hotels you've completed a stay at"))
                    }
                }
                is Result.Error -> {
                    return Result.failure(Exception(bookingsResult.throwable.message ?: "Failed to verify eligibility"))
                }
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
                comment = comment.trim(),
                rating = rating
            )
            
            val createdReview = reviewRepository.createReview(review)
            // After creation, aggregate rating and review count for the hotel
            aggregateHotelRatingForHotel(hotelId)
            Result.success(createdReview)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


