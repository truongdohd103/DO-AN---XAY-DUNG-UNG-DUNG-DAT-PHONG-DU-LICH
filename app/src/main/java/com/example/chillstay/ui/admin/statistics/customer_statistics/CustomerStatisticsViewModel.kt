package com.example.chillstay.ui.admin.statistics.customer_statistics

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.booking.GetCustomerStatisticsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerStatisticsViewModel(
    private val getCustomerStatisticsUseCase: GetCustomerStatisticsUseCase
) : BaseViewModel<CustomerStatisticsUiState, CustomerStatisticsIntent, CustomerStatisticsEffect>(
    CustomerStatisticsUiState()
) {

    val uiState = state

    private var lastLoadedFilters: FilterKey? = null
    private var isInitialLoad = true

    data class FilterKey(
        val year: Int?,
        val quarter: Int?,
        val month: Int?
    )

    companion object {
        private const val TAG = "CustomerStatisticsVM"
    }

    init {
        loadStatistics(forceReload = true)
    }

    override fun onEvent(event: CustomerStatisticsIntent) {
        when (event) {
            is CustomerStatisticsIntent.LoadStatistics -> {
                loadStatistics(forceReload = true)
            }
            is CustomerStatisticsIntent.ApplyFilters -> {
                loadStatistics(forceReload = true)
            }
            is CustomerStatisticsIntent.YearChanged -> {
                _state.value = _state.value.copy(
                    selectedYear = event.year,
                    selectedQuarter = null,
                    selectedMonth = null
                )
            }
            is CustomerStatisticsIntent.QuarterChanged -> {
                _state.value = _state.value.copy(
                    selectedQuarter = event.quarter,
                    selectedMonth = null
                )
            }
            is CustomerStatisticsIntent.MonthChanged -> {
                _state.value = _state.value.copy(selectedMonth = event.month)
            }
            is CustomerStatisticsIntent.ToggleYearDropdown -> {
                _state.value = _state.value.copy(
                    isYearExpanded = !_state.value.isYearExpanded,
                    isQuarterExpanded = false,
                    isMonthExpanded = false
                )
            }
            is CustomerStatisticsIntent.ToggleQuarterDropdown -> {
                _state.value = _state.value.copy(
                    isQuarterExpanded = !_state.value.isQuarterExpanded,
                    isYearExpanded = false,
                    isMonthExpanded = false
                )
            }
            is CustomerStatisticsIntent.ToggleMonthDropdown -> {
                _state.value = _state.value.copy(
                    isMonthExpanded = !_state.value.isMonthExpanded,
                    isYearExpanded = false,
                    isQuarterExpanded = false
                )
            }
            is CustomerStatisticsIntent.ViewCustomer -> {
                viewModelScope.launch {
                    sendEffect {
                        CustomerStatisticsEffect.NavigateToCustomer(event.userId)
                    }
                }
            }
            is CustomerStatisticsIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.copy(currentPage = event.page)
                }
            }
            is CustomerStatisticsIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.copy(currentPage = currentPage + 1)
                }
            }
            is CustomerStatisticsIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.copy(currentPage = currentPage - 1)
                }
            }
            is CustomerStatisticsIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { CustomerStatisticsEffect.NavigateBack }
                }
            }
        }
    }

    private fun getCurrentFilterKey(): FilterKey {
        val state = _state.value
        return FilterKey(
            year = state.selectedYear,
            quarter = state.selectedQuarter,
            month = state.selectedMonth
        )
    }

    private fun loadStatistics(forceReload: Boolean) {
        viewModelScope.launch {
            val currentState = _state.value
            val currentFilters = getCurrentFilterKey()

            if (!forceReload && currentFilters == lastLoadedFilters && !isInitialLoad) {
                Log.d(TAG, "Using cached data, skipping reload")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                Log.d(TAG, "Loading customer statistics with filters: " +
                        "year=${currentState.selectedYear}, " +
                        "quarter=${currentState.selectedQuarter}, " +
                        "month=${currentState.selectedMonth}")

                // ✅ FIXED: Đổi từ .first() sang .collectLatest {}
                getCustomerStatisticsUseCase(
                    year = currentState.selectedYear,
                    quarter = currentState.selectedQuarter,
                    month = currentState.selectedMonth
                ).collectLatest { result ->  // ← THAY ĐỔI TẠI ĐÂY
                    when (result) {
                        is Result.Success -> {
                            val stats = result.data

                            Log.d(TAG, "Statistics loaded: " +
                                    "revenue=${stats.totalRevenue}, " +
                                    "bookings=${stats.totalBookings}, " +
                                    "customers=${stats.totalCustomers}")

                            val sortedCustomers = stats.bookingsByCustomer.values
                                .sortedByDescending { it.totalSpent }
                                .toList()

                            val topByBookings = stats.bookingsByCustomer.values.maxByOrNull { it.totalBookings }
                            val topByRevenue = stats.bookingsByCustomer.values.maxByOrNull { it.totalSpent }

                            Log.d(TAG, "Top by bookings: ${topByBookings?.name} (${topByBookings?.totalBookings})")
                            Log.d(TAG, "Top by revenue: ${topByRevenue?.name} ($${topByRevenue?.totalSpent})")

                            // ✅ FIXED: Vẫn giữ withContext(Dispatchers.Main)
                            withContext(Dispatchers.Main) {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    totalSpent = stats.totalRevenue,
                                    totalBookings = stats.totalBookings,
                                    totalCustomers = stats.totalCustomers,
                                    periodSpent = stats.periodRevenue,
                                    periodLabels = stats.periodLabels,
                                    customerStats = sortedCustomers,
                                    topByBookings = topByBookings,
                                    topBySpent = topByRevenue,
                                    currentPage = 1
                                )

                                lastLoadedFilters = currentFilters
                                isInitialLoad = false
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Error loading statistics: ${result.throwable.message}")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.throwable.message ?: "Failed to load statistics"
                            )
                            sendEffect {
                                CustomerStatisticsEffect.ShowError(
                                    result.throwable.message ?: "Failed to load statistics"
                                )
                            }
                        }
                    }
                }  // ← KẾT THÚC collectLatest
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading statistics: ${e.message}")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
            }
        }
    }
}