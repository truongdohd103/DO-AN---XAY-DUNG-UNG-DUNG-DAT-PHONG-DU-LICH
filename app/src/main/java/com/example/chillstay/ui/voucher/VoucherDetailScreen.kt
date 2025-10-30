package com.example.chillstay.ui.voucher

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherDetailScreen(
    voucherId: String,
    onBackClick: () -> Unit,
    viewModel: VoucherDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(voucherId) {
        viewModel.onEvent(VoucherDetailIntent.LoadVoucherDetail(voucherId))
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Handle error if needed
            android.util.Log.e("VoucherDetailScreen", "Error: $error")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voucher Details",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = uiState.error ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.onEvent(VoucherDetailIntent.ClearError) }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    uiState.voucher != null -> {
                        // Use Column with verticalScroll instead of LazyColumn to avoid nested scrolling issues
                        // This is more appropriate for fixed content cards
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Voucher Header Card
                            VoucherHeaderCard(
                                voucher = uiState.voucher!!,
                                isClaimed = uiState.isClaimed,
                                isClaiming = uiState.isClaiming,
                                isEligible = uiState.isEligible,
                                eligibilityMessage = uiState.eligibilityMessage,
                                onClaimClick = {
                                    currentUser?.uid?.let { userId ->
                                        viewModel.onEvent(VoucherDetailIntent.ClaimVoucher(voucherId, userId))
                                    }
                                }
                            )
                            
                            // Description Card
                            DescriptionCard(
                                description = uiState.voucher!!.description
                            )
                            
                            // Conditions Card - Use actual voucher conditions
                            ConditionsCard(
                                conditions = uiState.voucher?.conditions?.let { conditions ->
                                    buildString {
                                        if (conditions.minBookingAmount > 0) {
                                            appendLine("• Minimum booking amount: $${conditions.minBookingAmount}")
                                        }
                                        if (conditions.maxDiscountAmount > 0) {
                                            appendLine("• Maximum discount: $${conditions.maxDiscountAmount}")
                                        }
                                        if (conditions.maxUsagePerUser > 0) {
                                            appendLine("• Max usage per user: ${conditions.maxUsagePerUser}")
                                        }
                                        if (conditions.maxTotalUsage > 0) {
                                            appendLine("• Total usage limit: ${conditions.maxTotalUsage}")
                                        }
                                        if (conditions.requiredUserLevel != null) {
                                            appendLine("• Required user level: ${conditions.requiredUserLevel}")
                                        }
                                        if (conditions.validDays.isNotEmpty()) {
                                            appendLine("• Valid days: ${conditions.validDays.joinToString(", ")}")
                                        }
                                        if (conditions.validTimeSlots.isNotEmpty()) {
                                            appendLine("• Valid time slots: ${conditions.validTimeSlots.joinToString(", ")}")
                                        }
                                        if (isEmpty()) {
                                            append("No special conditions")
                                        }
                                    }
                                } ?: "No special conditions"
                            )
                            
                            // Applicable Hotels Card
                            if (uiState.applicableHotels.isNotEmpty()) {
                                ApplicableHotelsCard(
                                    hotelIds = uiState.applicableHotels
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
                        }
                    }
                }
            }
        }
}

@Composable
fun VoucherHeaderCard(
    voucher: com.example.chillstay.domain.model.Voucher,
    isClaimed: Boolean,
    isClaiming: Boolean,
    isEligible: Boolean,
    eligibilityMessage: String,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(
                                Color(0xFF87CEEB),
                                Color(0xFF4169E1)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = voucher.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }

            // Content
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Code: ${voucher.code}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (voucher.type) {
                        com.example.chillstay.domain.model.VoucherType.PERCENTAGE -> "${voucher.value}% OFF"
                        com.example.chillstay.domain.model.VoucherType.FIXED_AMOUNT -> "$${voucher.value} OFF"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Eligibility message
                if (eligibilityMessage.isNotEmpty()) {
                    Text(
                        text = eligibilityMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEligible) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Claim button
                if (!isClaimed) {
                    Button(
                        onClick = onClaimClick,
                        enabled = isEligible && !isClaiming,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isClaiming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isClaiming) "Claiming..." else "Claim Voucher")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Claimed",
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Voucher Claimed",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DescriptionCard(description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ConditionsCard(conditions: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = conditions,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ApplicableHotelsCard(hotelIds: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Applicable Hotels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (hotelIds.isEmpty()) {
                Text(
                    text = "This voucher can be used at all hotels",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Use Column instead of LazyColumn to avoid nested scrolling issues
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    hotelIds.forEach { hotelId ->
                        Text(
                            text = "• Hotel ID: $hotelId",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
