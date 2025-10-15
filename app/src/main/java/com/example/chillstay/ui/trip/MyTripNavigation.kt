package com.example.chillstay.ui.trip

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object MyTripNavigation {
    const val route = "my_trip"
}

fun NavGraphBuilder.myTripRoute(
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {}
) {
    composable(route = MyTripNavigation.route) {
        // MyTripScreen sẽ được inject từ DI container
        // MyTripScreen(
        //     onBackClick = onBackClick,
        //     onHotelClick = onHotelClick
        // )
    }
}

fun NavHostController.navigateToMyTrip(navOptions: NavOptions? = null) {
    navigate(route = MyTripNavigation.route, navOptions = navOptions)
}
