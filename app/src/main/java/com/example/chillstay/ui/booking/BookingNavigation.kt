package com.example.chillstay.ui.booking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object BookingNavigation {
    const val HOTEL_ID_ARG = "hotelId"
    const val ROOM_ID_ARG = "roomId"
    const val DATE_FROM_ARG = "dateFrom"
    const val DATE_TO_ARG = "dateTo"
    const val BOOKING_ID_ARG = "bookingId"
    
    fun newBookingRoute(hotelId: String, roomId: String, dateFrom: String, dateTo: String) = 
        "${Routes.BOOKING}/$hotelId/$roomId/$dateFrom/$dateTo"
    
    fun bookingDetailRoute(bookingId: String) = 
        "${Routes.BOOKING_DETAIL}/$bookingId"
}

fun NavGraphBuilder.bookingRoutes(
    onBackClick: () -> Unit
) {
    // New booking route
    composable(
        route = "${Routes.BOOKING}/{${BookingNavigation.HOTEL_ID_ARG}}/{${BookingNavigation.ROOM_ID_ARG}}/{${BookingNavigation.DATE_FROM_ARG}}/{${BookingNavigation.DATE_TO_ARG}}",
        arguments = listOf(
            navArgument(BookingNavigation.HOTEL_ID_ARG) { type = NavType.StringType },
            navArgument(BookingNavigation.ROOM_ID_ARG) { type = NavType.StringType },
            navArgument(BookingNavigation.DATE_FROM_ARG) { type = NavType.StringType },
            navArgument(BookingNavigation.DATE_TO_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(BookingNavigation.HOTEL_ID_ARG) ?: ""
        val roomId = backStackEntry.arguments?.getString(BookingNavigation.ROOM_ID_ARG) ?: ""
        val dateFrom = backStackEntry.arguments?.getString(BookingNavigation.DATE_FROM_ARG) ?: ""
        val dateTo = backStackEntry.arguments?.getString(BookingNavigation.DATE_TO_ARG) ?: ""
        
        BookingScreen(
            hotelId = hotelId,
            roomId = roomId,
            dateFrom = dateFrom,
            dateTo = dateTo,
            onBackClick = onBackClick
        )
    }
    
    // Booking detail route
    composable(
        route = "${Routes.BOOKING_DETAIL}/{${BookingNavigation.BOOKING_ID_ARG}}",
        arguments = listOf(
            navArgument(BookingNavigation.BOOKING_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val bookingId = backStackEntry.arguments?.getString(BookingNavigation.BOOKING_ID_ARG) ?: ""
        
        BookingScreen(
            bookingId = bookingId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToNewBooking(
    hotelId: String, 
    roomId: String, 
    dateFrom: String, 
    dateTo: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = BookingNavigation.newBookingRoute(hotelId, roomId, dateFrom, dateTo),
        navOptions = navOptions
    )
}

fun NavHostController.navigateToBookingDetail(
    bookingId: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = BookingNavigation.bookingDetailRoute(bookingId),
        navOptions = navOptions
    )
}
