package com.example.chillstay.ui.admin.statistics.accommodation_view

import com.example.chillstay.core.base.UiEffect

sealed interface AccommodationViewEffect : UiEffect {
    data object NavigateBack : AccommodationViewEffect
    data class NavigateToRoom(val roomId : String) : AccommodationViewEffect
    data class ShowError(val message: String) : AccommodationViewEffect
    data class ShowSuccess(val message: String) : AccommodationViewEffect
}