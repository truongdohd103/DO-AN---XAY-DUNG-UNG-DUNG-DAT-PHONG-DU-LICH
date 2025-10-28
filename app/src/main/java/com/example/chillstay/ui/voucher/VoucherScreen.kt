package com.example.chillstay.ui.voucher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherScreen(
    onBackClick: () -> Unit = {},
    onVoucherClick: (String) -> Unit = {},
    showBackButton: Boolean = true,
    viewModel: VoucherViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(Unit) {
        viewModel.onEvent(VoucherIntent.LoadVouchers)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            // Handle error if needed
            android.util.Log.e("VoucherScreen", "Error: $error")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voucher",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            when {
                uiState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                
                uiState.error != null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
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
                                    onClick = { 
                                        viewModel.onEvent(VoucherIntent.ClearError)
                                        viewModel.onEvent(VoucherIntent.LoadVouchers)
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }
                
                uiState.vouchers.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No vouchers available",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                else -> {
                    items(uiState.vouchers) { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            onClick = { onVoucherClick(voucher.id) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
            }
        }
    }
}


@Composable
fun VoucherCard(
    voucher: com.example.chillstay.domain.model.Voucher,
    onClick: () -> Unit
) {
    val gradientColors = when (voucher.type) {
        com.example.chillstay.domain.model.VoucherType.PERCENTAGE -> 
            listOf(Color(0xFF87CEEB), Color(0xFF4169E1))
        com.example.chillstay.domain.model.VoucherType.FIXED_AMOUNT -> 
            listOf(Color(0xFF1AB6B6), Color(0xFF159999))
    }
    
    val expiresIn = formatExpirationDate(voucher.validTo)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(brush = Brush.linearGradient(gradientColors)),
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = voucher.description,
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Code: ${voucher.code}",
                    color = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
                if (expiresIn.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = expiresIn,
                        color = Color(0xFF1AB6B6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatExpirationDate(validTo: com.google.firebase.Timestamp): String {
    val now = Date()
    val validToDate = validTo.toDate()
    val diffInMillis = validToDate.time - now.time
    val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)
    
    return when {
        diffInDays < 0 -> "Expired"
        diffInDays == 0L -> "Expires today"
        diffInDays == 1L -> "1 day left"
        diffInDays < 7 -> "$diffInDays days left"
        else -> {
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            "Expires ${formatter.format(validToDate)}"
        }
    }
}