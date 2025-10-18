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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

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
                    if (email.isNotBlank() && password.isNotBlank()) {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                // Optionally show error via snackbar/state
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
                    if (email.isNotBlank() && password.isNotBlank()) {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener {
                                navController.navigate(Routes.MAIN) {
                                    popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                                }
                            }
                            .addOnFailureListener { }
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
                    if (email.isNotBlank() && password.isNotBlank() && password == confirmPassword) {
                        val auth = FirebaseAuth.getInstance()
                        val db = FirebaseFirestore.getInstance()
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                    val userDoc = mapOf(
                                        "email" to email,
                                        "password" to password,
                                        "fullName" to "",
                                        "gender" to "",
                                        "photoUrl" to "",
                                        "dateOfBirth" to LocalDate.now().toString()
                                    )
                                    db.collection("users").document(uid).set(userDoc)
                                        .addOnSuccessListener {
                                            navController.navigate("${Routes.SIGN_IN}?message=Account created successfully!") {
                                                popUpTo(Routes.SIGN_UP) { inclusive = true }
                                            }
                                        }
                                }
                            }
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
                onHotelClick = { hotelId -> navController.navigate("${Routes.HOTEL_DETAIL}/$hotelId") },
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) }
            )
        }
        composable("${Routes.HOTEL_DETAIL}/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            HotelDetailScreen(
                hotelId = hotelId,
                onBackClick = { navController.popBackStack() },
                onChooseRoomClick = { navController.navigate("${Routes.ROOM}/$hotelId") }
            )
        }
        composable("${Routes.ROOM}/{hotelId}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            RoomScreen(
                hotelId = hotelId,
                onBackClick = { navController.popBackStack() },
                onBookNowClick = { hotelId, roomId, dateFrom, dateTo ->
                    val isSignedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
                    if (isSignedIn) {
                        navController.navigate("${Routes.BOOKING}/$hotelId/$roomId/$dateFrom/$dateTo")
                    } else {
                        navController.navigate(Routes.AUTHENTICATION)
                    }
                }
            )
        }
        composable("${Routes.BOOKING}/{hotelId}/{roomId}/{dateFrom}/{dateTo}") { backStackEntry ->
            val hotelId = backStackEntry.arguments?.getString("hotelId") ?: ""
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val dateFrom = backStackEntry.arguments?.getString("dateFrom") ?: ""
            val dateTo = backStackEntry.arguments?.getString("dateTo") ?: ""
            BookingScreen(
                hotelId = hotelId,
                roomId = roomId,
                dateFrom = dateFrom,
                dateTo = dateTo,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
