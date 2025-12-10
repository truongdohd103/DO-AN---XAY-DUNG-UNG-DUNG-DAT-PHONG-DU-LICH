package com.example.chillstay.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import com.example.chillstay.ui.components.BottomNavigationBar
import com.example.chillstay.ui.home.HomeScreen
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.voucher.VoucherScreen
import com.example.chillstay.ui.voucher.VoucherDetailScreen  // Import thêm
import com.example.chillstay.ui.bookmark.MyBookmarkScreen
import com.example.chillstay.ui.trip.MyTripScreen
import com.example.chillstay.ui.profile.ProfileScreen
import com.example.chillstay.ui.profile.ProfileViewModel
import com.example.chillstay.ui.profile.ProfileUiEffect
import com.example.chillstay.ui.navigation.Routes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import android.util.Log
import com.example.chillstay.ui.auth.AuthIntent
import com.example.chillstay.ui.auth.AuthState
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.chillstay.core.common.OnboardingManager
import android.content.pm.ApplicationInfo

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    authState: AuthState,
    onAuthEvent: (AuthIntent) -> Unit,
    initialTab: Int = 0,
    onBackClick: () -> Unit = {},
    onHotelClick: (String, Boolean) -> Unit = { _, _ -> },
    onRequireAuth: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onContinueItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> },
    onVoucherClick: (String) -> Unit = {},  // Giữ nhưng không dùng trực tiếp
    onNavigateToReview: (String) -> Unit = {},
    onNavigateToBill: (String) -> Unit = {},
    onNavigateToBooking: (String) -> Unit = {},
    onNavigateToMyReviews: () -> Unit = {}
) {
    // Use rememberSaveable to persist tab selection across navigation
    var selectedTab by rememberSaveable { 
        android.util.Log.d("MainScreen", "Initializing with initialTab: $initialTab")
        mutableStateOf(initialTab) 
    }

    val onboardingContext = LocalContext.current
    // Update selectedTab when initialTab changes
    LaunchedEffect(initialTab) {
        android.util.Log.d("MainScreen", "LaunchedEffect: initialTab changed to $initialTab")
        selectedTab = initialTab
        val lastRoute = "${Routes.MAIN}?tab=$initialTab"
        val tabIndex = initialTab
        OnboardingManager.setLastRoute(onboardingContext, lastRoute)
        OnboardingManager.setLastTab(onboardingContext, tabIndex)
    }

    val isSignedIn = authState.isAuthenticated

    // Use coroutine scope for background operations
    val coroutineScope = rememberCoroutineScope()

    val profileViewModel: ProfileViewModel = koinViewModel()
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val profileContext = LocalContext.current
    LaunchedEffect(profileViewModel) {
        profileViewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProfileUiEffect.ShowMessage -> Toast.makeText(profileContext, effect.message, Toast.LENGTH_LONG).show()
                ProfileUiEffect.NavigateToMyReviews -> onNavigateToMyReviews()
            }
        }
    }

    // Nested NavController cho Voucher tab (sub-stack: Voucher list → Detail)
    val voucherNavController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    // Optimize bookmark refresh with debouncing
    LaunchedEffect(selectedTab) {
        Log.d("MainScreen", "Tab changed to $selectedTab, refreshing bookmarks if needed")

        when (selectedTab) {
            0 -> { // Home tab - refresh bookmarks when coming back
                coroutineScope.launch {
                    homeViewModel.onEvent(com.example.chillstay.ui.home.HomeIntent.RefreshBookmarks)
                }
            }
            2 -> { // Bookmark tab
                coroutineScope.launch {
                    homeViewModel.onEvent(com.example.chillstay.ui.home.HomeIntent.RefreshBookmarks)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    Log.d("MainScreen", "Tab selected: $tabIndex, isSignedIn: $isSignedIn")

                    if (tabIndex == 2 || tabIndex == 3 || tabIndex == 4) {
                        if (isSignedIn) {
                            selectedTab = tabIndex
                            val lastRoute = "${Routes.MAIN}?tab=$tabIndex"
                            val idx = tabIndex
                            coroutineScope.launch { OnboardingManager.setLastRoute(onboardingContext, lastRoute) }
                            coroutineScope.launch { OnboardingManager.setLastTab(onboardingContext, idx) }
                        } else {
                            onRequireAuth()
                        }
                    } else {
                        selectedTab = tabIndex
                        val lastRoute = "${Routes.MAIN}?tab=$tabIndex"
                        val idx = tabIndex
                        coroutineScope.launch { OnboardingManager.setLastRoute(onboardingContext, lastRoute) }
                        coroutineScope.launch { OnboardingManager.setLastTab(onboardingContext, idx) }
                    }
                }
            )
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp)
        
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Use when expression with memoized selectedTab to optimize recomposition
            when (selectedTab) {
                0 -> HomeScreen(
                    viewModel = homeViewModel,
                    onHotelClick = { hotelId -> onHotelClick(hotelId, false) },
                    onVipClick = onVipClick,
                    onSearchClick = onSearchClick,
                    onSeeAllRecentClick = { 
                        selectedTab = 3 // My Trip tab
                        // Note: MyTripScreen will show COMPLETED tab by default
                    },
                    onContinueItemClick = onContinueItemClick,
                    onRequireAuth = onRequireAuth
                )
                1 -> {
                    // Nested NavHost cho Voucher tab: Handle sub-navigation (list → detail)
                    NavHost(
                        navController = voucherNavController,
                        startDestination = Routes.VOUCHER,  // Start tại Voucher list
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(Routes.VOUCHER) {
                            VoucherScreen(
                                onBackClick = {}, // Không cần back ở list (tab handle)
                                onVoucherClick = { voucherId ->
                                    voucherNavController.navigate("${Routes.VOUCHER_DETAIL}/$voucherId") {
                                        launchSingleTop = true  // Tránh duplicate detail
                                    }
                                },
                                showBackButton = false  // Ẩn back ở list
                            )
                        }
                        composable("${Routes.VOUCHER_DETAIL}/{voucherId}") { backStackEntry ->
                            val voucherId = backStackEntry.arguments?.getString("voucherId") ?: ""
                            VoucherDetailScreen(
                                voucherId = voucherId,
                                onBackClick = {
                                    // Back trong sub-stack: Pop về Voucher list (giữ tab=1)
                                    if (!voucherNavController.popBackStack()) {
                                        // Fallback nếu stack empty: Stay at list
                                        Log.d("MainScreen", "Already at Voucher list, no pop needed")
                                    }
                                }
                            )
                        }
                    }
                }
                2 -> MyBookmarkScreen(onBackClick = {}, onHotelClick = { hotelId -> onHotelClick(hotelId, false) })
                3 -> MyTripScreen(
                           onHotelClick = { hotelId, fromMyTrip -> onHotelClick(hotelId, fromMyTrip) },
                           onBookingClick = { bookingId: String -> 
                               // Navigate to booking detail for pending bookings
                               Log.d("MainScreen", "Navigate to booking: $bookingId")
                               onNavigateToBooking(bookingId)
                           },
                           onWriteReview = { bookingId: String -> 
                               // Navigate to review screen
                               onNavigateToReview(bookingId)
                           },
                           onViewBill = { bookingId: String -> 
                               // Navigate to bill screen
                               onNavigateToBill(bookingId)
                           },
                           onCancelBooking = { bookingId: String -> 
                               // Cancel booking and refresh data
                               Log.d("MainScreen", "Cancel booking: $bookingId")
                               // Refresh home data after cancellation
                               coroutineScope.launch {
                                   homeViewModel.onEvent(com.example.chillstay.ui.home.HomeIntent.RefreshBookmarks)
                               }
                           },
                           initialTab = 1 // Show COMPLETED tab by default
                       )
                4 -> ProfileScreen(
                    state = profileState,
                    onEvent = profileViewModel::onEvent,
                    onLogoutClick = { onAuthEvent(AuthIntent.SignOut) }
                )
            }
            
        }
    }
}
