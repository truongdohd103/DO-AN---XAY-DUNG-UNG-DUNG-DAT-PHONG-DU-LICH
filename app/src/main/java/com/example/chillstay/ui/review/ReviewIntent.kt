package com.example.chillstay.ui.review

import com.example.chillstay.core.base.UiEvent

sealed interface ReviewIntent : UiEvent {
    data class LoadBookingDetails(val bookingId: String) : ReviewIntent
    data class SubmitReview(val rating: Int, val comment: String) : ReviewIntent
    data class UpdateRating(val rating: Int) : ReviewIntent
    data class UpdateComment(val comment: String) : ReviewIntent
    object RetryLoad : ReviewIntent
}
