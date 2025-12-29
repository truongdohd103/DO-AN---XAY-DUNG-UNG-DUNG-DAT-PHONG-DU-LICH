package com.example.chillstay.domain.usecase.review

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetReviewByIdUseCase constructor(private val reviewRepository: ReviewRepository) {
    operator fun invoke(reviewId: String): Flow<Result<Review>> = flow {
        val review = reviewRepository.getReviewById(reviewId) ?: throw IllegalStateException("Review not found")
        emit(Result.success(review))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}