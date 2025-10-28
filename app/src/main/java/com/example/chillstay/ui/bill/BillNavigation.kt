package com.example.chillstay.ui.bill

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object BillNavigation {
    const val BOOKING_ID_ARG = "bookingId"
    
    fun route(bookingId: String) = "${Routes.BILL}/$bookingId"
}

fun NavGraphBuilder.billRoute(
    onBackClick: () -> Unit
) {
    composable(
        route = "${Routes.BILL}/{${BillNavigation.BOOKING_ID_ARG}}",
        arguments = listOf(
            navArgument(BillNavigation.BOOKING_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val bookingId = backStackEntry.arguments?.getString(BillNavigation.BOOKING_ID_ARG) ?: ""
        
        BillScreen(
            bookingId = bookingId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToBill(
    bookingId: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = BillNavigation.route(bookingId),
        navOptions = navOptions
    )
}
