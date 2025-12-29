package com.example.chillstay.ui.admin.booking.booking_view

import com.example.chillstay.core.base.UiEffect

sealed interface BookingViewEffect : UiEffect {
    object NavigateBack : BookingViewEffect
    data class ShowError(val message: String) : BookingViewEffect
}