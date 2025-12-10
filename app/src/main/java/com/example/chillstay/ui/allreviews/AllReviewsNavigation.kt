package com.example.chillstay.ui.allreviews

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object AllReviewsNavigation {
    const val HOTEL_ID_ARG = "hotelId"
    fun route(hotelId: String) = "${Routes.ALL_REVIEWS}/$hotelId"
}

fun NavGraphBuilder.allReviewsRoute(
    onBackClick: () -> Unit
) {
    composable(
        route = "${Routes.ALL_REVIEWS}/{${AllReviewsNavigation.HOTEL_ID_ARG}}",
        arguments = listOf(
            navArgument(AllReviewsNavigation.HOTEL_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val hotelId = backStackEntry.arguments?.getString(AllReviewsNavigation.HOTEL_ID_ARG) ?: ""
        AllReviewsScreen(
            hotelId = hotelId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToAllReviews(
    hotelId: String,
    navOptions: NavOptions? = null
) {
    navigate(route = AllReviewsNavigation.route(hotelId), navOptions = navOptions)
}
