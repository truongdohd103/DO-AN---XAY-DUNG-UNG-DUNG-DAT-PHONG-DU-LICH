package com.example.chillstay.ui.admin.statistics.accommodation_view

import com.example.chillstay.core.base.UiEvent

sealed interface AccommodationViewIntent : UiEvent {
    // Load operations
    data class LoadHotelStatistics(val hotelId: String) : AccommodationViewIntent
    data object ApplyFilters : AccommodationViewIntent

    // Date filter operations
    data class DateFromChanged(val dateMillis: Long?) : AccommodationViewIntent
    data class DateToChanged(val dateMillis: Long?) : AccommodationViewIntent
    data object ToggleDateFromPicker : AccommodationViewIntent
    data object ToggleDateToPicker : AccommodationViewIntent

    // Pagination operations
    data class GoToPage(val page: Int) : AccommodationViewIntent
    data object NextPage : AccommodationViewIntent
    data object PreviousPage : AccommodationViewIntent

    // Navigation
    data object NavigateBack : AccommodationViewIntent
    data class NavigateToRoom(val roomId: String) : AccommodationViewIntent
}