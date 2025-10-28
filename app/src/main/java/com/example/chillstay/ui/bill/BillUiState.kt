package com.example.chillstay.ui.bill

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel

data class BillUiState(
    val bookingId: String = "",
    val booking: Booking? = null,
    val hotel: Hotel? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDownloading: Boolean = false,
    val isSharing: Boolean = false
) {
    fun updateBookingId(bookingId: String) = copy(bookingId = bookingId)
    
    fun updateBooking(booking: Booking?) = copy(booking = booking)
    
    fun updateHotel(hotel: Hotel?) = copy(hotel = hotel)
    
    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)
    
    fun updateError(error: String?) = copy(error = error)
    
    fun clearError() = copy(error = null)
    
    fun updateIsDownloading(isDownloading: Boolean) = copy(isDownloading = isDownloading)
    
    fun updateIsSharing(isSharing: Boolean) = copy(isSharing = isSharing)
}
