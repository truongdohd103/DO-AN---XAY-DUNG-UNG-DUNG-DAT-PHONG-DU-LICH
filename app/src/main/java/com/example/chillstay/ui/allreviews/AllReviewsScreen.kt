package com.example.chillstay.ui.allreviews

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllReviewsScreen(
    hotelId: String,
    viewModel: AllReviewsViewModel = koinViewModel(),
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()

    LaunchedEffect(hotelId) {
        viewModel.loadReviews(hotelId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "All Reviews",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1AB6B6))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (uiState.error != null) {
                    item {
                        Text(
                            text = uiState.error ?: "",
                            color = Color(0xFFFF4A4A),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                if (uiState.reviewsWithUser.isEmpty() && uiState.error == null) {
                    item {
                        Text(
                            text = "Chưa có review nào",
                            color = Color(0xFF757575),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }

                items(uiState.reviewsWithUser) { reviewWithUser ->
                    val timeLabel = formatReviewDateTime(reviewWithUser.review.createdAt)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE0F2F1)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val photo = reviewWithUser.user?.photoUrl
                                    if (photo != null && photo.isNotBlank()) {
                                        coil.compose.AsyncImage(
                                            model = photo,
                                            contentDescription = reviewWithUser.userName,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                        )
                                    } else {
                                        val label = reviewWithUser.userName.take(1)
                                        Text(
                                            text = label,
                                            color = Color(0xFF1AB6B6),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reviewWithUser.userName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF212121),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeLabel,
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(5) { idx ->
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = if (idx < reviewWithUser.review.rating) Color(0xFFFFC107) else Color(0xFFE0E0E0),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = reviewWithUser.review.comment,
                                fontSize = 14.sp,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

private fun formatReviewDateTime(createdAt: com.google.firebase.Timestamp?): String {
    if (createdAt == null) return ""
    val date = createdAt.toDate()
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val dd = String.format("%02d", cal.get(java.util.Calendar.DAY_OF_MONTH))
    val mm = String.format("%02d", cal.get(java.util.Calendar.MONTH) + 1)
    val yyyy = cal.get(java.util.Calendar.YEAR)
    val hh = String.format("%02d", cal.get(java.util.Calendar.HOUR_OF_DAY))
    val min = String.format("%02d", cal.get(java.util.Calendar.MINUTE))
    return "$dd-$mm-$yyyy $hh:$min"
}
