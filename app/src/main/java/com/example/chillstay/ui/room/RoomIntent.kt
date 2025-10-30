package com.example.chillstay.ui.room

import com.example.chillstay.core.base.UiEvent

sealed interface RoomIntent : UiEvent {
    data class LoadRooms(val hotelId: String) : RoomIntent
    data class RefreshRooms(val hotelId: String) : RoomIntent
    data class RetryLoad(val hotelId: String) : RoomIntent
}


