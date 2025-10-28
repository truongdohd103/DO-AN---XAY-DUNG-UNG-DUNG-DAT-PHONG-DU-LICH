package com.example.chillstay.ui.voucher

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.chillstay.ui.navigation.Routes

object VoucherNavigation {
    const val VOUCHER_ID_ARG = "voucherId"
    
    const val voucherRoute = Routes.VOUCHER
    
    fun voucherDetailRoute(voucherId: String) = "${Routes.VOUCHER_DETAIL}/$voucherId"
}

fun NavGraphBuilder.voucherRoutes(
    onBackClick: () -> Unit,
    onVoucherClick: (voucherId: String) -> Unit
) {
    // Voucher list route
    composable(route = VoucherNavigation.voucherRoute) {
        VoucherScreen(
            onBackClick = onBackClick,
            onVoucherClick = onVoucherClick
        )
    }
    
    // Voucher detail route
    composable(
        route = "${Routes.VOUCHER_DETAIL}/{${VoucherNavigation.VOUCHER_ID_ARG}}",
        arguments = listOf(
            navArgument(VoucherNavigation.VOUCHER_ID_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val voucherId = backStackEntry.arguments?.getString(VoucherNavigation.VOUCHER_ID_ARG) ?: ""
        
        VoucherDetailScreen(
            voucherId = voucherId,
            onBackClick = onBackClick
        )
    }
}

fun NavHostController.navigateToVoucher(navOptions: NavOptions? = null) {
    navigate(route = VoucherNavigation.voucherRoute, navOptions = navOptions)
}

fun NavHostController.navigateToVoucherDetail(
    voucherId: String,
    navOptions: NavOptions? = null
) {
    navigate(
        route = VoucherNavigation.voucherDetailRoute(voucherId),
        navOptions = navOptions
    )
}

fun NavHostController.navigateToVoucherDetail(
    voucherId: String,
    builder: NavOptionsBuilder.() -> Unit
) {
    navigate(
        route = VoucherNavigation.voucherDetailRoute(voucherId),
        builder = builder
    )
}
