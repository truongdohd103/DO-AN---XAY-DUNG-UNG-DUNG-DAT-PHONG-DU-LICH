package com.example.chillstay.ui.admin.accommodation.room_manage

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Room

sealed interface RoomManageEffect : UiEffect {
    object NavigateBack : RoomManageEffect
    object NavigateToCreateRoom : RoomManageEffect
    data class NavigateToEditRoom(val room: Room) : RoomManageEffect
    data class ShowDisableSuccess(val room: Room) : RoomManageEffect
    data class ShowDeleteSuccess(val room: Room) : RoomManageEffect
    data class ShowError(val message: String) : RoomManageEffect
}

