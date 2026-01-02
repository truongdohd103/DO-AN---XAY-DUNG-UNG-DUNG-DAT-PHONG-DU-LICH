package com.example.chillstay.ui.admin.customer.customer_view

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.CustomerActivity
import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipLevel

@Immutable
data class CustomerViewUiState(
    // Loading states
    val isLoading: Boolean = false,
    val isLoadingActivities: Boolean = false,
    val error: String? = null,

    // Data
    val user: User? = null,
    val stats: CustomerStats = CustomerStats(),
    val vipLevel: VipLevel = VipLevel.BRONZE,

    // Activities
    val allActivities: List<CustomerActivity> = emptyList(),
    val filteredActivities: List<CustomerActivity> = emptyList(),
    val selectedTab: ActivityTab = ActivityTab.BOOKING,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5,

    // UI state
    val userId: String = ""
) : UiState {

    // Computed properties for pagination
    val totalPages: Int
        get() = if (filteredActivities.isEmpty()) 1 else (filteredActivities.size + itemsPerPage - 1) / itemsPerPage

    val paginatedActivities: List<CustomerActivity>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, filteredActivities.size)
            return if (startIndex < filteredActivities.size) {
                filteredActivities.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    fun updateUser(user: User) = copy(user = user)

    fun updateStats(stats: CustomerStats) = copy(stats = stats)

    fun updateVipLevel(level: VipLevel) = copy(vipLevel = level)

    fun updateAllActivities(activities: List<CustomerActivity>) = copy(
        allActivities = activities
    )

    fun updateSelectedTab(tab: ActivityTab) = copy(
        selectedTab = tab,
        currentPage = 1  // Reset to page 1 when changing tabs
    )

    fun updateFilteredActivities(activities: List<CustomerActivity>) = copy(
        filteredActivities = activities,
        currentPage = 1  // Reset to page 1 when filtering
    )

    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun updateIsLoadingActivities(isLoading: Boolean) = copy(isLoadingActivities = isLoading)

    fun updateError(error: String?) = copy(error = error)

    fun clearError() = copy(error = null)

    fun updateCurrentPage(page: Int) = copy(currentPage = page)
}