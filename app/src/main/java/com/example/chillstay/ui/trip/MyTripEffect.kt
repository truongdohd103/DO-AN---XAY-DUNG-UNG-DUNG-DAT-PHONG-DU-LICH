package com.example.chillstay.ui.trip

import com.example.chillstay.core.base.UiEffect

sealed interface MyTripEffect : UiEffect {
    data class ShowError(val message: String) : MyTripEffect
    data class ShowSuccess(val message: String) : MyTripEffect
    data class NavigateToHotelDetail(val hotelId: String, val fromMyTrip: Boolean = true) : MyTripEffect
    data class NavigateToBookingDetail(val bookingId: String) : MyTripEffect
    data class NavigateToReview(val bookingId: String) : MyTripEffect
    data class NavigateToBill(val bookingId: String) : MyTripEffect
    object ShowBookingCancelled : MyTripEffect
    object RequireAuthentication : MyTripEffect
}
