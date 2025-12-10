package com.example.chillstay.ui.review

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.review.CreateReviewUseCase
import com.example.chillstay.domain.usecase.review.UpdateReviewUseCase
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class ReviewViewModel(
    private val getUserBookings: GetUserBookingsUseCase,
    private val getHotelById: GetHotelByIdUseCase,
    private val getBookingById: GetBookingByIdUseCase,
    private val createReview: CreateReviewUseCase,
    private val getCurrentUserId: GetCurrentUserIdUseCase,
    private val updateReviewUseCase: UpdateReviewUseCase,
    private val reviewRepository: ReviewRepository
) : BaseViewModel<ReviewUiState, ReviewIntent, ReviewEffect>(ReviewUiState()) {

    override fun onEvent(event: ReviewIntent) = when (event) {
        is ReviewIntent.LoadBookingDetails -> handleLoadBookingDetails(event.bookingId)
        is ReviewIntent.SubmitReview -> handleSubmitReview(event.rating, event.comment)
        is ReviewIntent.UpdateRating -> handleUpdateRating(event.rating)
        is ReviewIntent.UpdateComment -> handleUpdateComment(event.comment)
        is ReviewIntent.RetryLoad -> handleRetryLoad()
    }

    private fun handleLoadBookingDetails(bookingId: String) {
        Log.d("ReviewViewModel", "Loading booking details for: $bookingId")
        _state.update { 
            it.updateBookingId(bookingId)
                .updateIsLoading(true)
                .clearError()
        }
        
        viewModelScope.launch {
            try {
                val bookingResult = getBookingById(bookingId)
                when (bookingResult) {
                    is Result.Success -> {
                        val booking = bookingResult.data ?: run {
                            _state.update { it.updateIsLoading(false).updateError("Booking not found") }
                            sendEffect { ReviewEffect.ShowError("Booking not found") }
                            return@launch
                        }
                        _state.update { it.updateBooking(booking) }
                        getHotelById(booking.hotelId).collect { hotelRes ->
                            when (hotelRes) {
                                is Result.Success -> {
                                    _state.update { it.updateHotel(hotelRes.data) }
                                }
                                is Result.Error -> {
                                    _state.update { it.updateError(hotelRes.throwable.message) }
                                }
                            }
                        }
                        val userIdRes = getCurrentUserId().first()
                        val userId = if (userIdRes is Result.Success) userIdRes.data else null
                        if (userId != null) {
                            when (val completed = getUserBookings(userId, "COMPLETED")) {
                                is Result.Success -> {
                                    val eligible = completed.data.any { it.hotelId == booking.hotelId }
                                    _state.update { it.updateIsEligible(eligible).updateIsLoading(false) }
                                    try {
                                        val existing = reviewRepository.getUserReviewForHotel(userId, booking.hotelId)
                                        if (existing != null) {
                                            _state.update { 
                                                it.updateExistingReview(existing)
                                                    .updateIsEditing(true)
                                                    .updateRating(existing.rating)
                                                    .updateComment(existing.comment)
                                            }
                                        } else {
                                            _state.update { it.updateExistingReview(null).updateIsEditing(false) }
                                        }
                                    } catch (_: Exception) {}
                                }
                                is Result.Error -> {
                                    _state.update { it.updateIsEligible(false).updateIsLoading(false).updateError(completed.throwable.message) }
                                }
                            }
                        } else {
                            _state.update { it.updateIsEligible(false).updateIsLoading(false).updateError("User not authenticated") }
                            sendEffect { ReviewEffect.ShowError("Please sign in to write a review") }
                        }
                    }
                    is Result.Error -> {
                        _state.update { 
                            it.updateIsLoading(false)
                                .updateError(bookingResult.throwable.message ?: "Failed to load booking details")
                        }
                        sendEffect { ReviewEffect.ShowError(bookingResult.throwable.message ?: "Failed to load booking details") }
                    }
                }
            } catch (exception: Exception) {
                Log.e("ReviewViewModel", "Exception loading booking details: ${exception.message}")
                _state.update { 
                    it.updateIsLoading(false)
                        .updateError(exception.message ?: "Unknown error")
                }
                viewModelScope.launch {
                    sendEffect { ReviewEffect.ShowError(exception.message ?: "Failed to load booking details") }
                }
            }
        }
    }

    private fun handleSubmitReview(rating: Int, comment: String) {
        Log.d("ReviewViewModel", "Submitting review: rating=$rating, comment=$comment")
        _state.update { it.updateIsSubmitting(true) }
        
        viewModelScope.launch {
            try {
                val booking = _state.value.booking
                val userIdRes = getCurrentUserId().first()
                val userId = if (userIdRes is Result.Success) userIdRes.data else null
                if (booking == null || userId == null) {
                    _state.update { it.updateIsSubmitting(false).updateError("Missing booking or user") }
                    sendEffect { ReviewEffect.ShowError("Missing booking or user") }
                    return@launch
                }
                if (_state.value.isEditing && _state.value.existingReview != null) {
                    val updated = _state.value.existingReview!!.copy(
                        comment = comment.trim(),
                        rating = rating,
                        createdAt = com.google.firebase.Timestamp.now()
                    )
                    when (val res = updateReviewUseCase(updated)) {
                        is Result.Success -> {
                            _state.update { it.updateIsSubmitting(false).updateIsSubmitted(true) }
                            sendEffect { ReviewEffect.ShowReviewSubmitted }
                        }
                        is Result.Error -> {
                            _state.update { it.updateIsSubmitting(false).updateError(res.throwable.message ?: "Failed to submit review") }
                            sendEffect { ReviewEffect.ShowError(res.throwable.message ?: "Failed to submit review") }
                        }
                    }
                } else {
                    val result = createReview(userId, booking.hotelId, comment, rating)
                    when (result) {
                        is Result.Success -> {
                            _state.update { it.updateIsSubmitting(false).updateIsSubmitted(true) }
                            sendEffect { ReviewEffect.ShowReviewSubmitted }
                            Log.d("ReviewViewModel", "Review submitted successfully")
                        }
                        is Result.Error -> {
                            _state.update { it.updateIsSubmitting(false).updateError(result.throwable.message ?: "Failed to submit review") }
                            sendEffect { ReviewEffect.ShowError(result.throwable.message ?: "Failed to submit review") }
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e("ReviewViewModel", "Exception submitting review: ${exception.message}")
                _state.update { 
                    it.updateIsSubmitting(false)
                        .updateError(exception.message ?: "Failed to submit review")
                }
                viewModelScope.launch {
                    sendEffect { ReviewEffect.ShowError(exception.message ?: "Failed to submit review") }
                }
            }
        }
    }

    private fun handleUpdateRating(rating: Int) {
        _state.update { it.updateRating(rating) }
    }

    private fun handleUpdateComment(comment: String) {
        _state.update { it.updateComment(comment) }
    }

    private fun handleRetryLoad() {
        val currentBookingId = _state.value.bookingId
        if (currentBookingId.isNotEmpty()) {
            handleLoadBookingDetails(currentBookingId)
        }
    }
}
