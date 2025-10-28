package com.example.chillstay.ui.bookmark

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object MyBookmarkNavigation {
    const val route = Routes.BOOKMARK
}

fun NavGraphBuilder.bookmarkRoute(
    onBackClick: () -> Unit,
    onHotelClick: (hotelId: String) -> Unit
) {
    composable(route = MyBookmarkNavigation.route) {
        MyBookmarkScreen(
            onBackClick = onBackClick,
            onHotelClick = onHotelClick
        )
    }
}

fun NavHostController.navigateToBookmark(navOptions: NavOptions? = null) {
    navigate(route = MyBookmarkNavigation.route, navOptions = navOptions)
}
