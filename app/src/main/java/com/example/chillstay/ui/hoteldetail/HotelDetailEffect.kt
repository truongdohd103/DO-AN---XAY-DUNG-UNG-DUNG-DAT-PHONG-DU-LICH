package com.example.chillstay.ui.hoteldetail

import com.example.chillstay.core.base.UiEffect

sealed interface HotelDetailEffect : UiEffect {
    data class ShowError(val message: String) : HotelDetailEffect
    data class ShowSuccess(val message: String) : HotelDetailEffect
    data class NavigateToRoomSelection(val hotelId: String) : HotelDetailEffect
    object ShowBookmarkAdded : HotelDetailEffect
    object ShowBookmarkRemoved : HotelDetailEffect
    object RequireAuthentication : HotelDetailEffect
}
