package com.example.chillstay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chillstay.ui.auth.AuthenticationScreen
import com.example.chillstay.ui.auth.SignInScreen
import com.example.chillstay.ui.auth.SignUpScreen
import com.example.chillstay.ui.home.HomeScreen
import com.example.chillstay.ui.home.HomeViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.AUTHENTICATION,
    homeViewModel: HomeViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.AUTHENTICATION) {
            AuthenticationScreen(
                onSignInClick = { navController.navigate(Routes.SIGN_IN) },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ }
            )
        }
        composable(Routes.SIGN_IN) {
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { email, password -> 
                    // Simple demo authentication
                    if (email.isNotBlank() && password.isNotBlank()) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                        }
                    }
                },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ }
            )
        }
        composable("${Routes.SIGN_IN}?message={message}") { backStackEntry ->
            val message = backStackEntry.arguments?.getString("message")
            SignInScreen(
                onBackClick = { navController.popBackStack() },
                onSignInClick = { email, password -> 
                    // Simple demo authentication
                    if (email.isNotBlank() && password.isNotBlank()) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                        }
                    }
                },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ },
                successMessage = message
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { email, password, confirmPassword -> 
                    // Navigate to sign in with success message
                    navController.navigate("${Routes.SIGN_IN}?message=Account created successfully!") {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                },
                onSignInClick = { navController.navigate(Routes.SIGN_IN) },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ }
            )
        }
        composable(Routes.HOME) { 
            HomeScreen(viewModel = homeViewModel) 
        }
    }
}
