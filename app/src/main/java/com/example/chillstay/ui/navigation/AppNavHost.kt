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
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import com.example.chillstay.R
import androidx.compose.ui.res.painterResource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
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
                            .addOnFailureListener { e ->
                                navController.navigate("${Routes.SIGN_IN}?message=${java.net.URLEncoder.encode(e.message ?: "Sign in failed", "UTF-8")}")
                            }
                    }
                },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ },
                errorMessage = null
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
                            .addOnFailureListener { e ->
                                navController.navigate("${Routes.SIGN_IN}?message=${java.net.URLEncoder.encode(e.message ?: "Sign in failed", "UTF-8")}")
                            }
                    }
                },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ },
                successMessage = null,
                errorMessage = message
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
                                } else {
                                    val err = task.exception?.message ?: "Sign up failed"
                                    navController.navigate("${Routes.SIGN_UP}")
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
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) },
                onVipClick = {
                    val isSignedIn = FirebaseAuth.getInstance().currentUser != null
                    if (isSignedIn) navController.navigate(Routes.VIP_STATUS) else navController.navigate(Routes.AUTHENTICATION)
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                onContinueItemClick = { hotelId, roomId, dateFrom, dateTo ->
                    val isSignedIn = FirebaseAuth.getInstance().currentUser != null
                    if (isSignedIn) {
                        navController.navigate("${Routes.BOOKING}/$hotelId/$roomId/$dateFrom/$dateTo")
                    } else {
                        navController.navigate(Routes.AUTHENTICATION)
                    }
                }
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
        composable(Routes.VIP_STATUS) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "VIP Status", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1AB6B6))
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) { }
            }
        }
        composable(Routes.SEARCH) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = "Search", color = Color.White) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_arrow_back),
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1AB6B6))
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) { }
            }
        }
    }
}
