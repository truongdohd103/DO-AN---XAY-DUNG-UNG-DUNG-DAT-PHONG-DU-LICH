package com.example.chillstay.ui.admin.customer.customer_view

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.CustomerActivity
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.domain.model.VipLevel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerViewScreen(
    userId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToBooking: (String) -> Unit = {},
    onNavigateToReview: (String) -> Unit = {},
    viewModel: CustomerViewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)

    LaunchedEffect(userId) {
        viewModel.onEvent(CustomerViewIntent.LoadCustomerDetails(userId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomerViewEffect.NavigateBack -> onNavigateBack()
                is CustomerViewEffect.NavigateToBookingDetail -> onNavigateToBooking(effect.bookingId)
                is CustomerViewEffect.NavigateToReviewDetail -> onNavigateToReview(effect.reviewId)
                is CustomerViewEffect.ShowNotificationSent -> {
                    // Show toast or snackbar
                }
                is CustomerViewEffect.ShowBlacklistSuccess -> {
                    // Show toast or snackbar
                }
                is CustomerViewEffect.ShowError -> {
                    // Handle error
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
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Customer View",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 30.sp
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
                                onClick = {
                                    viewModel.onEvent(CustomerViewIntent.LoadCustomerDetails(userId))
                                }
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
                            .padding(20.dp)
                            .padding(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Customer Info Card
                        CustomerInfoCard(
                            user = uiState.user,
                            vipLevel = uiState.vipLevel
                        )

                        // Personal Information Card
                        PersonalInfoCard(
                            user = uiState.user,
                            stats = uiState.stats
                        )

                        // Recent Activity Card
                        RecentActivityCard(
                            selectedTab = uiState.selectedTab,
                            activities = uiState.paginatedActivities,
                            isLoadingActivities = uiState.isLoadingActivities,
                            currentPage = uiState.currentPage,
                            totalPages = uiState.totalPages,
                            onTabSelected = { tab ->
                                viewModel.onEvent(CustomerViewIntent.SelectActivityTab(tab))
                            },
                            onActivityClick = { activity ->
                                viewModel.onEvent(
                                    CustomerViewIntent.ViewActivity(
                                        activity.relatedId,
                                        activity.type.name
                                    )
                                )
                            },
                            onPreviousPage = {
                                viewModel.onEvent(CustomerViewIntent.PreviousPage)
                            },
                            onNextPage = {
                                viewModel.onEvent(CustomerViewIntent.NextPage)
                            }
                        )
                    }
                }
            }
        }

        // Bottom Action Buttons
        if (!uiState.isLoading && uiState.error == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 35.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.onEvent(CustomerViewIntent.SendNotification) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Send Notification",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { viewModel.onEvent(CustomerViewIntent.AddToBlacklist) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Add to Blacklist",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerInfoCard(
    user: User?,
    vipLevel: VipLevel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0D9488), Color(0xFF14B8A6))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getInitials(user?.fullName ?: ""),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // User Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = user?.fullName ?: "Unknown",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    // VIP Badge
                    StatusBadge(
                        text = vipLevel.displayName,
                        backgroundColor = getVipBadgeColor(vipLevel),
                        textColor = getVipTextColor(vipLevel)
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun PersonalInfoCard(
    user: User?,
    stats: CustomerStats
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Personal Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            InfoItem(
                label = "Email",
                value = user?.email ?: "N/A"
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            InfoItem(
                label = "Phone",
                value = user?.phoneNumber ?: "N/A"
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            InfoItem(
                label = "Join Date",
                value =  if(user?.memberSince == null) "N/A" else formatDate(user.memberSince.toDate())
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            InfoItem(
                label = "Total Bookings",
                value = stats.totalBookings.toString()
            )

            HorizontalDivider(color = Color(0xFFE5E7EB))

            InfoItem(
                label = "Total Spent",
                value = "$${stats.totalSpent.toInt()}",
                showDivider = false
            )
        }
    }
}

@Composable
fun RecentActivityCard(
    selectedTab: ActivityTab,
    activities: List<CustomerActivity>,
    isLoadingActivities: Boolean,
    currentPage: Int,
    totalPages: Int,
    onTabSelected: (ActivityTab) -> Unit,
    onActivityClick: (CustomerActivity) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Recent Activity",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabButton(
                    text = "Booking",
                    isSelected = selectedTab == ActivityTab.BOOKING,
                    onClick = { onTabSelected(ActivityTab.BOOKING) },
                    modifier = Modifier.weight(1f)
                )

                TabButton(
                    text = "Review",
                    isSelected = selectedTab == ActivityTab.REVIEW,
                    onClick = { onTabSelected(ActivityTab.REVIEW) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Activities List or Loading
            when {
                isLoadingActivities -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1AB6B6))
                    }
                }
                activities.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No activities found",
                            color = Color(0xFF6B7280),
                            fontSize = 14.sp
                        )
                    }
                }
                else -> {
                    // Display paginated activities
                    activities.forEach { activity ->
                        ActivityItem(
                            activity = activity,
                            onClick = { onActivityClick(activity) }
                        )
                    }

                    // Pagination Controls
                    if (totalPages > 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color(0xFFE5E7EB)
                        )

                        PaginationControls(
                            currentPage = currentPage,
                            totalPages = totalPages,
                            onPreviousPage = onPreviousPage,
                            onNextPage = onNextPage,
                            tealColor = Color(0xFF1AB6B6)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (showDivider) 8.dp else 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Start,
            modifier = Modifier.wrapContentWidth()
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            maxLines = Int.MAX_VALUE,
            overflow = TextOverflow.Clip
        )
    }
}



@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1AB6B6) else Color.White,
            contentColor = if (isSelected) Color.White else Color(0xFF374151)
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFFD1D5DB)) else null
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ActivityItem(
    activity: CustomerActivity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9FAFB)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = activity.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Text(
                    text = activity.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280)
                )
            }

            Text(
                text = getTimeAgo(activity.createdAt.toDate()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
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
                containerColor = tealColor,
                disabledContainerColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Previous",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Page $currentPage of $totalPages",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF6B7280)
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor,
                disabledContainerColor = Color(0xFFE5E7EB)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Next",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StatusBadge(
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

private fun getInitials(fullName: String): String {
    val names = fullName.trim().split(" ")
    return when {
        names.isEmpty() -> ""
        names.size == 1 -> names[0].take(2).uppercase()
        else -> "${names.first().first()}${names.last().first()}".uppercase()
    }
}

private fun formatDate(date: Date): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date)
}

private fun getTimeAgo(date: Date): String {
    val now = Date()
    val diff = now.time - date.time

    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}

private fun getVipBadgeColor(level: VipLevel): Color {
    return when (level) {
        VipLevel.BRONZE -> Color(0xFFFED7AA)
        VipLevel.SILVER -> Color(0xFFF3F4F6)
        VipLevel.GOLD -> Color(0xFFFEF3C7)
        VipLevel.PLATINUM -> Color(0xFFE0E7FF)
        VipLevel.DIAMOND -> Color(0xFFDCFCE7)
    }
}

private fun getVipTextColor(level: VipLevel): Color {
    return when (level) {
        VipLevel.BRONZE -> Color(0xFF9A3412)
        VipLevel.SILVER -> Color(0xFF374151)
        VipLevel.GOLD -> Color(0xFF92400E)
        VipLevel.PLATINUM -> Color(0xFF4338CA)
        VipLevel.DIAMOND -> Color(0xFF065F46)
    }
}