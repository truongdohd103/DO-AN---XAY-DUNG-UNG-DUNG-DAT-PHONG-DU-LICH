package com.example.chillstay.ui.admin.statistics.room_view

import com.example.chillstay.core.base.UiEffect

sealed interface RoomViewEffect : UiEffect {
    data object NavigateBack : RoomViewEffect
    data class ShowError(val message: String) : RoomViewEffect
    data class ShowSuccess(val message: String) : RoomViewEffect
}