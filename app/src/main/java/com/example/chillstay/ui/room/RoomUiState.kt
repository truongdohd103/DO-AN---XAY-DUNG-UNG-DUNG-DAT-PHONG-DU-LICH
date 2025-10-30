package com.example.chillstay.ui.room

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Room

@Immutable
data class RoomUiState(
    val isLoading: Boolean = true,
    val rooms: List<Room> = emptyList(),
    val hotelName: String? = null,
    val error: String? = null
) : UiState {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateRooms(value: List<Room>) = copy(rooms = value)
    fun updateHotelName(value: String?) = copy(hotelName = value)
    fun updateError(value: String?) = copy(error = value)
    fun clearError() = copy(error = null)
}


