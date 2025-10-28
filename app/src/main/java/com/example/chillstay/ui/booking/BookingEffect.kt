package com.example.chillstay.ui.booking

import com.example.chillstay.core.base.UiEffect

sealed interface BookingEffect : UiEffect {
    data class ShowError(val message: String) : BookingEffect
    object ShowBookingCreated : BookingEffect
    data class NavigateToBookingDetail(val bookingId: String) : BookingEffect
    object RequireAuthentication : BookingEffect
}
