package com.example.chillstay.ui.home

import com.example.chillstay.core.base.UiEvent

sealed interface HomeIntent : UiEvent {
    data class ChangeHotelCategory(val categoryIndex: Int) : HomeIntent
    data class RefreshHotels(val categoryIndex: Int) : HomeIntent
    data class RetryLoadHotels(val categoryIndex: Int) : HomeIntent
    data class ToggleBookmark(val hotelId: String) : HomeIntent
    object RefreshBookmarks : HomeIntent
}
