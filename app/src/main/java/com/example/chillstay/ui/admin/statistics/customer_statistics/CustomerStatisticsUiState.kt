package com.example.chillstay.ui.admin.statistics.customer_statistics

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.CustomerStats

@Immutable
data class CustomerStatisticsUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Filters
    val selectedYear: Int? = null,
    val selectedQuarter: Int? = null,
    val selectedMonth: Int? = null,

    // Dropdown states
    val isYearExpanded: Boolean = false,
    val isQuarterExpanded: Boolean = false,
    val isMonthExpanded: Boolean = false,

    // Available options
    val availableYears: List<Int> = listOf(2024, 2025, 2026),
    val availableQuarters: List<Int> = listOf(1, 2, 3, 4),
    val availableMonths: List<Int> = (1..12).toList(),

    // Statistics data
    val totalSpent: Double = 0.0,
    val totalBookings: Int = 0,
    val totalCustomers: Int = 0,
    val periodSpent: Map<String, Double> = emptyMap(),
    val periodLabels: List<String> = emptyList(),
    val customerStats: List<CustomerStats> = emptyList(),

    // Top performers
    val topByBookings: CustomerStats? = null,
    val topBySpent: CustomerStats? = null,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (customerStats.isEmpty()) 1
        else (customerStats.size + itemsPerPage - 1) / itemsPerPage

    val paginatedCustomerStats: List<CustomerStats>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, customerStats.size)
            return if (startIndex < customerStats.size) {
                customerStats.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Chart data for display
    val chartData: List<Pair<String, Double>>
        get() = periodLabels.map { label ->
            label to (periodSpent[label] ?: 0.0)
        }

    val maxRevenueValue: Double
        get() = periodSpent.values.maxOrNull() ?: 0.0

    // Get chart type display name
    val chartTypeDisplayName: String
        get() = when {
            selectedYear == null -> "📈 Revenue Trend (2024-2026)"
            selectedMonth != null -> "📈 Revenue Trend in ${getMonthName(selectedMonth)} $selectedYear"
            selectedQuarter != null -> "📈 Revenue Trend in Q$selectedQuarter $selectedYear"
            else -> "📈 Revenue Trend in $selectedYear"
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