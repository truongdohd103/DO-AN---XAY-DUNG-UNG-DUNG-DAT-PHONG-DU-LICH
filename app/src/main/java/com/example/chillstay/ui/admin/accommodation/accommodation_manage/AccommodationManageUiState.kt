package com.example.chillstay.ui.admin.accommodation.accommodation_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel

@Immutable
data class AccommodationManageUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,

    // Data
    val allHotels: List<Hotel> = emptyList(),
    val hotels: List<Hotel> = emptyList(), // Filtered hotels

    // Search & Filter
    val searchQuery: String = "",
    val selectedCountry: String = "",
    val selectedCity: String = "",
    val availableCountries: List<String> = emptyList(),
    val availableCities: List<String> = emptyList(),

    // Dropdown states
    val isCountryExpanded: Boolean = false,
    val isCityExpanded: Boolean = false,

    // Statistics - CẬP NHẬT: Giờ đây sẽ phản ánh filtered results
    val totalProperties: Int = 0,
    val activeProperties: Int = 0,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5,
    val hasMoreHotels: Boolean = false
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (hotels.isEmpty()) 1 else (hotels.size + itemsPerPage - 1) / itemsPerPage

    val paginatedHotels: List<Hotel>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, hotels.size)
            return if (startIndex < hotels.size) {
                hotels.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Update functions - Search & Filter
    fun updateSearchQuery(query: String) = copy(
        searchQuery = query,
        currentPage = 1
    )

    fun updateSelectedCountry(country: String) = copy(
        selectedCountry = country,
        currentPage = 1
    )

    fun updateSelectedCity(city: String) = copy(
        selectedCity = city,
        currentPage = 1
    )

    // Update functions - Data
    fun updateAllHotels(hotels: List<Hotel>) = copy(
        allHotels = hotels
        // KHÔNG cập nhật statistics ở đây nữa, sẽ cập nhật trong updateFilteredHotels
    )

    fun updateFilteredHotels(hotels: List<Hotel>) = copy(
        hotels = hotels,
        currentPage = 1
        // Statistics sẽ được cập nhật trực tiếp trong ViewModel
    )

    // Update functions - UI State
    fun updateCurrentPage(page: Int) = copy(currentPage = page)

    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun clearError() = copy(error = null)

    fun updateError(error: String?) = copy(error = error)

    // Dropdown functions
    fun toggleCountryExpanded() = copy(
        isCountryExpanded = !isCountryExpanded,
        isCityExpanded = false
    )

    fun toggleCityExpanded() = copy(
        isCityExpanded = !isCityExpanded,
        isCountryExpanded = false
    )

    fun setCountryExpanded(expanded: Boolean) = copy(isCountryExpanded = expanded)

    fun setCityExpanded(expanded: Boolean) = copy(isCityExpanded = expanded)
}