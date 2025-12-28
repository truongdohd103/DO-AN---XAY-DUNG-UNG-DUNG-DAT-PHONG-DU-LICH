package com.example.chillstay.ui.admin.home

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.core.base.UiState

data class AdminHomeUiState(
    val greeting: String = "Welcome back, Admin!",
    val isStatisticsExpanded: Boolean = false,
    val error: String? = null
) : UiState

sealed interface AdminHomeIntent : UiEvent {
    data object NavigateToAccommodation : AdminHomeIntent
    data object NavigateToVoucher : AdminHomeIntent
    data object NavigateToCustomer : AdminHomeIntent
    data object NavigateToNotification : AdminHomeIntent
    data object NavigateToBooking : AdminHomeIntent
    data object NavigateToStatistics : AdminHomeIntent
    data object ToggleStatistics : AdminHomeIntent
    data object NavigateToPrice : AdminHomeIntent
    data object NavigateToCalendar : AdminHomeIntent
    data object NavigateToProfile : AdminHomeIntent
    data object SignOut : AdminHomeIntent
    data object ClearError : AdminHomeIntent
}

sealed interface AdminHomeEffect : UiEffect {
    data object NavigateToAccommodation : AdminHomeEffect
    data object NavigateToVoucher : AdminHomeEffect
    data object NavigateToCustomer : AdminHomeEffect
    data object NavigateToNotification : AdminHomeEffect
    data object NavigateToBooking : AdminHomeEffect
    data object NavigateToStatistics : AdminHomeEffect
    data object NavigateToPrice : AdminHomeEffect
    data object NavigateToCalendar : AdminHomeEffect
    data object NavigateToProfile : AdminHomeEffect
    data object NavigateToAuth : AdminHomeEffect
    data class ShowError(val message: String) : AdminHomeEffect
}
