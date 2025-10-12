package com.example.chillstay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chillstay.core.common.OnboardingManager
import com.example.chillstay.ui.auth.AuthenticationScreen
import com.example.chillstay.ui.auth.SignInScreen
import com.example.chillstay.ui.auth.SignUpScreen
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.main.MainScreen
import com.example.chillstay.ui.welcome.WelcomeScreen
import com.example.chillstay.ui.welcome.CarouselScreen
import com.example.chillstay.ui.hoteldetail.HotelDetailScreen
import com.example.chillstay.ui.room.RoomScreen
import com.example.chillstay.ui.booking.BookingScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.WELCOME,
    homeViewModel: HomeViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStartedClick = {
                    OnboardingManager.markWelcomeSeen(navController.context)
                    navController.navigate(Routes.CAROUSEL) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CAROUSEL) {
            CarouselScreen(
                onNextClick = {
                    OnboardingManager.markOnboardingDone(navController.context)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.CAROUSEL) { inclusive = true }
                    }
                },
                onSkipClick = {
                    OnboardingManager.markOnboardingDone(navController.context)
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.CAROUSEL) { inclusive = true }
                    }
                }
            )
        }
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
                        navController.navigate(Routes.MAIN) {
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
                        navController.navigate(Routes.MAIN) {
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
        composable(Routes.MAIN) {
            MainScreen(
                homeViewModel = homeViewModel,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { navController.navigate(Routes.HOTEL_DETAIL) }
            )
        }
        composable(Routes.HOTEL_DETAIL) {
            HotelDetailScreen(
                onBackClick = { navController.popBackStack() },
                onChooseRoomClick = { navController.navigate(Routes.ROOM) }
            )
        }
        composable(Routes.ROOM) {
            RoomScreen(
                onBackClick = { navController.popBackStack() },
                onBookNowClick = { navController.navigate(Routes.BOOKING) }
            )
        }
        composable(Routes.BOOKING) {
            BookingScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
