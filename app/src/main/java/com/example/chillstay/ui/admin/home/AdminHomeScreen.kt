package com.example.chillstay.ui.admin.home

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.R
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    onNavigateToAccommodation: () -> Unit = {},
    onNavigateToVoucher: () -> Unit = {},
    onNavigateToCustomer: () -> Unit = {},
    onNavigateToNotification: () -> Unit = {},
    onNavigateToBooking: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToPrice: () -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {},
    viewModel: AdminHomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F5F5)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AdminHomeEffect.NavigateToAccommodation -> onNavigateToAccommodation()
                is AdminHomeEffect.NavigateToVoucher -> onNavigateToVoucher()
                is AdminHomeEffect.NavigateToCustomer -> onNavigateToCustomer()
                is AdminHomeEffect.NavigateToNotification -> onNavigateToNotification()
                is AdminHomeEffect.NavigateToBooking -> onNavigateToBooking()
                is AdminHomeEffect.NavigateToStatistics -> onNavigateToStatistics()
                is AdminHomeEffect.NavigateToPrice -> onNavigateToPrice()
                is AdminHomeEffect.NavigateToCalendar -> onNavigateToCalendar()
                is AdminHomeEffect.NavigateToProfile -> onNavigateToProfile()
                is AdminHomeEffect.NavigateToAuth -> onNavigateToAuth()
                is AdminHomeEffect.ShowError -> {
                    // Handle error if needed
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chillstay Admin",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AdminHomeIntent.SignOut) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = tealColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Greeting
                    Text(
                        text = uiState.greeting,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    // Menu Buttons
                    MenuButton(
                        text = "Accommodation",
                        backgroundColor = tealColor,
                        onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToAccommodation) }
                    )

                    MenuButton(
                        text = "Voucher",
                        backgroundColor = tealColor,
                        onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToVoucher) }
                    )

                    MenuButton(
                        text = "Customer",
                        backgroundColor = tealColor,
                        onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToCustomer) }
                    )

                    MenuButton(
                        text = "Notification",
                        backgroundColor = tealColor,
                        onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToNotification) }
                    )

                    MenuButton(
                        text = "Booking",
                        backgroundColor = tealColor,
                        onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToBooking) }
                    )

                    StatisticsButton(
                        text = "Statistics",
                        backgroundColor = tealColor,
                        isExpanded = uiState.isStatisticsExpanded,
                        onToggle = { viewModel.onEvent(AdminHomeIntent.ToggleStatistics) }
                    )

                    // Secondary Buttons - chỉ hiển thị khi Statistics được mở
                    Column(
                        modifier = Modifier.animateContentSize()
                    ) {
                        if (uiState.isStatisticsExpanded) {
                            MenuButton(
                                text = "Accommodation",
                                backgroundColor = lightGray,
                                textColor = Color.Black,
                                onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToAccommodation) }
                            )

                            MenuButton(
                                text = "Customer",
                                backgroundColor = lightGray,
                                textColor = Color.Black,
                                onClick = { viewModel.onEvent(AdminHomeIntent.NavigateToCustomer) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

        }
    }
}

@Composable
fun MenuButton(
    text: String,
    backgroundColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF0E9485).copy(alpha = 0.2f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun StatisticsButton(
    text: String,
    backgroundColor: Color,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    // Animation cho icon xoay
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "icon_rotation"
    )

    Button(
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = Color(0xFF0E9485).copy(alpha = 0.2f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Text ở chính giữa
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
            
            // Icon ở bên phải
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Toggle",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(24.dp)
                    .rotate(rotationAngle)
            )
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminHomeScreenPreview() {
    val dummyAuthRepo = object : com.example.chillstay.domain.repository.AuthRepository {
        override suspend fun signIn(email: String, password: String): String = ""
        override suspend fun signUp(email: String, password: String): String = ""
        override suspend fun signOut() {}
        override fun getCurrentUserId(): String? = null
    }
    
    MaterialTheme {
        AdminHomeScreen(
            viewModel = AdminHomeViewModel(
                signOutUseCase = com.example.chillstay.domain.usecase.user.SignOutUseCase(dummyAuthRepo)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MenuButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MenuButton(
                text = "Accommodation",
                backgroundColor = Color(0xFFF5F5F5),
                textColor = Color.Black,
                onClick = {}
            )
            MenuButton(
                text = "Customer",
                backgroundColor = Color(0xFFF5F5F5),
                textColor = Color.Black,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsButtonPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            var isExpanded by remember { mutableStateOf(false) }
            
            StatisticsButton(
                text = "Statistics",
                backgroundColor = Color(0xFF1AB5B5),
                isExpanded = isExpanded,
                onToggle = { isExpanded = !isExpanded }
            )
            
            if (isExpanded) {
                MenuButton(
                    text = "Accommodation",
                    backgroundColor = Color(0xFFF5F5F5),
                    textColor = Color.Black,
                    onClick = {}
                )
                MenuButton(
                    text = "Customer",
                    backgroundColor = Color(0xFFF5F5F5),
                    textColor = Color.Black,
                    onClick = {}
                )
            }
        }
    }
}
