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
import com.example.chillstay.ui.auth.AuthEffect
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.home.HomeIntent
import com.example.chillstay.ui.main.MainScreen
import com.example.chillstay.ui.welcome.WelcomeScreen
import com.example.chillstay.ui.welcome.CarouselScreen
import com.example.chillstay.ui.profile.profileRoute
import com.example.chillstay.ui.vip.VipStatusScreen
import com.example.chillstay.ui.search.searchRoute
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
import com.example.chillstay.ui.trip.navigateToTrip
import com.example.chillstay.ui.voucher.voucherRoutes
import com.example.chillstay.ui.voucher.navigateToVoucherDetail
import com.example.chillstay.ui.review.reviewRoute
import com.example.chillstay.ui.review.navigateToReview
import com.example.chillstay.ui.bill.billRoute
import com.example.chillstay.ui.bill.navigateToBill
import com.example.chillstay.ui.roomgallery.roomGalleryRoute
import com.example.chillstay.ui.roomgallery.navigateToRoomGallery
import com.example.chillstay.ui.myreviews.myReviewsRoute
import com.example.chillstay.ui.myreviews.navigateToMyReviews
import com.example.chillstay.ui.allreviews.allReviewsRoute
import com.example.chillstay.ui.allreviews.navigateToAllReviews

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
                AuthEffect.NavigateToMain -> {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTHENTICATION) { inclusive = true }
                    }
                }

                AuthEffect.NavigateToSignIn -> {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.SIGN_UP) { inclusive = true }
                    }
                }

                AuthEffect.NavigateToAuth -> {
                    navController.navigate(Routes.AUTHENTICATION) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }

                is AuthEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val isSignedIn = authState.isAuthenticated

    val computedStart = run {
        val ctx = context
        when {
            OnboardingManager.isFirstLaunch(ctx) -> Routes.WELCOME
            !OnboardingManager.isOnboardingDone(ctx) -> Routes.CAROUSEL
            else -> Routes.MAIN
        }
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val route = destination.route ?: Routes.MAIN
            val last = if (route.contains("${Routes.MAIN}?tab")) {
                val tabVal = arguments?.getString("tab") ?: "0"
                "${Routes.MAIN}?tab=$tabVal"
            } else if (route == Routes.MAIN) {
                val tabVal = arguments?.getString("tab") ?: "0"
                "${Routes.MAIN}?tab=$tabVal"
            } else route
            scope.launch { OnboardingManager.setLastRoute(context, last) }
            if (route.contains("${Routes.MAIN}")) {
                val tabInt = arguments?.getString("tab")?.toIntOrNull() ?: 0
                scope.launch { OnboardingManager.setLastTab(context, tabInt) }
            }
        }
    }

    LaunchedEffect(computedStart) {
        if (computedStart == Routes.MAIN) {
            val last = OnboardingManager.getLastRoute(context)
            if (last != null && last.startsWith("${Routes.MAIN}?tab=")) {
                navController.navigate(last) {
                    popUpTo(Routes.MAIN) { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = computedStart) {
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
                    navController.navigateToHotelDetail(hotelId, fromMyTrip, OnboardingManager.getLastTab(context))
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
                },
                onNavigateToMyReviews = {
                    navController.navigateToMyReviews()
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
                    navController.navigateToHotelDetail(hotelId, fromMyTrip, OnboardingManager.getLastTab(context))
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
                },
                onNavigateToMyReviews = {
                    navController.navigateToMyReviews()
                }
            )
        }
        // Hotel Detail Routes
        hotelDetailRoutes(
            onBackClick = { fromMyTrip, sourceTab ->
                if (fromMyTrip) {
                    navController.navigate("${Routes.MAIN}?tab=3") {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                } else if (sourceTab in 0..4) {
                    navController.navigate("${Routes.MAIN}?tab=$sourceTab") {
                        popUpTo(Routes.MAIN) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    navController.popBackStack()
                }
            },
            onChooseRoomClick = { hotelId ->
                navController.navigateToRoom(hotelId)
            },
            onSeeAllReviewsClick = { hotelId ->
                navController.navigateToAllReviews(hotelId)
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
            },
            onOpenGalleryClick = { hotelId, roomId ->
                navController.navigateToRoomGallery(hotelId, roomId)
            }
        )
        roomGalleryRoute(
            onBackClick = { navController.popBackStack() }
        )
        // Booking Routes
        bookingRoutes(
            onBackClick = {
                homeViewModel.onEvent(HomeIntent.RefreshUserSections)
                homeViewModel.onEvent(HomeIntent.RefreshBookmarks)
                navController.popBackStack()
            },
            onNavigateToMyTrips = {
                homeViewModel.onEvent(HomeIntent.RefreshUserSections)
                navController.navigate("${Routes.MAIN}?tab=3") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        composable(Routes.VIP_STATUS) {
            VipStatusScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        searchRoute(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=0") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onHotelClick = { hotelId -> navController.navigateToHotelDetail(hotelId, false, 0) }
        )
        // Bookmark Route
        bookmarkRoute(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=2") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onHotelClick = { hotelId -> navController.navigateToHotelDetail(hotelId, false, 2) }
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
        profileRoute(
            onLogoutClick = { authViewModel.onEvent(com.example.chillstay.ui.auth.AuthIntent.SignOut) }
        )
        // My Reviews Route
        myReviewsRoute(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=4") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onHotelClick = { hotelId -> navController.navigateToHotelDetail(hotelId, false) }
        )
        // Voucher Routes
        voucherRoutes(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=1") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onVoucherClick = { voucherId -> 
                navController.navigateToVoucherDetail(voucherId)
            }
        )
        // Review Route
        reviewRoute(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=3") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        // All Reviews Route
        allReviewsRoute(
            onBackClick = { navController.popBackStack() }
        )
        // Bill Route
        billRoute(
            onBackClick = {
                navController.navigate("${Routes.MAIN}?tab=3") {
                    popUpTo(Routes.MAIN) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}
