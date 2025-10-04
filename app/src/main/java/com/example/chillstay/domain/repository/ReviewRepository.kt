package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Review

interface ReviewRepository {
    suspend fun getHotelReviews(hotelId: String, limit: Int? = null, offset: Int = 0): List<Review>
    suspend fun getUserReviewForHotel(userId: String, hotelId: String): Review?
    suspend fun createReview(review: Review): Review
    suspend fun updateReview(review: Review): Review
    suspend fun deleteReview(reviewId: String): Boolean
}
