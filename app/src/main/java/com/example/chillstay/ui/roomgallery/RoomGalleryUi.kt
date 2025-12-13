package com.example.chillstay.ui.roomgallery

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.core.base.UiState

enum class RoomGalleryCategory { ExteriorView, Facilities, Dining, ThisRoom }

@Immutable
data class RoomGalleryUiState(
    val isLoading: Boolean = true,
    val hotelName: String? = null,
    val roomName: String? = null,
    val exteriorView: List<String> = emptyList(),
    val facilities: List<String> = emptyList(),
    val dining: List<String> = emptyList(),
    val thisRoom: List<String> = emptyList(),
    val selectedCategory: RoomGalleryCategory = RoomGalleryCategory.ExteriorView,
    val error: String? = null
) : UiState {
    val currentImages: List<String>
        get() = when (selectedCategory) {
            RoomGalleryCategory.ExteriorView -> exteriorView
            RoomGalleryCategory.Facilities -> facilities
            RoomGalleryCategory.Dining -> dining
            RoomGalleryCategory.ThisRoom -> thisRoom
        }
    val totalCount: Int
        get() = exteriorView.size + facilities.size + dining.size + thisRoom.size

    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateHotelName(value: String?) = copy(hotelName = value)
    fun updateRoomName(value: String?) = copy(roomName = value)
    fun updateExteriorView(value: List<String>) = copy(exteriorView = value)
    fun updateFacilities(value: List<String>) = copy(facilities = value)
    fun updateDining(value: List<String>) = copy(dining = value)
    fun updateThisRoom(value: List<String>) = copy(thisRoom = value)
    fun updateSelectedCategory(value: RoomGalleryCategory) = copy(selectedCategory = value)
    fun updateError(value: String?) = copy(error = value)
    fun clearError() = copy(error = null)
}

sealed interface RoomGalleryIntent : UiEvent {
    data class LoadGallery(val hotelId: String, val roomId: String) : RoomGalleryIntent
    data class SelectCategory(val category: RoomGalleryCategory) : RoomGalleryIntent
}

sealed interface RoomGalleryEffect : UiEffect {
    data class ShowError(val message: String) : RoomGalleryEffect
}
