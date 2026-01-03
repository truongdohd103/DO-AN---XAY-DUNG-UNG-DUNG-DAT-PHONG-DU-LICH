package com.example.chillstay.ui.admin.statistics.accommodation_statistics

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.HotelBookingStats

@Immutable
data class AccommodationStatisticsUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Filters
    val selectedYear: Int? = null,  // null = "All"
    val selectedQuarter: Int? = null,  // null = "All", 1-4
    val selectedMonth: Int? = null,  // null = "All", 1-12
    val selectedCountry: String = "",
    val selectedCity: String = "",

    // Dropdown states
    val isYearExpanded: Boolean = false,
    val isQuarterExpanded: Boolean = false,
    val isMonthExpanded: Boolean = false,
    val isCountryExpanded: Boolean = false,
    val isCityExpanded: Boolean = false,

    // Available options
    val availableYears: List<Int> = listOf(2024, 2025, 2026),
    val availableQuarters: List<Int> = listOf(1, 2, 3, 4),
    val availableMonths: List<Int> = (1..12).toList(),
    val availableCountries: List<String> = emptyList(),
    val availableCities: List<String> = emptyList(),

    // Statistics data
    val totalRevenue: Double = 0.0,
    val totalBookings: Int = 0,
    val cancellationRate: Double = 0.0,
    val periodRevenue: Map<String, Double> = emptyMap(),
    val periodLabels: List<String> = emptyList(),
    val hotelStats: List<HotelBookingStats> = emptyList(),

    // Top performers
    val topByBookings: HotelBookingStats? = null,
    val topByRevenue: HotelBookingStats? = null,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (hotelStats.isEmpty()) 1
        else (hotelStats.size + itemsPerPage - 1) / itemsPerPage

    val paginatedHotelStats: List<HotelBookingStats>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, hotelStats.size)
            return if (startIndex < hotelStats.size) {
                hotelStats.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Chart data for display - use periodLabels order
    val chartData: List<Pair<String, Double>>
        get() = periodLabels.map { label ->
            label to (periodRevenue[label] ?: 0.0)
        }

    val maxRevenueValue: Double
        get() = periodRevenue.values.maxOrNull() ?: 0.0

    // Get chart type display name
    val chartTypeDisplayName: String
        get() = when {
            selectedYear == null -> "Yearly Revenue (2024-2026)"
            selectedMonth != null -> "Weekly Revenue in ${getMonthName(selectedMonth)} $selectedYear"
            selectedQuarter != null -> "Monthly Revenue in Q$selectedQuarter $selectedYear"
            else -> "Monthly Revenue in $selectedYear"
        }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }
}