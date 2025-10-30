package com.example.chillstay.ui.home

import com.example.chillstay.core.base.UiEffect

sealed interface HomeEffect : UiEffect {
    data class ShowError(val message: String) : HomeEffect
    data class ShowSuccess(val message: String) : HomeEffect
    data class NavigateToHotelDetail(val hotelId: String) : HomeEffect
    object ShowBookmarkAdded : HomeEffect
    object ShowBookmarkRemoved : HomeEffect
}
