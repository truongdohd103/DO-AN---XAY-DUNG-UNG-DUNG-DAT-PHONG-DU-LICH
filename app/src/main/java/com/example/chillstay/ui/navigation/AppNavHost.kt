package com.example.chillstay.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chillstay.core.common.OnboardingManager
import kotlinx.coroutines.launch
import com.example.chillstay.ui.auth.AuthenticationScreen
import com.example.chillstay.ui.auth.SignInScreen
import com.example.chillstay.ui.auth.SignUpScreen
import com.example.chillstay.ui.auth.AuthViewModel
import com.example.chillstay.ui.auth.AuthUiEffect
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.main.MainScreen
import com.example.chillstay.ui.welcome.WelcomeScreen
import com.example.chillstay.ui.welcome.CarouselScreen
import com.example.chillstay.ui.profile.ProfileScreen
import com.example.chillstay.ui.vip.VipStatusScreen
import com.example.chillstay.ui.search.SearchScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

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

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Routes.WELCOME,
    homeViewModel: HomeViewModel
) {
    val authViewModel: AuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(authViewModel) {
        authViewModel.uiEffect.collect { effect ->
            when (effect) {
                AuthUiEffect.NavigateToMain -> {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                    }
                }

                AuthUiEffect.NavigateToSignIn -> {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }

                AuthUiEffect.NavigateToAuth -> {
                    navController.navigate(Routes.AUTHENTICATION) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }

                is AuthUiEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val isSignedIn = authState.isAuthenticated

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
                state = authState,
                onEvent = authViewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { navController.navigate(Routes.SIGN_UP) },
                onForgotPasswordClick = { /* TODO: Implement forgot password */ },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ }
            )
        }
        composable(Routes.SIGN_UP) {
            SignUpScreen(
                state = authState,
                onEvent = authViewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                onSignInClick = { navController.navigate(Routes.SIGN_IN) },
                onGoogleClick = { /* TODO: Implement Google auth */ },
                onFacebookClick = { /* TODO: Implement Facebook auth */ }
            )
        }
        composable(Routes.MAIN) {
            MainScreen(
                homeViewModel = homeViewModel,
                authState = authState,
                onAuthEvent = authViewModel::onEvent,
                initialTab = 0,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { hotelId, fromMyTrip ->
                    android.util.Log.d("AppNavHost", "onHotelClick called with hotelId=$hotelId, fromMyTrip=$fromMyTrip")
                    navController.navigateToHotelDetail(hotelId, fromMyTrip)
                },
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) },
                onVipClick = {
                    if (isSignedIn) navController.navigate(Routes.VIP_STATUS) else navController.navigate(Routes.AUTHENTICATION)
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                onContinueItemClick = { hotelId, roomId, dateFrom, dateTo ->
                    if (isSignedIn) {
                        navController.navigateToNewBooking(hotelId, roomId, dateFrom, dateTo)
                    } else {
                        navController.navigate(Routes.AUTHENTICATION)
                    }
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
        composable("${Routes.MAIN}?tab={tab}") { backStackEntry ->
            val tabParam = backStackEntry.arguments?.getString("tab")?.toIntOrNull() ?: 0
            android.util.Log.d("AppNavHost", "MainScreen with tab parameter: $tabParam")
            MainScreen(
                homeViewModel = homeViewModel,
                authState = authState,
                onAuthEvent = authViewModel::onEvent,
                initialTab = tabParam,
                onBackClick = { navController.popBackStack() },
                onHotelClick = { hotelId, fromMyTrip ->
                    android.util.Log.d("AppNavHost", "onHotelClick called with hotelId=$hotelId, fromMyTrip=$fromMyTrip")
                    navController.navigateToHotelDetail(hotelId, fromMyTrip)
                },
                onRequireAuth = { navController.navigate(Routes.AUTHENTICATION) },
                onVipClick = {
                    if (isSignedIn) navController.navigate(Routes.VIP_STATUS) else navController.navigate(Routes.AUTHENTICATION)
                },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onContinueItemClick = { hotelId, roomId, dateFrom, dateTo ->
                    if (isSignedIn) {
                        navController.navigateToNewBooking(hotelId, roomId, dateFrom, dateTo)
                    } else {
                        navController.navigate(Routes.AUTHENTICATION)
                    }
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
            ProfileScreen(
                state = authState,
                onEvent = authViewModel::onEvent
            )
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
