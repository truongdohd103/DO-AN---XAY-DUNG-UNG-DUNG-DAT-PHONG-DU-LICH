package com.example.chillstay.ui.home

import com.example.chillstay.core.base.UiEvent

sealed interface HomeIntent : UiEvent {
    data class ChangeHotelCategory(val categoryIndex: Int) : HomeIntent
    object RefreshHotels : HomeIntent
    data class ToggleBookmark(val hotelId: String) : HomeIntent
    object RefreshBookmarks : HomeIntent
    object RefreshUserSections : HomeIntent
}
