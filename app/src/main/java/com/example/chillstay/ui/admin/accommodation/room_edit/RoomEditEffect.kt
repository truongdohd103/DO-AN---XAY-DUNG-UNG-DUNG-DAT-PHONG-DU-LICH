package com.example.chillstay.ui.admin.accommodation.room_edit

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Room

sealed interface RoomEditEffect : UiEffect {
    object NavigateBack : RoomEditEffect
    data class ShowSaveSuccess(val room: Room) : RoomEditEffect
    data class ShowCreateSuccess(val room: Room) : RoomEditEffect
    data class ShowError(val message: String) : RoomEditEffect
}

