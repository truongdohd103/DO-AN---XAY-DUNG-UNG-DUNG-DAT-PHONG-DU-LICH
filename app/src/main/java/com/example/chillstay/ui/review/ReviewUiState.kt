package com.example.chillstay.ui.review

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel

@Immutable
data class ReviewUiState(
    val bookingId: String = "",
    val booking: Booking? = null,
    val hotel: Hotel? = null,
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isSubmitted: Boolean = false,
    val isEligible: Boolean = false,
    val existingReview: com.example.chillstay.domain.model.Review? = null,
    val isEditing: Boolean = false
) : UiState {
    fun updateBookingId(bookingId: String) = copy(bookingId = bookingId)
    
    fun updateBooking(booking: Booking?) = copy(booking = booking)
    
    fun updateHotel(hotel: Hotel?) = copy(hotel = hotel)
    
    fun updateRating(rating: Int) = copy(rating = rating)
    
    fun updateComment(comment: String) = copy(comment = comment)
    
    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)
    
    fun updateIsSubmitting(isSubmitting: Boolean) = copy(isSubmitting = isSubmitting)
    
    fun updateError(error: String?) = copy(error = error)
    
    fun clearError() = copy(error = null)
    
    fun updateIsSubmitted(isSubmitted: Boolean) = copy(isSubmitted = isSubmitted)
    fun updateIsEligible(isEligible: Boolean) = copy(isEligible = isEligible)

    fun updateExistingReview(review: com.example.chillstay.domain.model.Review?) = copy(existingReview = review)
    fun updateIsEditing(value: Boolean) = copy(isEditing = value)
}
