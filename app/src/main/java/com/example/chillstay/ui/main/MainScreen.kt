package com.example.chillstay.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.chillstay.ui.components.BottomNavigationBar
import com.example.chillstay.ui.home.HomeScreen
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.voucher.VoucherScreen
import com.example.chillstay.ui.bookmark.MyBookmarkScreen
import com.example.chillstay.ui.trip.MyTripScreen
import com.example.chillstay.ui.profile.ProfileScreen

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    onBackClick: () -> Unit = {},
    onHotelClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
                when (selectedTab) {
                    0 -> HomeScreen(viewModel = homeViewModel, onHotelClick = onHotelClick)
                    1 -> VoucherScreen(onBackClick = onBackClick)
                    2 -> MyBookmarkScreen(onBackClick = onBackClick, onHotelClick = onHotelClick)
                    3 -> MyTripScreen(onBackClick = onBackClick, onHotelClick = onHotelClick)
                    4 -> ProfileScreen(onBackClick = onBackClick)
                }
        }
    }
}
