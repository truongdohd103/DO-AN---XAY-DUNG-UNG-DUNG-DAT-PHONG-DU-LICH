package com.example.chillstay.ui.admin.booking.booking_manage

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Booking

sealed interface BookingManageEffect : UiEffect {
    object NavigateBack : BookingManageEffect
    data class NavigateToView(val bookingId: String) : BookingManageEffect
    data class ShowError(val message: String) : BookingManageEffect
}