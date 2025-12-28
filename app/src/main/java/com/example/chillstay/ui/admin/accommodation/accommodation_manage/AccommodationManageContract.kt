package com.example.chillstay.ui.admin.accommodation.accommodation_manage

import com.example.chillstay.domain.model.Hotel

data class AccommodationManageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hotels: List<Hotel> = emptyList(),
    val paginatedHotels: List<Hotel> = emptyList(),
    val searchQuery: String = "",
    val selectedCountry: String = "",
    val selectedCity: String = "",
    val availableCountries: List<String> = emptyList(),
    val availableCities: List<String> = emptyList(),
    val isCountryExpanded: Boolean = false,
    val isCityExpanded: Boolean = false,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val totalProperties: Int = 0,
    val activeProperties: Int = 0
)

sealed interface AccommodationManageIntent {
    data object LoadHotels : AccommodationManageIntent
    data class SearchQueryChanged(val query: String) : AccommodationManageIntent
    data object PerformSearch : AccommodationManageIntent
    data class CountryChanged(val country: String) : AccommodationManageIntent
    data class CityChanged(val city: String) : AccommodationManageIntent
    data object ToggleCountryDropdown : AccommodationManageIntent
    data object ToggleCityDropdown : AccommodationManageIntent
    data class EditHotel(val hotel: Hotel) : AccommodationManageIntent
    data class InvalidateHotel(val hotel: Hotel) : AccommodationManageIntent
    data class DeleteHotel(val hotel: Hotel) : AccommodationManageIntent
    data class GoToPage(val page: Int) : AccommodationManageIntent
    data object PreviousPage : AccommodationManageIntent
    data object NextPage : AccommodationManageIntent
}

sealed interface AccommodationManageEffect {
    data object NavigateBack : AccommodationManageEffect
    data object NavigateToCreateNew : AccommodationManageEffect
    data class NavigateToEdit(val hotel: Hotel) : AccommodationManageEffect
    data object NavigateToViewAll : AccommodationManageEffect
    data class ShowInvalidateSuccess(val hotel: Hotel) : AccommodationManageEffect
    data class ShowDeleteSuccess(val hotel: Hotel) : AccommodationManageEffect
    data class ShowError(val message: String) : AccommodationManageEffect
}
