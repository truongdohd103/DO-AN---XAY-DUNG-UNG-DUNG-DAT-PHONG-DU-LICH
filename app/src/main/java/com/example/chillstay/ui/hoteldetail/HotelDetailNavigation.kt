package com.example.chillstay.ui.hoteldetail

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.NavBackStackEntry

object HotelDetailNavigation {
    const val route = "hotel_detail/{hotelId}"
    const val routeWithArg = "hotel_detail"
    
    fun createRoute(hotelId: String) = "hotel_detail/$hotelId"
}

fun NavGraphBuilder.hotelDetailRoute(
    onBackClick: () -> Unit = {},
    onChooseRoomClick: (String) -> Unit = {}
) {
    composable(
        route = HotelDetailNavigation.route,
        arguments = listOf(
            navArgument("hotelId") {
                type = NavType.StringType
            }
        )
    ) { backStackEntry: NavBackStackEntry ->
        val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
        // HotelDetailScreen sẽ được inject từ DI container
        // HotelDetailScreen(
        //     hotelId = hotelId,
        //     onBackClick = onBackClick,
        //     onChooseRoomClick = { onChooseRoomClick(hotelId) }
        // )
    }
}

fun NavHostController.navigateToHotelDetail(hotelId: String, navOptions: NavOptions? = null) {
    navigate(route = HotelDetailNavigation.createRoute(hotelId), navOptions = navOptions)
}
