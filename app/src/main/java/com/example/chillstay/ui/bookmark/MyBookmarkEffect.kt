package com.example.chillstay.ui.bookmark

import com.example.chillstay.core.base.UiEffect

sealed interface MyBookmarkEffect : UiEffect {
    data class ShowError(val message: String) : MyBookmarkEffect
    data class ShowSuccess(val message: String) : MyBookmarkEffect
    data class NavigateToHotelDetail(val hotelId: String) : MyBookmarkEffect
    object ShowBookmarkRemoved : MyBookmarkEffect
    object RequireAuthentication : MyBookmarkEffect
}
