package com.example.chillstay.ui.admin.accommodation.room_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Room

@Immutable
data class RoomManageUiState(
    val hotelId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val rooms: List<Room> = emptyList()
) : UiState {
    fun updateHotelId(hotelId: String?) = copy(hotelId = hotelId)
    
    fun updateRooms(rooms: List<Room>) = copy(rooms = rooms)
    
    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)
    
    fun clearError() = copy(error = null)
    
    fun updateError(error: String?) = copy(error = error)
}

