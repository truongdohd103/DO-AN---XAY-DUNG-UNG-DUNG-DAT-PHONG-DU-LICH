package com.example.chillstay.ui.admin.customer.customer_manage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipLevel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerManageScreen(
    onBack: () -> Unit = {},
    onView: (String) -> Unit = {},
    onDisable: (String) -> Unit = {},
    onEnable: (String) -> Unit = {},
    viewModel: CustomerManageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F5F5)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomerManageEffect.NavigateBack -> onBack()
                is CustomerManageEffect.NavigateToCustomerView -> onView(effect.userId)
                is CustomerManageEffect.ShowStatusChangeSuccess -> {
                    // Show success message if needed
                }
                is CustomerManageEffect.ShowError -> {
                    // Handle error if needed
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = tealColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Customers",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Content
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
                            Text(
                                text = uiState.error ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.onEvent(CustomerManageIntent.LoadCustomers) }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Greeting
                        Text(
                            text = "Hello 👋",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(CustomerManageIntent.SearchQueryChanged(it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(63.dp),
                            placeholder = {
                                Text(
                                    "Search",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF767676)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF767676)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.onEvent(CustomerManageIntent.PerformSearch)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = lightGray,
                                unfocusedContainerColor = lightGray,
                                disabledContainerColor = lightGray,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Statistics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatisticCard(
                                title = "Total Customers",
                                value = uiState.totalCustomers.toString(),
                                backgroundColor = Color(0xFFE3F2FD),
                                textColor = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )

                            StatisticCard(
                                title = "Active Customers",
                                value = uiState.activeCustomers.toString(),
                                backgroundColor = Color(0xFFE8F5E9),
                                textColor = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // All Customers Title
                        Text(
                            text = "All Customers",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Customer List
                        uiState.paginatedCustomers.forEach { customer ->
                            CustomerCard(
                                customer = customer,
                                isActive = true, // TODO: Get actual status
                                vipLevel = VipLevel.BRONZE, // TODO: Get actual VIP level
                                onViewClick = {
                                    viewModel.onEvent(CustomerManageIntent.ViewCustomer(customer))
                                },
                                onToggleStatusClick = {
                                    viewModel.onEvent(CustomerManageIntent.ToggleCustomerStatus(customer))
                                }
                            )
                        }

                        // Pagination
                        if (uiState.customers.isNotEmpty()) {
                            PaginationControls(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                onPageChange = {
                                    viewModel.onEvent(CustomerManageIntent.GoToPage(it))
                                },
                                onPreviousPage = {
                                    viewModel.onEvent(CustomerManageIntent.PreviousPage)
                                },
                                onNextPage = {
                                    viewModel.onEvent(CustomerManageIntent.NextPage)
                                },
                                tealColor = tealColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun CustomerCard(
    customer: User,
    isActive: Boolean,
    vipLevel: VipLevel,
    onViewClick: () -> Unit,
    onToggleStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = if (isActive) {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0D9488), Color(0xFF14B8A6))
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF9CA3AF), Color(0xFF9CA3AF))
                            )
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(customer.fullName),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Customer Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = customer.fullName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = customer.email,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    maxLines = 1
                )

                // TODO: Add phone number field to User model
                Text(
                    text = "+1 234-567-8900", // Placeholder
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )

                // Status and VIP Level Badges
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status Badge
                    Badge(
                        text = if (isActive) "Active" else "Inactive",
                        backgroundColor = if (isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                        textColor = if (isActive) Color(0xFF065F46) else Color(0xFF991B1B)
                    )

                    // VIP Level Badge
                    Badge(
                        text = vipLevel.displayName,
                        backgroundColor = when (vipLevel) {
                            VipLevel.BRONZE -> Color(0xFFFED7AA)
                            VipLevel.SILVER -> Color(0xFFF3F4F6)
                            VipLevel.GOLD -> Color(0xFFFEF3C7)
                            VipLevel.PLATINUM -> Color(0xFFE0E7FF)
                            VipLevel.DIAMOND -> Color(0xFFDCFCE7)
                        },
                        textColor = when (vipLevel) {
                            VipLevel.BRONZE -> Color(0xFF9A3412)
                            VipLevel.SILVER -> Color(0xFF374151)
                            VipLevel.GOLD -> Color(0xFF92400E)
                            VipLevel.PLATINUM -> Color(0xFF4338CA)
                            VipLevel.DIAMOND -> Color(0xFF065F46)
                        }
                    )
                }
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onViewClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "View",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onToggleStatusClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color(0xFFEF4444) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isActive) "Disable" else "Enable",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun Badge(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    tealColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Previous")
        }

        Text(
            text = "Page $currentPage of $totalPages",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Next")
        }
    }
}

private fun getInitials(fullName: String): String {
    val names = fullName.trim().split(" ")
    return when {
        names.isEmpty() -> ""
        names.size == 1 -> names[0].take(2).uppercase()
        else -> "${names.first().first()}${names.last().first()}".uppercase()
    }
}