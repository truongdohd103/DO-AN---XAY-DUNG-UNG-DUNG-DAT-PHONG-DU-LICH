package com.example.chillstay.ui.admin.accommodation.accommodation_manage

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.Hotel

sealed class AccommodationManageIntent : UiEvent {
    // Load operations
    data object LoadHotels : AccommodationManageIntent()
    data object LoadMoreHotels : AccommodationManageIntent()

    // Search operations
    data class SearchQueryChanged(val query: String) : AccommodationManageIntent()
    data object PerformSearch : AccommodationManageIntent()

    // Filter operations
    data class CountryChanged(val country: String) : AccommodationManageIntent()
    data class CityChanged(val city: String) : AccommodationManageIntent()
    data object ToggleCountryDropdown : AccommodationManageIntent()
    data object ToggleCityDropdown : AccommodationManageIntent()

    // Pagination operations
    data class GoToPage(val page: Int) : AccommodationManageIntent()
    data object NextPage : AccommodationManageIntent()
    data object PreviousPage : AccommodationManageIntent()

    // Hotel management operations
    data object CreateNew : AccommodationManageIntent()
    data class EditHotel(val hotel: Hotel) : AccommodationManageIntent()
    data class InvalidateHotel(val hotel: Hotel) : AccommodationManageIntent()
    data class DeleteHotel(val hotel: Hotel) : AccommodationManageIntent()

    // Error handling
    data object ClearError : AccommodationManageIntent()
}