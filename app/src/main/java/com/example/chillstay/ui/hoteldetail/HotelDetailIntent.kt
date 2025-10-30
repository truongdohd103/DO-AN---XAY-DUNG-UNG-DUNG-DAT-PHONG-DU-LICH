package com.example.chillstay.ui.hoteldetail

import com.example.chillstay.core.base.UiEvent

sealed interface HotelDetailIntent : UiEvent {
    data class LoadHotelDetails(val hotelId: String) : HotelDetailIntent
    data class ToggleBookmark(val hotelId: String, val isBookmarked: Boolean) : HotelDetailIntent
    data class RetryLoad(val hotelId: String) : HotelDetailIntent
    data class NavigateToRooms(val hotelId: String) : HotelDetailIntent
}
