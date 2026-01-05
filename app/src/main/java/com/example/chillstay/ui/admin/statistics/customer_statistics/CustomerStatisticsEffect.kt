package com.example.chillstay.ui.admin.statistics.customer_statistics

import com.example.chillstay.core.base.UiEffect

sealed interface CustomerStatisticsEffect : UiEffect {
    data object NavigateBack : CustomerStatisticsEffect
    data class NavigateToCustomer(val userId: String) : CustomerStatisticsEffect
    data class ShowError(val message: String) : CustomerStatisticsEffect
    data class ShowSuccess(val message: String) : CustomerStatisticsEffect
}