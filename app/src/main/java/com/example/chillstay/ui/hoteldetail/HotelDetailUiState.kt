package com.example.chillstay.ui.hoteldetail

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room

@Immutable
data class HotelDetailUiState(
    val isLoading: Boolean = true,
    val hotel: Hotel? = null,
    val rooms: List<Room> = emptyList(),
    val minPrice: Int? = null,
    val isBookmarked: Boolean = false,
    val error: String? = null
) : UiState {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateHotel(value: Hotel?) = copy(hotel = value)
    fun updateRooms(value: List<Room>) = copy(rooms = value)
    fun updateMinPrice(value: Int?) = copy(minPrice = value)
    fun updateIsBookmarked(value: Boolean) = copy(isBookmarked = value)
    fun updateError(value: String?) = copy(error = value)
    fun clearError() = copy(error = null)
}
