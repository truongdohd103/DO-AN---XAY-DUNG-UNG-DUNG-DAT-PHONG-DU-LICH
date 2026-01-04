package com.example.chillstay.ui.admin.statistics.accommodation_statistics

import com.example.chillstay.core.base.UiEffect

sealed interface AccommodationStatisticsEffect : UiEffect {
    data object NavigateBack : AccommodationStatisticsEffect
    data class NavigateToView(val hotelId: String) : AccommodationStatisticsEffect
    data class ShowError(val message: String) : AccommodationStatisticsEffect
    data class ShowSuccess(val message: String) : AccommodationStatisticsEffect
}