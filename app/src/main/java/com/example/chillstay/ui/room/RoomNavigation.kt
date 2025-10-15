package com.example.chillstay.ui.room

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

object RoomNavigation {
    const val route = "room/{hotelId}"
    const val routeWithArg = "room"
    fun createRoute(hotelId: String) = "room/$hotelId"
}

fun NavGraphBuilder.roomRoute(
    onBackClick: () -> Unit = {},
    onBookNowClick: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    composable(
        route = RoomNavigation.route,
        arguments = listOf(navArgument("hotelId") { type = NavType.StringType })
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString("hotelId").orEmpty()
        RoomScreen(
            hotelId = hotelId,
            onBackClick = onBackClick,
            onBookNowClick = onBookNowClick
        )
    }
}

fun NavHostController.navigateToRoom(hotelId: String, navOptions: NavOptions? = null) {
    navigate(RoomNavigation.createRoute(hotelId), navOptions)
}


