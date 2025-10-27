package com.example.chillstay.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.firebase.auth.FirebaseAuth
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

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {},
    onRequireAuth: () -> Unit = {},
    onVipClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onContinueItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> },
    onVoucherClick: (String) -> Unit = {}  // Giữ nhưng không dùng trực tiếp
) {
    // Use remember to prevent unnecessary recomposition
    var selectedTab by remember { mutableStateOf(0) }

    // Memoize FirebaseAuth check to avoid repeated calls
    val isSignedIn by remember {
        derivedStateOf { FirebaseAuth.getInstance().currentUser != null }
    }

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
                    homeViewModel.handleIntent(com.example.chillstay.ui.home.HomeIntent.RefreshBookmarks)
                }
            }
            2 -> { // Bookmark tab
                coroutineScope.launch {
                    homeViewModel.handleIntent(com.example.chillstay.ui.home.HomeIntent.RefreshBookmarks)
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

                    // Use memoized isSignedIn value to avoid repeated FirebaseAuth calls
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
                    onHotelClick = onHotelClick,
                    onVipClick = onVipClick,
                    onSearchClick = onSearchClick,
                    onSeeAllRecentClick = { selectedTab = 3 },
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
                2 -> MyBookmarkScreen(onBackClick = {}, onHotelClick = onHotelClick)
                3 -> MyTripScreen(onHotelClick = { onHotelClick("") })
                4 -> ProfileScreen(
                    onLogout = {
                        Log.d("MainScreen", "User logged out, switching to home tab")
                        // Use coroutine scope for Firebase sign out
                        coroutineScope.launch {
                            FirebaseAuth.getInstance().signOut()
                            selectedTab = 0
                        }
                    }
                )
            }
        }
    }
}