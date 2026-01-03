package com.example.chillstay.ui.admin.home

import com.example.chillstay.core.base.UiEvent

sealed interface AdminHomeIntent : UiEvent {
    object NavigateToAccommodation : AdminHomeIntent
    object NavigateToVoucher : AdminHomeIntent
    object NavigateToCustomer : AdminHomeIntent
    object NavigateToNotification : AdminHomeIntent
    object NavigateToBooking : AdminHomeIntent
    object NavigateToAccommodationStatistics : AdminHomeIntent
    object NavigateToCustomerStatistics : AdminHomeIntent
    object ToggleStatistics : AdminHomeIntent
    object NavigateToPrice : AdminHomeIntent
    object NavigateToCalendar : AdminHomeIntent
    object NavigateToProfile : AdminHomeIntent
    object SignOut : AdminHomeIntent
    object ClearError : AdminHomeIntent
}
