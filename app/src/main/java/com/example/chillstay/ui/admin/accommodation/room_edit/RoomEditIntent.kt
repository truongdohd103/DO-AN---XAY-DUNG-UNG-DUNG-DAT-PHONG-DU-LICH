package com.example.chillstay.ui.admin.accommodation.room_edit

import com.example.chillstay.core.base.UiEvent

sealed class RoomEditIntent : UiEvent {
    data class LoadForCreate(val hotelId: String) : RoomEditIntent()
    data class LoadForEdit(val roomId: String) : RoomEditIntent()

    data class UpdateRoomName(val value: String) : RoomEditIntent()
    data class UpdateArea(val value: String) : RoomEditIntent()
    data class UpdateDoubleBeds(val value: String) : RoomEditIntent()
    data class UpdateSingleBeds(val value: String) : RoomEditIntent()
    data class UpdateMaxOccupancy(val value: String) : RoomEditIntent()
    data class UpdatePricePerNight(val value: String) : RoomEditIntent()
    data class UpdateDiscount(val value: String) : RoomEditIntent()
    data class UpdateAvailableQuantity(val value: String) : RoomEditIntent()
    data class UpdateBreakfastPrice(val value: String) : RoomEditIntent()

    data class AddImage(val url: String) : RoomEditIntent()
    data class RemoveImage(val index: Int) : RoomEditIntent()

    data class ToggleFeature(val feature: String) : RoomEditIntent()

    data object Save : RoomEditIntent()
    data object Create : RoomEditIntent()
    data object NavigateBack : RoomEditIntent()
    data object ClearError : RoomEditIntent()
}

