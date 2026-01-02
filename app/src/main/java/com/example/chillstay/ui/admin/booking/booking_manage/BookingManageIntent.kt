package com.example.chillstay.ui.admin.booking.booking_manage

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.Booking

sealed interface BookingManageIntent : UiEvent {
    // Load operations
    data object LoadBookings : BookingManageIntent

    // Search operations
    data class SearchQueryChanged(val query: String) : BookingManageIntent
    data object PerformSearch : BookingManageIntent

    // Date filter operations
    data class DateFromChanged(val date: Long?) : BookingManageIntent
    data class DateToChanged(val date: Long?) : BookingManageIntent
    data object ToggleDateFromPicker : BookingManageIntent
    data object ToggleDateToPicker : BookingManageIntent
    data object ClearDateFilters : BookingManageIntent

    // Pagination operations
    data class GoToPage(val page: Int) : BookingManageIntent
    data object NextPage : BookingManageIntent
    data object PreviousPage : BookingManageIntent

    // Navigation operations
    data object NavigateBack : BookingManageIntent
    data class ViewBooking(val bookingId: String) : BookingManageIntent

    // Error handling
    data object ClearError : BookingManageIntent
}