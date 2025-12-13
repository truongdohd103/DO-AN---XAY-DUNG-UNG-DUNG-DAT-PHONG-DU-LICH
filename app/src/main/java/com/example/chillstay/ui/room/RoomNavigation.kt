package com.example.chillstay.ui.room

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object RoomNavigation {
    const val HOTEL_ID_ARG = "hotelId"
    
    fun route(hotelId: String) = "${Routes.ROOM}/$hotelId"
}

fun NavGraphBuilder.roomRoute(
    onBackClick: () -> Unit,
    onBookNowClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit,
    onOpenGalleryClick: (hotelId: String, roomId: String) -> Unit
) {
    composable(
        route = "${Routes.ROOM}/{${RoomNavigation.HOTEL_ID_ARG}}",
        arguments = listOf(
            navArgument(RoomNavigation.HOTEL_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(RoomNavigation.HOTEL_ID_ARG) ?: ""
        
        RoomScreen(
            hotelId = hotelId,
            onBackClick = onBackClick,
            onBookNowClick = onBookNowClick,
            onOpenGalleryClick = { roomId -> onOpenGalleryClick(hotelId, roomId) }
        )
    }
}

fun NavHostController.navigateToRoom(
    hotelId: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = RoomNavigation.route(hotelId),
        navOptions = navOptions
    )
}
