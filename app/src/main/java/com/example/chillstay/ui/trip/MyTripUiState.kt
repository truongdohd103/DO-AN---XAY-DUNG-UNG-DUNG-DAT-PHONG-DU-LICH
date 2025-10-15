package com.example.chillstay.ui.trip

import androidx.compose.runtime.Immutable
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room

@Immutable
data class MyTripUiState(
    val isLoading: Boolean = true,
    val selectedTab: Int = 0,
    val bookings: List<Booking> = emptyList(),
    val roomMap: Map<String, Room> = emptyMap(),
    val hotelMap: Map<String, Hotel> = emptyMap(),
    val error: String? = null,
    val isEmpty: Boolean = false
) {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateSelectedTab(value: Int) = copy(selectedTab = value)
    fun updateBookings(value: List<Booking>) = copy(bookings = value)
    fun updateRoomMap(value: Map<String, Room>) = copy(roomMap = value)
    fun updateHotelMap(value: Map<String, Hotel>) = copy(hotelMap = value)
    fun updateError(value: String?) = copy(error = value)
    fun updateIsEmpty(value: Boolean) = copy(isEmpty = value)
    fun clearError() = copy(error = null)
}
