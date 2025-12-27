package com.example.chillstay.ui.admin.customer.customer_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipLevel

@Immutable
data class CustomerManageUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Data
    val allCustomers: List<User> = emptyList(),
    val customers: List<User> = emptyList(), // Filtered customers

    // Search & Filter
    val searchQuery: String = "",
    val selectedStatus: String = "", // "ACTIVE", "INACTIVE", or empty for all
    val selectedVipLevel: String = "", // VipLevel name or empty for all
    val availableVipLevels: List<String> = VipLevel.entries.map { it.displayName },

    // Dropdown states
    val isStatusExpanded: Boolean = false,
    val isVipLevelExpanded: Boolean = false,

    // Statistics
    val totalCustomers: Int = 0,
    val activeCustomers: Int = 0,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (customers.isEmpty()) 1 else (customers.size + itemsPerPage - 1) / itemsPerPage

    val paginatedCustomers: List<User>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, customers.size)
            return if (startIndex < customers.size) {
                customers.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Update functions - Search & Filter
    fun updateSearchQuery(query: String) = copy(
        searchQuery = query,
        currentPage = 1
    )

    fun updateSelectedStatus(status: String) = copy(
        selectedStatus = status,
        currentPage = 1
    )

    fun updateSelectedVipLevel(level: String) = copy(
        selectedVipLevel = level,
        currentPage = 1
    )

    // Update functions - Data
    fun updateAllCustomers(customers: List<User>) = copy(
        allCustomers = customers
    )

    fun updateFilteredCustomers(customers: List<User>) = copy(
        customers = customers,
        currentPage = 1
    )

    // Update functions - UI State
    fun updateCurrentPage(page: Int) = copy(currentPage = page)

    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun clearError() = copy(error = null)

    fun updateError(error: String?) = copy(error = error)

    // Dropdown functions
    fun toggleStatusExpanded() = copy(
        isStatusExpanded = !isStatusExpanded,
        isVipLevelExpanded = false
    )

    fun toggleVipLevelExpanded() = copy(
        isVipLevelExpanded = !isVipLevelExpanded,
        isStatusExpanded = false
    )

    fun setStatusExpanded(expanded: Boolean) = copy(isStatusExpanded = expanded)

    fun setVipLevelExpanded(expanded: Boolean) = copy(isVipLevelExpanded = expanded)
}