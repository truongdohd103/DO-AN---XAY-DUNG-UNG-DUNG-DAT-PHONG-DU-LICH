package com.example.chillstay.ui.admin.customer.review_view

import com.example.chillstay.core.base.UiEvent

sealed interface ReviewViewIntent : UiEvent {
    data class LoadReview(val reviewId: String) : ReviewViewIntent
    object NavigateBack : ReviewViewIntent
    object ClearError : ReviewViewIntent
}