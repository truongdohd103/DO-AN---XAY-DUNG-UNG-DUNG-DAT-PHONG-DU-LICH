package com.example.chillstay.ui.vip

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.example.chillstay.ui.navigation.Routes

object VipStatusNavigation {
    const val vipStatusRoute = Routes.VIP_STATUS
}

fun NavGraphBuilder.vipStatusRoutes(
    onBackClick: () -> Unit
) {
    composable(route = VipStatusNavigation.vipStatusRoute) {
        VipStatusScreen(
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToVipStatus(navOptions: NavOptions? = null) {
    navigate(route = VipStatusNavigation.vipStatusRoute, navOptions = navOptions)
}

