package com.example.chillstay.ui.admin.accommodation.room_edit

import com.example.chillstay.domain.model.Room

data class RoomEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val mode: Mode = Mode.Create,
    
    val roomName: String = "",
    val area: String = "",
    val doubleBeds: String = "",
    val singleBeds: String = "",
    val maxOccupancy: String = "",
    
    val pricePerNight: String = "",
    val discount: String = "",
    val availableQuantity: String = "",
    
    val imageUrls: List<String> = emptyList(),
    
    val availableFeatures: List<String> = listOf("AC", "TV", "WiFi", "Minibar", "Balcony", "Bathtub", "Kitchen", "Workspace"),
    val selectedFeatures: List<String> = emptyList(),
    
    val breakfastPrice: String = ""
)

enum class Mode {
    Create, Edit
}

sealed interface RoomEditIntent {
    data class LoadForCreate(val hotelId: String) : RoomEditIntent
    data class LoadForEdit(val roomId: String) : RoomEditIntent
    
    data class UpdateRoomName(val name: String) : RoomEditIntent
    data class UpdateArea(val area: String) : RoomEditIntent
    data class UpdateDoubleBeds(val count: String) : RoomEditIntent
    data class UpdateSingleBeds(val count: String) : RoomEditIntent
    data class UpdateMaxOccupancy(val count: String) : RoomEditIntent
    
    data class UpdatePricePerNight(val price: String) : RoomEditIntent
    data class UpdateDiscount(val discount: String) : RoomEditIntent
    data class UpdateAvailableQuantity(val quantity: String) : RoomEditIntent
    
    data class RemoveImage(val index: Int) : RoomEditIntent
    data class AddImage(val url: String) : RoomEditIntent
    
    data class ToggleFeature(val feature: String) : RoomEditIntent
    data class UpdateBreakfastPrice(val price: String) : RoomEditIntent
    
    data object Save : RoomEditIntent
    data object Create : RoomEditIntent
    data object NavigateBack : RoomEditIntent
}

sealed interface RoomEditEffect {
    data object NavigateBack : RoomEditEffect
    data class ShowSaveSuccess(val room: Room) : RoomEditEffect
    data class ShowCreateSuccess(val room: Room) : RoomEditEffect
    data class ShowError(val message: String) : RoomEditEffect
}
