package com.example.chillstay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chillstay.core.common.OnboardingManager
import kotlinx.coroutines.launch
import com.example.chillstay.ui.auth.AuthenticationScreen
import com.example.chillstay.ui.auth.SignInScreen
import com.example.chillstay.ui.auth.SignUpScreen
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.main.MainScreen
import com.example.chillstay.ui.welcome.WelcomeScreen
import com.example.chillstay.ui.welcome.CarouselScreen
import com.example.chillstay.ui.profile.ProfileScreen
import com.example.chillstay.ui.vip.VipStatusScreen
import com.example.chillstay.ui.search.SearchScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import java.time.LocalDate

// Navigation imports
import com.example.chillstay.ui.hoteldetail.hotelDetailRoutes
import com.example.chillstay.ui.hoteldetail.navigateToHotelDetail
import com.example.chillstay.ui.room.roomRoute
import com.example.chillstay.ui.room.navigateToRoom
import com.example.chillstay.ui.booking.bookingRoutes
import com.example.chillstay.ui.booking.navigateToNewBooking
import com.example.chillstay.ui.booking.navigateToBookingDetail
import com.example.chillstay.ui.bookmark.bookmarkRoute
import com.example.chillstay.ui.trip.tripRoute
import com.example.chillstay.ui.voucher.voucherRoutes
import com.example.chillstay.ui.voucher.navigateToVoucherDetail
import com.example.chillstay.ui.review.reviewRoute
import com.example.chillstay.ui.review.navigateToReview
import com.example.chillstay.ui.bill.billRoute
import com.example.chillstay.ui.bill.navigateToBill
import android.util.Log

