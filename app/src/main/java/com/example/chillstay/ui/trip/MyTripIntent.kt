package com.example.chillstay.ui.trip

sealed interface MyTripIntent {
    data class LoadBookings(val userId: String, val status: String?) : MyTripIntent
    data class ChangeTab(val tabIndex: Int) : MyTripIntent
    data class CancelBooking(val bookingId: String) : MyTripIntent
    data class RefreshBookings(val userId: String, val status: String?) : MyTripIntent
    data class RetryLoad(val userId: String, val status: String?) : MyTripIntent
}
