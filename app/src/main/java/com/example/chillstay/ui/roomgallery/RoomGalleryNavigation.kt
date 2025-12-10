package com.example.chillstay.ui.roomgallery

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object RoomGalleryNavigation {
    const val HOTEL_ID_ARG = "hotelId"
    const val ROOM_ID_ARG = "roomId"
    fun route(hotelId: String, roomId: String) = "${Routes.ROOM_GALLERY}/$hotelId/$roomId"
}

fun NavGraphBuilder.roomGalleryRoute(
    onBackClick: () -> Unit
) {
    composable(
        route = "${Routes.ROOM_GALLERY}/{${RoomGalleryNavigation.HOTEL_ID_ARG}}/{${RoomGalleryNavigation.ROOM_ID_ARG}}",
        arguments = listOf(
            navArgument(RoomGalleryNavigation.HOTEL_ID_ARG) { type = NavType.StringType },
            navArgument(RoomGalleryNavigation.ROOM_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(RoomGalleryNavigation.HOTEL_ID_ARG) ?: ""
        val roomId = backStackEntry.arguments?.getString(RoomGalleryNavigation.ROOM_ID_ARG) ?: ""
        RoomGalleryScreen(
            hotelId = hotelId,
            roomId = roomId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToRoomGallery(
    hotelId: String,
    roomId: String,
    navOptions: NavOptions? = null
) {
    navigate(route = RoomGalleryNavigation.route(hotelId, roomId), navOptions = navOptions)
}
