package com.example.chillstay.ui.bookmark

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

object MyBookmarkNavigation {
    const val route = "my_bookmark"
}

fun NavGraphBuilder.myBookmarkRoute(
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {}
) {
    composable(route = MyBookmarkNavigation.route) {
        // MyBookmarkScreen sẽ được inject từ DI container
        // MyBookmarkScreen(
        //     onBackClick = onBackClick,
        //     onHotelClick = onHotelClick
        // )
    }
}

fun NavHostController.navigateToMyBookmark(navOptions: NavOptions? = null) {
    navigate(route = MyBookmarkNavigation.route, navOptions = navOptions)
}
