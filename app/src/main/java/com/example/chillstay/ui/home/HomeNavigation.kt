package com.example.chillstay.ui.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object HomeNavigation {
    const val route = "home"
}

fun NavGraphBuilder.homeRoute(
    onHotelClick: (String) -> Unit = {}
) {
    composable(route = HomeNavigation.route) {
        // HomeScreen sẽ được inject từ DI container
        // HomeScreen(onHotelClick = onHotelClick)
    }
}

fun NavHostController.navigateToHomeRoute(navOptions: NavOptions? = null) {
    navigate(route = HomeNavigation.route, navOptions = navOptions)
}
