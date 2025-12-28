package com.example.chillstay.ui.admin.accommodation.room_manage

import com.example.chillstay.domain.model.Room

data class RoomManageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val rooms: List<Room> = emptyList(),
    val totalRooms: Int = 0,
    val activeRooms: Int = 0
)

sealed interface RoomManageIntent {
    data class LoadRooms(val hotelId: String) : RoomManageIntent
    data object CreateNewRoom : RoomManageIntent
    data class EditRoom(val room: Room) : RoomManageIntent
    data class DeleteRoom(val room: Room) : RoomManageIntent
    data class DisableRoom(val room: Room) : RoomManageIntent
    data object NavigateBack : RoomManageIntent
}

sealed interface RoomManageEffect {
    data object NavigateBack : RoomManageEffect
    data object NavigateToCreateRoom : RoomManageEffect
    data class NavigateToEditRoom(val room: Room) : RoomManageEffect
    data class ShowDeleteSuccess(val room: Room) : RoomManageEffect
    data class ShowDisableSuccess(val room: Room) : RoomManageEffect
    data class ShowError(val message: String) : RoomManageEffect
}
