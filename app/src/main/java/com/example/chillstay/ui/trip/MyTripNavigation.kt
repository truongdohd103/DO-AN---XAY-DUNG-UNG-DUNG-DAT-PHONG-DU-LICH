package com.example.chillstay.ui.trip

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object MyTripNavigation {
    const val route = Routes.MY_TRIPS
}

fun NavGraphBuilder.tripRoute(
    onHotelClick: (hotelId: String, fromMyTrip: Boolean) -> Unit,
    onBookingClick: (bookingId: String) -> Unit,
    onWriteReview: (bookingId: String) -> Unit,
    onViewBill: (bookingId: String) -> Unit,
    onCancelBooking: (bookingId: String) -> Unit
) {
    composable(route = MyTripNavigation.route) {
        MyTripScreen(
            onHotelClick = onHotelClick,
            onBookingClick = onBookingClick,
            onWriteReview = onWriteReview,
            onViewBill = onViewBill,
            onCancelBooking = onCancelBooking
        )
    }
}

fun NavHostController.navigateToTrip(navOptions: NavOptions? = null) {
    navigate(route = MyTripNavigation.route, navOptions = navOptions)
}
