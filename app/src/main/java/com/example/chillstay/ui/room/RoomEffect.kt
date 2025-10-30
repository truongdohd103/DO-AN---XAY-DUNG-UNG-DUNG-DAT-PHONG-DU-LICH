package com.example.chillstay.ui.room

import com.example.chillstay.core.base.UiEffect

sealed interface RoomEffect : UiEffect {
    data class ShowError(val message: String) : RoomEffect
    data class ShowSuccess(val message: String) : RoomEffect
    data class NavigateToBooking(val hotelId: String, val roomId: String, val dateFrom: String, val dateTo: String) : RoomEffect
    object RequireAuthentication : RoomEffect
}
