package com.example.chillstay.ui.review

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class ReviewViewModel(
    private val getUserBookings: GetUserBookingsUseCase,
    private val getHotelById: GetHotelByIdUseCase
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
                // For now, we'll simulate loading booking details
                // In a real implementation, you'd have a GetBookingByIdUseCase
                _state.update { 
                    it.updateIsLoading(false)
                        .updateError("Booking details loading not implemented yet")
                }
                viewModelScope.launch {
                    sendEffect { ReviewEffect.ShowError("Booking details loading not implemented yet") }
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
                // Simulate API call
                kotlinx.coroutines.delay(2000)
                
                _state.update { 
                    it.updateIsSubmitting(false)
                        .updateIsSubmitted(true)
                }
                
                viewModelScope.launch {
                    sendEffect { ReviewEffect.ShowReviewSubmitted }
                }
                
                Log.d("ReviewViewModel", "Review submitted successfully")
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
