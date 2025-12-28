package com.example.chillstay.ui.admin.customer.customer_manage

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.User

sealed interface CustomerManageIntent : UiEvent {
    // Load operations
    data object LoadCustomers : CustomerManageIntent

    // Search operations
    data class SearchQueryChanged(val query: String) : CustomerManageIntent
    data object PerformSearch : CustomerManageIntent

    // Filter operations
    data class StatusFilterChanged(val status: String) : CustomerManageIntent
    data class VipLevelFilterChanged(val level: String) : CustomerManageIntent
    data object ToggleStatusDropdown : CustomerManageIntent
    data object ToggleVipLevelDropdown : CustomerManageIntent

    // Pagination operations
    data class GoToPage(val page: Int) : CustomerManageIntent
    data object NextPage : CustomerManageIntent
    data object PreviousPage : CustomerManageIntent

    // Customer management operations
    data class ViewCustomer(val user: User) : CustomerManageIntent
    data class ToggleCustomerStatus(val user: User) : CustomerManageIntent

    // Error handling
    data object ClearError : CustomerManageIntent
}