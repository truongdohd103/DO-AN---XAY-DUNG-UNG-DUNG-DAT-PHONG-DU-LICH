package com.example.chillstay.ui.admin.statistics.customer_statistics

import com.example.chillstay.core.base.UiEvent

sealed interface CustomerStatisticsIntent : UiEvent {
    // Load operations
    data object LoadStatistics : CustomerStatisticsIntent
    data object ApplyFilters : CustomerStatisticsIntent

    // Year/Quarter/Month filter operations
    data class YearChanged(val year: Int?) : CustomerStatisticsIntent
    data class QuarterChanged(val quarter: Int?) : CustomerStatisticsIntent
    data class MonthChanged(val month: Int?) : CustomerStatisticsIntent

    data object ToggleYearDropdown : CustomerStatisticsIntent
    data object ToggleQuarterDropdown : CustomerStatisticsIntent
    data object ToggleMonthDropdown : CustomerStatisticsIntent

    // View customer detail
    data class ViewCustomer(val userId: String) : CustomerStatisticsIntent

    // Pagination operations
    data class GoToPage(val page: Int) : CustomerStatisticsIntent
    data object NextPage : CustomerStatisticsIntent
    data object PreviousPage : CustomerStatisticsIntent

    // Navigation
    data object NavigateBack : CustomerStatisticsIntent
}