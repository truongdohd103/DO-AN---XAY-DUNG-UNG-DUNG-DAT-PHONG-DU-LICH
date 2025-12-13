package com.example.chillstay.ui.search

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object SearchNavigation {
    fun route() = Routes.SEARCH
}

fun NavGraphBuilder.searchRoute(
    onBackClick: () -> Unit,
    onHotelClick: (String) -> Unit
) {
    composable(SearchNavigation.route()) {
        SearchScreen(
            onBackClick = onBackClick,
            onHotelClick = onHotelClick
        )
    }
}

fun NavHostController.navigateToSearch(
    navOptions: NavOptions? = null
) {
    navigate(
        route = SearchNavigation.route(),
        navOptions = navOptions
    )
}
