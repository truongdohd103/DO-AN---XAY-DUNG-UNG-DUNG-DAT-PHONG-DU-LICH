package com.example.chillstay.ui.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object HomeNavigation {
    const val route = Routes.HOME
}

fun NavGraphBuilder.homeRoute(
    homeViewModel: HomeViewModel,
    onHotelClick: (hotelId: String, fromMyTrip: Boolean) -> Unit,
    onRequireAuth: () -> Unit,
    onSearchClick: () -> Unit,
    onVoucherClick: (voucherId: String) -> Unit,
    onContinueItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit
) {
    composable(route = HomeNavigation.route) {
        // HomeScreen is typically integrated into MainScreen
        // This route can be used for standalone Home navigation if needed
    }
}

fun NavHostController.navigateToHomeRoute(navOptions: NavOptions? = null) {
    navigate(route = HomeNavigation.route, navOptions = navOptions)
}
