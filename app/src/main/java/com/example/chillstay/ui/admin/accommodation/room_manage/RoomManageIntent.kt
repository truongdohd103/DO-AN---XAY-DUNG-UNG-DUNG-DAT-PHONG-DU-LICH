package com.example.chillstay.ui.admin.accommodation.room_manage

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.Room

sealed class RoomManageIntent : UiEvent {
    data class LoadRooms(val hotelId: String) : RoomManageIntent()
    data object NavigateBack : RoomManageIntent()
    data object CreateNewRoom : RoomManageIntent()
    data class EditRoom(val room: Room) : RoomManageIntent()
    data class DisableRoom(val room: Room) : RoomManageIntent()
    data class DeleteRoom(val room: Room) : RoomManageIntent()
    data object ClearError : RoomManageIntent()
}

