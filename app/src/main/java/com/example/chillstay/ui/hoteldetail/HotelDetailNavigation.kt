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
    const val SOURCE_TAB_ARG = "sourceTab"
    
    fun route(hotelId: String, fromMyTrip: Boolean = false, sourceTab: Int = -1) =
        "${Routes.HOTEL_DETAIL}/$hotelId?${FROM_MY_TRIP_ARG}=$fromMyTrip&${SOURCE_TAB_ARG}=$sourceTab"
}

fun NavGraphBuilder.hotelDetailRoutes(
    onBackClick: (fromMyTrip: Boolean, sourceTab: Int) -> Unit,
    onChooseRoomClick: (hotelId: String) -> Unit,
    onSeeAllReviewsClick: (hotelId: String) -> Unit
) {
    composable(
        route = "${Routes.HOTEL_DETAIL}/{${HotelDetailNavigation.HOTEL_ID_ARG}}?${HotelDetailNavigation.FROM_MY_TRIP_ARG}={${HotelDetailNavigation.FROM_MY_TRIP_ARG}}&${HotelDetailNavigation.SOURCE_TAB_ARG}={${HotelDetailNavigation.SOURCE_TAB_ARG}}",
        arguments = listOf(
            navArgument(HotelDetailNavigation.HOTEL_ID_ARG) { type = NavType.StringType },
            navArgument(HotelDetailNavigation.FROM_MY_TRIP_ARG) { 
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument(HotelDetailNavigation.SOURCE_TAB_ARG) {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(HotelDetailNavigation.HOTEL_ID_ARG) ?: ""
        val fromMyTrip = backStackEntry.arguments?.getBoolean(HotelDetailNavigation.FROM_MY_TRIP_ARG) ?: false
        val sourceTab = backStackEntry.arguments?.getInt(HotelDetailNavigation.SOURCE_TAB_ARG) ?: -1
        
        HotelDetailScreen(
            hotelId = hotelId,
            onBackClick = { onBackClick(fromMyTrip, sourceTab) },
            onChooseRoomClick = { onChooseRoomClick(hotelId) },
            onSeeAllReviewsClick = { onSeeAllReviewsClick(hotelId) }
        )
    }
}

fun NavHostController.navigateToHotelDetail(
    hotelId: String,
    fromMyTrip: Boolean = false,
    sourceTab: Int = -1,
    navOptions: NavOptions? = null
) {
    navigate(
        route = HotelDetailNavigation.route(hotelId, fromMyTrip, sourceTab),
        navOptions = navOptions
    )
}
