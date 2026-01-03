package com.example.chillstay.ui.admin.statistics.accommodation_statistics

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.HotelCategory
import com.example.chillstay.domain.model.HotelListFilter
import com.example.chillstay.domain.usecase.booking.GetBookingStatisticsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccommodationStatisticsViewModel(
    private val getBookingStatisticsUseCase: GetBookingStatisticsUseCase,
    private val getHotelsUseCase: GetHotelsUseCase
) : BaseViewModel<AccommodationStatisticsUiState, AccommodationStatisticsIntent, AccommodationStatisticsEffect>(
    AccommodationStatisticsUiState()
) {

    val uiState = state

    // Cache to avoid reloading
    private var lastLoadedFilters: FilterKey? = null
    private var isInitialLoad = true

    data class FilterKey(
        val year: Int?,
        val quarter: Int?,
        val month: Int?,
        val country: String,
        val city: String
    )

    init {
        loadInitialData()
    }

    override fun onEvent(event: AccommodationStatisticsIntent) {
        when (event) {
            is AccommodationStatisticsIntent.LoadStatistics -> {
                loadStatistics(forceReload = true)
            }
            is AccommodationStatisticsIntent.ApplyFilters -> {
                loadStatistics(forceReload = true)
            }
            is AccommodationStatisticsIntent.YearChanged -> {
                _state.value = _state.value.copy(
                    selectedYear = event.year,
                    // Reset quarter and month when year changes
                    selectedQuarter = null,
                    selectedMonth = null
                )
            }
            is AccommodationStatisticsIntent.QuarterChanged -> {
                _state.value = _state.value.copy(
                    selectedQuarter = event.quarter,
                    // Reset month when quarter changes
                    selectedMonth = null
                )
            }
            is AccommodationStatisticsIntent.MonthChanged -> {
                _state.value = _state.value.copy(selectedMonth = event.month)
            }
            is AccommodationStatisticsIntent.ToggleYearDropdown -> {
                _state.value = _state.value.copy(
                    isYearExpanded = !_state.value.isYearExpanded,
                    isQuarterExpanded = false,
                    isMonthExpanded = false,
                    isCountryExpanded = false,
                    isCityExpanded = false
                )
            }
            is AccommodationStatisticsIntent.ToggleQuarterDropdown -> {
                _state.value = _state.value.copy(
                    isQuarterExpanded = !_state.value.isQuarterExpanded,
                    isYearExpanded = false,
                    isMonthExpanded = false,
                    isCountryExpanded = false,
                    isCityExpanded = false
                )
            }
            is AccommodationStatisticsIntent.ToggleMonthDropdown -> {
                _state.value = _state.value.copy(
                    isMonthExpanded = !_state.value.isMonthExpanded,
                    isYearExpanded = false,
                    isQuarterExpanded = false,
                    isCountryExpanded = false,
                    isCityExpanded = false
                )
            }
            is AccommodationStatisticsIntent.CountryChanged -> {
                updateCountryAndCities(event.country)
            }
            is AccommodationStatisticsIntent.CityChanged -> {
                _state.value = _state.value.copy(selectedCity = event.city)
            }
            is AccommodationStatisticsIntent.ToggleCountryDropdown -> {
                _state.value = _state.value.copy(
                    isCountryExpanded = !_state.value.isCountryExpanded,
                    isYearExpanded = false,
                    isQuarterExpanded = false,
                    isMonthExpanded = false,
                    isCityExpanded = false
                )
            }
            is AccommodationStatisticsIntent.ToggleCityDropdown -> {
                _state.value = _state.value.copy(
                    isCityExpanded = !_state.value.isCityExpanded,
                    isYearExpanded = false,
                    isQuarterExpanded = false,
                    isMonthExpanded = false,
                    isCountryExpanded = false
                )
            }
            is AccommodationStatisticsIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.copy(currentPage = event.page)
                }
            }
            is AccommodationStatisticsIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.copy(currentPage = currentPage + 1)
                }
            }
            is AccommodationStatisticsIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.copy(currentPage = currentPage - 1)
                }
            }
            is AccommodationStatisticsIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { AccommodationStatisticsEffect.NavigateBack }
                }
            }
        }
    }

    private fun getCurrentFilterKey(): FilterKey {
        val state = _state.value
        return FilterKey(
            year = state.selectedYear,
            quarter = state.selectedQuarter,
            month = state.selectedMonth,
            country = state.selectedCountry,
            city = state.selectedCity
        )
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                getHotelsUseCase(HotelListFilter(
                    category = HotelCategory.ALL,
                    limit = null
                )).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val hotels = result.data
                            val countries = hotels.map { it.country }.distinct().sorted()
                            val cities = hotels.map { it.city }.distinct().sorted()

                            _state.value = _state.value.copy(
                                availableCountries = countries,
                                availableCities = cities
                            )

                            // Load statistics after getting countries/cities
                            loadStatistics(forceReload = true)
                        }
                        is Result.Error -> {
                            Log.e("StatisticsVM", "Error loading hotels: ${result.throwable.message}")
                            sendEffect {
                                AccommodationStatisticsEffect.ShowError(
                                    result.throwable.message ?: "Failed to load hotels"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsVM", "Exception loading initial data: ${e.message}")
                sendEffect {
                    AccommodationStatisticsEffect.ShowError(
                        e.message ?: "Failed to load initial data"
                    )
                }
            }
        }
    }

    private fun loadStatistics(forceReload: Boolean) {
        viewModelScope.launch {
            val currentState = _state.value
            val currentFilters = getCurrentFilterKey()

            // Check if we need to reload
            if (!forceReload && currentFilters == lastLoadedFilters && !isInitialLoad) {
                Log.d("StatisticsVM", "Using cached data, skipping reload")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                Log.d("StatisticsVM", "Loading statistics with filters: " +
                        "year=${currentState.selectedYear}, " +
                        "quarter=${currentState.selectedQuarter}, " +
                        "month=${currentState.selectedMonth}, " +
                        "country=${currentState.selectedCountry}, " +
                        "city=${currentState.selectedCity}")

                getBookingStatisticsUseCase(
                    country = currentState.selectedCountry.ifBlank { null },
                    city = currentState.selectedCity.ifBlank { null },
                    year = currentState.selectedYear,
                    quarter = currentState.selectedQuarter,
                    month = currentState.selectedMonth
                ).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val stats = result.data

                            Log.d("StatisticsVM", "Statistics loaded: " +
                                    "revenue=${stats.totalRevenue}, " +
                                    "bookings=${stats.totalBookings}, " +
                                    "hotels=${stats.bookingsByHotel.size}, " +
                                    "periodLabels=${stats.periodLabels.size}, " +
                                    "periodRevenue=${stats.periodRevenue.size}")

                            // Debug period data
                            stats.periodLabels.forEach { label ->
                                val revenue = stats.periodRevenue[label] ?: 0.0
                                Log.d("StatisticsVM", "Period: $label -> Revenue: $revenue")
                            }

                            val sortedHotels = stats.bookingsByHotel.values
                                .sortedByDescending { it.revenue }
                                .toList()

                            // Calculate top performers
                            val topByBookings = stats.bookingsByHotel.values.maxByOrNull { it.bookings }
                            val topByRevenue = stats.bookingsByHotel.values.maxByOrNull { it.revenue }

                            Log.d("StatisticsVM", "Top by bookings: ${topByBookings?.hotelName} (${topByBookings?.bookings})")
                            Log.d("StatisticsVM", "Top by revenue: ${topByRevenue?.hotelName} ($${topByRevenue?.revenue})")

                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    totalRevenue = stats.totalRevenue,
                                    totalBookings = stats.totalBookings,
                                    cancellationRate = stats.cancellationRate,
                                    periodRevenue = stats.periodRevenue,
                                    periodLabels = stats.periodLabels,
                                    hotelStats = sortedHotels,
                                    topByBookings = topByBookings,
                                    topByRevenue = topByRevenue,
                                    currentPage = 1
                                )

                                // Update cache
                                lastLoadedFilters = currentFilters
                                isInitialLoad = false
                            }
                        }
                        is Result.Error -> {
                            Log.e("StatisticsVM", "Error loading statistics: ${result.throwable.message}")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.throwable.message ?: "Failed to load statistics"
                            )
                            sendEffect {
                                AccommodationStatisticsEffect.ShowError(
                                    result.throwable.message ?: "Failed to load statistics"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsVM", "Exception loading statistics: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }

    private fun updateCountryAndCities(country: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                getHotelsUseCase(HotelListFilter(
                    category = HotelCategory.ALL,
                    limit = null
                )).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val hotels = result.data
                            val updatedCities = if (country.isNotBlank()) {
                                hotels
                                    .filter { it.country.equals(country, ignoreCase = true) }
                                    .map { it.city }
                                    .distinct()
                                    .sorted()
                            } else {
                                hotels.map { it.city }.distinct().sorted()
                            }

                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    selectedCountry = country,
                                    selectedCity = "",
                                    availableCities = updatedCities
                                )
                            }
                        }
                        is Result.Error -> {
                            Log.e("StatisticsVM", "Error updating cities: ${result.throwable.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("StatisticsVM", "Exception updating cities: ${e.message}")
            }
        }
    }
}