package com.example.chillstay.ui.room

sealed interface RoomIntent {
    data class LoadRooms(val hotelId: String) : RoomIntent
    data class RefreshRooms(val hotelId: String) : RoomIntent
    data class RetryLoad(val hotelId: String) : RoomIntent
}


