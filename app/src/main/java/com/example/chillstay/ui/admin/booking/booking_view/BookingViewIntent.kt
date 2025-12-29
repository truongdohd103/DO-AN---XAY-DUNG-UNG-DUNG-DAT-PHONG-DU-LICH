package com.example.chillstay.ui.admin.booking.booking_view

import com.example.chillstay.core.base.UiEvent

sealed interface BookingViewIntent : UiEvent {
    data class LoadBooking(val bookingId: String) : BookingViewIntent
    object NavigateBack : BookingViewIntent
    object ClearError : BookingViewIntent
}