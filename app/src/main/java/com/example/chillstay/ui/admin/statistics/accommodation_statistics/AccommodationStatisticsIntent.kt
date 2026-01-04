package com.example.chillstay.ui.admin.statistics.accommodation_statistics

import com.example.chillstay.core.base.UiEvent

sealed interface AccommodationStatisticsIntent : UiEvent {
    // Load operations
    data object LoadStatistics : AccommodationStatisticsIntent
    data object ApplyFilters : AccommodationStatisticsIntent

    // Year/Quarter/Month filter operations
    data class YearChanged(val year: Int?) : AccommodationStatisticsIntent  // null = "All"
    data class QuarterChanged(val quarter: Int?) : AccommodationStatisticsIntent  // null = "All"
    data class MonthChanged(val month: Int?) : AccommodationStatisticsIntent  // null = "All"

    data object ToggleYearDropdown : AccommodationStatisticsIntent
    data object ToggleQuarterDropdown : AccommodationStatisticsIntent
    data object ToggleMonthDropdown : AccommodationStatisticsIntent

    // Location filter operations
    data class CountryChanged(val country: String) : AccommodationStatisticsIntent
    data class CityChanged(val city: String) : AccommodationStatisticsIntent
    data object ToggleCountryDropdown : AccommodationStatisticsIntent
    data object ToggleCityDropdown : AccommodationStatisticsIntent
    data class ViewHotel(val hotelId: String) : AccommodationStatisticsIntent
    // Pagination operations
    data class GoToPage(val page: Int) : AccommodationStatisticsIntent
    data object NextPage : AccommodationStatisticsIntent
    data object PreviousPage : AccommodationStatisticsIntent

    // Navigation
    data object NavigateBack : AccommodationStatisticsIntent
}