private const val TAG = "SignUpCheck"

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.WELCOME,
    homeViewModel: HomeViewModel
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.WELCOME) {
            val coroutineScope = rememberCoroutineScope()
            WelcomeScreen(
                onGetStartedClick = {
                    // Use coroutine scope to call suspend function async
                    coroutineScope.launch {
                        OnboardingManager.markWelcomeSeen(navController.context)
                    }
                    navController.navigate(Routes.CAROUSEL) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CAROUSEL) {
            val coroutineScope = rememberCoroutineScope()
            CarouselScreen(
                onSkipClick = {
                    // Use coroutine scope to call suspend function async
                    coroutineScope.launch {
                        OnboardingManager.markOnboardingDone(navController.context)
                    }
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.CAROUSEL) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.AUTHENTICATION) {
            AuthenticationScreen(
                onBackClick = { navController.navigate(Routes.MAIN) },
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
                    Log.d(TAG, "=== SIGN UP CLICKED ===")
                    Log.d(TAG, "Email: $email, Password length: ${password.length}, Confirm length: ${confirmPassword.length}")
                    
                    try {
                        if (email.isNotBlank() && password.isNotBlank() && password == confirmPassword) {
                            Log.d(TAG, "Validation passed. Initializing Firebase...")
                            
                            val auth = FirebaseAuth.getInstance()
                            val db = FirebaseFirestore.getInstance()

                            Log.d(TAG, "FirebaseAuth instance: ${if (auth != null) "OK" else "NULL"}")
                            Log.d(TAG, "FirebaseFirestore instance: ${if (db != null) "OK" else "NULL"}")
                            Log.d(TAG, "Current user before signup: ${auth.currentUser?.uid ?: "null"}")
                            
                            Log.d(TAG, "Calling FirebaseAuth.createUserWithEmailAndPassword...")
                            val task = auth.createUserWithEmailAndPassword(email, password)
                            
                            Log.d(TAG, "Task created, adding listeners...")
                            
                            task.addOnCompleteListener { completedTask ->
                                Log.d(TAG, "=== addOnCompleteListener CALLED ===")
                                Log.d(TAG, "Task isComplete: ${completedTask.isComplete}, isSuccessful: ${completedTask.isSuccessful}, isCanceled: ${completedTask.isCanceled}")
                                
                                if (completedTask.isSuccessful) {
                                    Log.d(TAG, "✅ FirebaseAuth.createUserWithEmailAndPassword SUCCEEDED")
                                    val uid = auth.currentUser?.uid
                                    Log.d(TAG, "User UID: ${uid ?: "NULL"}")
                                    
                                    if (uid == null) {
                                        Log.e(TAG, "❌ UID is null after successful signup!")
                                        return@addOnCompleteListener
                                    }
                                    
                                    val userDoc = mapOf(
                                        "e-mail" to email,  // Note: using "e-mail" to match FirestoreUserRepository
                                        "email" to email,
                                        "password" to password,
                                        "fullName" to "",
                                        "gender" to "",
                                        "photoUrl" to "",
                                        "dateOfBirth" to LocalDate.now().toString()
                                    )
                                    
                                    Log.d(TAG, "Creating Firestore document for uid=$uid...")
                                    db.collection("users").document(uid).set(userDoc)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "✅ Firestore profile created successfully for uid=$uid")
                                            try {
                                                navController.navigate("${Routes.SIGN_IN}?message=Account created successfully!") {
                                                    popUpTo(Routes.SIGN_UP) { inclusive = true }
                                                }
                                                Log.d(TAG, "✅ Navigation to SIGN_IN completed")
                                            } catch (e: Exception) {
                                                Log.e(TAG, "❌ Navigation error: ${e.message}", e)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "❌ Failed to create Firestore profile for uid=$uid: ${e.message}", e)
                                        }
                                } else {
                                    val exception = completedTask.exception
                                    val err = exception?.message ?: "Sign up failed"
                                    Log.e(TAG, "❌ FirebaseAuth.createUserWithEmailAndPassword FAILED: $err", exception)
                                    Log.e(TAG, "Exception type: ${exception?.javaClass?.simpleName ?: "null"}")
                                    
                                    try {
                                        navController.navigate("${Routes.SIGN_UP}?error=${java.net.URLEncoder.encode(err, "UTF-8")}")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Navigation error after signup failure", e)
                                    }
                                }
                            }
                            
                            task.addOnFailureListener { e ->
                                Log.e(TAG, "❌ addOnFailureListener CALLED: ${e.message}", e)
                                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                            }
                            
                            task.addOnSuccessListener {
                                Log.d(TAG, "✅ addOnSuccessListener CALLED")
                            }
                            
                            Log.d(TAG, "All listeners added, waiting for Firebase response...")
                        } else {
                            Log.w(TAG, "❌ Validation failed. emailBlank=${email.isBlank()}, passwordBlank=${password.isBlank()}, passwordsMatch=${password == confirmPassword}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ EXCEPTION in onSignUpClick: ${e.message}", e)
                        e.printStackTrace()
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
                initialTab = 0,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { hotelId, fromMyTrip -> 
                    Log.d("AppNavHost", "onHotelClick called with hotelId=$hotelId, fromMyTrip=$fromMyTrip")
                    navController.navigateToHotelDetail(hotelId, fromMyTrip)
                },
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) },
                onLogout = { 
                    navController.navigate(Routes.AUTHENTICATION) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
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
                        navController.navigateToNewBooking(hotelId, roomId, dateFrom, dateTo)
                    } else {
                        navController.navigate(Routes.AUTHENTICATION)
                    }
                },
                onVoucherClick = { voucherId ->
                    // Navigate directly to VOUCHER_DETAIL with proper back stack
                    navController.navigateToVoucherDetail(voucherId) {
                        // Ensure we can go back to MAIN and avoid duplicate stack
                        popUpTo(Routes.MAIN) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToReview = { bookingId ->
                    navController.navigateToReview(bookingId)
                },
                onNavigateToBill = { bookingId ->
                    navController.navigateToBill(bookingId)
                },
                onNavigateToBooking = { bookingId ->
                    navController.navigateToBookingDetail(bookingId)
                }
            )
        }
        composable("${Routes.MAIN}?tab={tab}") { backStackEntry ->
            val tabParam = backStackEntry.arguments?.getString("tab")?.toIntOrNull() ?: 0
            android.util.Log.d("AppNavHost", "MainScreen with tab parameter: $tabParam")
            MainScreen(
                homeViewModel = homeViewModel,
                initialTab = tabParam,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { hotelId, fromMyTrip -> 
                    android.util.Log.d("AppNavHost", "onHotelClick called with hotelId=$hotelId, fromMyTrip=$fromMyTrip")
                    navController.navigateToHotelDetail(hotelId, fromMyTrip)
                },
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) },
                onLogout = { 
                    navController.navigate(Routes.AUTHENTICATION) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
                onVipClick = {
                    val isSignedIn = FirebaseAuth.getInstance().currentUser != null
                    if (isSignedIn) navController.navigate(Routes.VIP_STATUS) else navController.navigate(Routes.AUTHENTICATION)
                },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onContinueItemClick = { hotelId, roomId, dateFrom, dateTo ->
                    navController.navigateToNewBooking(hotelId, roomId, dateFrom, dateTo)
                },
                onVoucherClick = { voucherId ->
                    navController.navigateToVoucherDetail(voucherId) {
                        popUpTo(Routes.MAIN) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToReview = { bookingId ->
                    navController.navigateToReview(bookingId)
                },
                onNavigateToBill = { bookingId ->
                    navController.navigateToBill(bookingId)
                },
                onNavigateToBooking = { bookingId ->
                    navController.navigateToBookingDetail(bookingId)
                }
            )
        }
        // Hotel Detail Routes
        hotelDetailRoutes(
            onBackClick = { fromMyTrip ->
                if (fromMyTrip) {
                    // Navigate to MAIN with tab=3 (My Trip tab) to restore the correct tab
                    android.util.Log.d("AppNavHost", "Back from HotelDetail with fromMyTrip=true, navigating to MAIN with tab=3")
                    navController.navigate("${Routes.MAIN}?tab=3") {
                        // Pop everything up to and including MAIN to replace it with MAIN?tab=3
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                } else {
                    android.util.Log.d("AppNavHost", "Back from HotelDetail with fromMyTrip=false, using popBackStack")
                    navController.popBackStack()
                }
            },
            onChooseRoomClick = { hotelId ->
                navController.navigateToRoom(hotelId)
            }
        )
        // Room Route
        roomRoute(
            onBackClick = { navController.popBackStack() },
            onBookNowClick = { hotelId, roomId, dateFrom, dateTo ->
                val isSignedIn = FirebaseAuth.getInstance().currentUser != null
                if (isSignedIn) {
                    navController.navigateToNewBooking(hotelId, roomId, dateFrom, dateTo)
                } else {
                    navController.navigate(Routes.AUTHENTICATION)
                }
            }
        )
        // Booking Routes
        bookingRoutes(
            onBackClick = { navController.popBackStack() }
        )
        composable(Routes.VIP_STATUS) {
            VipStatusScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        // Bookmark Route
        bookmarkRoute(
            onBackClick = {},
            onHotelClick = { hotelId -> navController.navigateToHotelDetail(hotelId, false) }
        )
        // Trip Route
        tripRoute(
            onHotelClick = { hotelId: String, fromMyTrip: Boolean -> 
                navController.navigateToHotelDetail(hotelId, fromMyTrip)
            },
            onBookingClick = { bookingId: String -> 
                android.util.Log.d("AppNavHost", "onBookingClick called with bookingId=$bookingId")
                navController.navigateToBookingDetail(bookingId)
            },
            onWriteReview = { bookingId: String -> 
                navController.navigateToReview(bookingId)
            },
            onViewBill = { bookingId: String -> 
                navController.navigateToBill(bookingId)
            },
            onCancelBooking = { bookingId: String -> 
                // TODO: Handle booking cancellation
            }
        )
        composable(Routes.PROFILE) {
            ProfileScreen()
        }
        // Voucher Routes
        voucherRoutes(
            onBackClick = { navController.popBackStack() },
            onVoucherClick = { voucherId -> 
                navController.navigateToVoucherDetail(voucherId)
            }
        )
        // Review Route
        reviewRoute(
            onBackClick = { navController.popBackStack() }
        )
        // Bill Route
        billRoute(
            onBackClick = { navController.popBackStack() }
        )
    }
}
