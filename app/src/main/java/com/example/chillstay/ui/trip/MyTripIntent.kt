package com.example.chillstay.ui.trip

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.Booking

sealed interface MyTripIntent : UiEvent {
    data class LoadBookings(val userId: String, val status: String?) : MyTripIntent
    data class ChangeTab(val tabIndex: Int) : MyTripIntent
    data class CancelBooking(val bookingId: String) : MyTripIntent
    data class RefreshBookings(val userId: String, val status: String?) : MyTripIntent
    data class RetryLoad(val userId: String, val status: String?) : MyTripIntent
    data class ShowCancelDialog(val booking: Booking) : MyTripIntent
    object HideCancelDialog : MyTripIntent
    data class LoadHotelDetails(val hotelIds: List<String>) : MyTripIntent
    data class LoadRoomDetails(val roomIds: List<String>) : MyTripIntent
}
