package com.example.chillstay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chillstay.ui.home.HomeScreen
import com.example.chillstay.ui.home.HomeViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.HOME,
    homeViewModel: HomeViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.HOME) { HomeScreen(viewModel = homeViewModel) }
    }
}
