package com.example.chillstay.ui.trip

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room

@Immutable
data class MyTripUiState(
    val bookings: List<Booking> = emptyList(),
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hotelMap: Map<String, Hotel> = emptyMap(),
    val roomMap: Map<String, Room> = emptyMap(),
    val showCancelDialog: Boolean = false,
    val bookingToCancel: Booking? = null
) : UiState {
    fun updateBookings(bookings: List<Booking>) = copy(bookings = bookings)
    
    fun updateSelectedTab(tabIndex: Int) = copy(selectedTab = tabIndex)
    
    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)
    
    fun updateError(error: String?) = copy(error = error)
    
    fun clearError() = copy(error = null)
    
    fun updateHotelMap(hotelMap: Map<String, Hotel>) = copy(hotelMap = hotelMap)
    
    fun updateRoomMap(roomMap: Map<String, Room>) = copy(roomMap = roomMap)
    
    fun updateShowCancelDialog(show: Boolean) = copy(showCancelDialog = show)
    
    fun updateBookingToCancel(booking: Booking?) = copy(bookingToCancel = booking)
    
    fun updateHotelMapEntry(hotelId: String, hotel: Hotel) = copy(
        hotelMap = hotelMap + (hotelId to hotel)
    )
    
    fun updateRoomMapEntry(roomId: String, room: Room) = copy(
        roomMap = roomMap + (roomId to room)
    )
}