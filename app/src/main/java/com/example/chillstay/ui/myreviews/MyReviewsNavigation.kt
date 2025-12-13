package com.example.chillstay.ui.myreviews

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object MyReviewsNavigation {
    const val route = Routes.MY_REVIEWS
}

fun NavGraphBuilder.myReviewsRoute(
    onBackClick: () -> Unit,
    onHotelClick: (String) -> Unit
) {
    composable(route = MyReviewsNavigation.route) {
        MyReviewsScreen(
            onBackClick = onBackClick,
            onHotelClick = onHotelClick
        )
    }
}

fun NavHostController.navigateToMyReviews(navOptions: NavOptions? = null) {
    navigate(route = MyReviewsNavigation.route, navOptions = navOptions)
}
