package com.example.chillstay.ui.review

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object ReviewNavigation {
    const val BOOKING_ID_ARG = "bookingId"
    
    fun route(bookingId: String) = "${Routes.REVIEW}/$bookingId"
}

fun NavGraphBuilder.reviewRoute(
    onBackClick: () -> Unit
) {
    composable(
        route = "${Routes.REVIEW}/{${ReviewNavigation.BOOKING_ID_ARG}}",
        arguments = listOf(
            navArgument(ReviewNavigation.BOOKING_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val bookingId = backStackEntry.arguments?.getString(ReviewNavigation.BOOKING_ID_ARG) ?: ""
        
        ReviewScreen(
            bookingId = bookingId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToReview(
    bookingId: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = ReviewNavigation.route(bookingId),
        navOptions = navOptions
    )
}
