package com.example.chillstay.ui.admin.home

import com.example.chillstay.core.base.UiEvent

sealed class AdminHomeIntent : UiEvent {
    object NavigateToAccommodation : AdminHomeIntent()
    object NavigateToVoucher : AdminHomeIntent()
    object NavigateToCustomer : AdminHomeIntent()
    object NavigateToNotification : AdminHomeIntent()
    object NavigateToBooking : AdminHomeIntent()
    object NavigateToStatistics : AdminHomeIntent()
    object ToggleStatistics : AdminHomeIntent()
    object NavigateToPrice : AdminHomeIntent()
    object NavigateToCalendar : AdminHomeIntent()
    object NavigateToProfile : AdminHomeIntent()
    object ClearError : AdminHomeIntent()
}
