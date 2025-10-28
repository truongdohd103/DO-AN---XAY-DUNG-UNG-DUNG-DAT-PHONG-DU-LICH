package com.example.chillstay.ui.hoteldetail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object HotelDetailNavigation {
    const val HOTEL_ID_ARG = "hotelId"
    const val FROM_MY_TRIP_ARG = "fromMyTrip"
    
    fun route(hotelId: String, fromMyTrip: Boolean = false) = 
        if (fromMyTrip) "${Routes.HOTEL_DETAIL}/$hotelId?fromMyTrip=$fromMyTrip"
        else "${Routes.HOTEL_DETAIL}/$hotelId"
}

fun NavGraphBuilder.hotelDetailRoutes(
    onBackClick: (fromMyTrip: Boolean) -> Unit,
    onChooseRoomClick: (hotelId: String) -> Unit
) {
    // Basic hotel detail route
    composable(
        route = "${Routes.HOTEL_DETAIL}/{${HotelDetailNavigation.HOTEL_ID_ARG}}",
        arguments = listOf(
            navArgument(HotelDetailNavigation.HOTEL_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(HotelDetailNavigation.HOTEL_ID_ARG) ?: ""
        val fromMyTrip = false
        
        HotelDetailScreen(
            hotelId = hotelId,
            onBackClick = { onBackClick(fromMyTrip) },
            onChooseRoomClick = { onChooseRoomClick(hotelId) }
        )
    }
    
    // Hotel detail route with fromMyTrip parameter
    composable(
        route = "${Routes.HOTEL_DETAIL}/{${HotelDetailNavigation.HOTEL_ID_ARG}}?${HotelDetailNavigation.FROM_MY_TRIP_ARG}={${HotelDetailNavigation.FROM_MY_TRIP_ARG}}",
        arguments = listOf(
            navArgument(HotelDetailNavigation.HOTEL_ID_ARG) { type = NavType.StringType },
            navArgument(HotelDetailNavigation.FROM_MY_TRIP_ARG) { 
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(HotelDetailNavigation.HOTEL_ID_ARG) ?: ""
        val fromMyTrip = backStackEntry.arguments?.getBoolean(HotelDetailNavigation.FROM_MY_TRIP_ARG) ?: false
        
        HotelDetailScreen(
            hotelId = hotelId,
            onBackClick = { onBackClick(fromMyTrip) },
            onChooseRoomClick = { onChooseRoomClick(hotelId) }
        )
    }
}

fun NavHostController.navigateToHotelDetail(
    hotelId: String,
    fromMyTrip: Boolean = false,
    navOptions: NavOptions? = null
) {
    navigate(
        route = HotelDetailNavigation.route(hotelId, fromMyTrip),
        navOptions = navOptions
    )
}
