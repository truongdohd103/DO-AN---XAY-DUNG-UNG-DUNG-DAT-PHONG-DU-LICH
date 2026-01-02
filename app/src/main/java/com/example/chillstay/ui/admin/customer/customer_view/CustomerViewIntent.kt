package com.example.chillstay.ui.admin.customer.customer_view

import com.example.chillstay.core.base.UiEvent

sealed interface CustomerViewIntent : UiEvent {
    data class LoadCustomerDetails(val userId: String) : CustomerViewIntent
    data class SelectActivityTab(val tab: ActivityTab) : CustomerViewIntent
    data class ViewActivity(val activityId: String, val activityType: String) : CustomerViewIntent
    data object SendNotification : CustomerViewIntent

    // Pagination operations
    data class GoToPage(val page: Int) : CustomerViewIntent
    data object NextPage : CustomerViewIntent
    data object PreviousPage : CustomerViewIntent

    data object AddToBlacklist : CustomerViewIntent
    data object ClearError : CustomerViewIntent
}

enum class ActivityTab {
    BOOKING,
    REVIEW
}