package com.example.chillstay.ui.admin.home

import com.example.chillstay.core.base.UiEffect

sealed interface AdminHomeEffect : UiEffect {
    object NavigateToAccommodation : AdminHomeEffect
    object NavigateToVoucher : AdminHomeEffect
    object NavigateToCustomer : AdminHomeEffect
    object NavigateToNotification : AdminHomeEffect
    object NavigateToBooking : AdminHomeEffect
    object NavigateToStatistics : AdminHomeEffect
    object NavigateToPrice : AdminHomeEffect
    object NavigateToCalendar : AdminHomeEffect
    object NavigateToProfile : AdminHomeEffect
    object NavigateToAuth : AdminHomeEffect
    data class ShowError(val message: String) : AdminHomeEffect
}
