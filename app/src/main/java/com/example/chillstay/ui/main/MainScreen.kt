package com.example.chillstay.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.chillstay.ui.components.BottomNavigationBar
import com.example.chillstay.ui.home.HomeScreen
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.voucher.VoucherScreen
import com.example.chillstay.ui.voucher.VoucherDetailScreen  // Import thêm
import com.example.chillstay.ui.bookmark.MyBookmarkScreen
import com.example.chillstay.ui.trip.MyTripScreen
import com.example.chillstay.ui.profile.ProfileScreen
import com.example.chillstay.ui.navigation.Routes
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import android.util.Log
import com.example.chillstay.ui.auth.AuthUiEvent
import com.example.chillstay.ui.auth.AuthUiState

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    authState: AuthUiState,
    onAuthEvent: (AuthUiEvent) -> Unit,
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
    onNavigateToBooking: (String) -> Unit = {}
) {
    // Use rememberSaveable to persist tab selection across navigation
    var selectedTab by rememberSaveable { 
        android.util.Log.d("MainScreen", "Initializing with initialTab: $initialTab")
        mutableStateOf(initialTab) 
    }

    // Update selectedTab when initialTab changes
    LaunchedEffect(initialTab) {
        android.util.Log.d("MainScreen", "LaunchedEffect: initialTab changed to $initialTab")
        selectedTab = initialTab
    }

    val isSignedIn = authState.isAuthenticated

    // Use coroutine scope for background operations
    val coroutineScope = rememberCoroutineScope()

    // Nested NavController cho Voucher tab (sub-stack: Voucher list → Detail)
    val voucherNavController = rememberNavController()

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
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tabIndex ->
                    Log.d("MainScreen", "Tab selected: $tabIndex, isSignedIn: $isSignedIn")

                    if (tabIndex == 2 || tabIndex == 3 || tabIndex == 4) {
                        if (isSignedIn) {
                            selectedTab = tabIndex
                        } else {
                            onRequireAuth()
                        }
                    } else {
                        selectedTab = tabIndex
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                    onContinueItemClick = onContinueItemClick
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
                    state = authState,
                    onEvent = onAuthEvent
                )
            }
        }
    }
}