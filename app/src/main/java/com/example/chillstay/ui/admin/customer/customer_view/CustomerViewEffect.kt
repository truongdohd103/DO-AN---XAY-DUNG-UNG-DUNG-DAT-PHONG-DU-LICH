package com.example.chillstay.ui.admin.customer.customer_view

import com.example.chillstay.core.base.UiEffect

sealed interface CustomerViewEffect : UiEffect {
    data object NavigateBack : CustomerViewEffect
    data class NavigateToBookingDetail(val bookingId: String) : CustomerViewEffect
    data class NavigateToReviewDetail(val reviewId: String) : CustomerViewEffect
    data class ShowNotificationSent(val userId: String) : CustomerViewEffect
    data class ShowBlacklistSuccess(val userId: String) : CustomerViewEffect
    data class ShowError(val message: String) : CustomerViewEffect
}