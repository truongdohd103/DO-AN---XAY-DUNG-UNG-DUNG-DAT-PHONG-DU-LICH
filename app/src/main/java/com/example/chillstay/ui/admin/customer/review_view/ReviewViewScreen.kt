package com.example.chillstay.ui.admin.customer.review_view

import android.annotation.SuppressLint
import android.graphics.Color.parseColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.ui.components.MarqueeText
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewViewScreen(
    reviewId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: ReviewViewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)

    LaunchedEffect(reviewId) {
        viewModel.onEvent(ReviewViewIntent.LoadReview(reviewId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ReviewViewEffect.NavigateBack -> onNavigateBack()
                is ReviewViewEffect.ShowError -> {
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
                    IconButton(onClick = { viewModel.onEvent(ReviewViewIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Review View",
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
                        CircularProgressIndicator(color = tealColor)
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
                                onClick = { viewModel.onEvent(ReviewViewIntent.LoadReview(reviewId)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tealColor
                                )
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
                            .padding(horizontal = 18.dp)
                            .padding(top = 28.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(33.dp)
                    ) {
                        // User Info
                        val level = uiState.vipStatus?.level ?: VipLevel.BRONZE
                        uiState.user?.let { user ->
                            UserInfoSection(user = user, level = level)
                        }

                        // Review Date
                        uiState.review?.let { review ->
                            ReviewDateSection(timestamp = review.createdAt.toDate())
                        }

                        // Rating
                        uiState.review?.let { review ->
                            RatingSection(rating = review.rating)
                        }

                        // Content
                        uiState.review?.let { review ->
                            ContentSection(content = review.comment)
                        }

                        // Hotel
                        uiState.hotel?.let { hotel ->
                            HotelSection(hotel = hotel, tealColor = tealColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserInfoSection(user: User, level : VipLevel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D9488),
                            Color(0xFF14B8A6)
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (user.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = user.fullName.split(" ").mapNotNull { it.firstOrNull() }.take(2)
                        .joinToString("").uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // User Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = user.fullName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Badge
                if (user.isActive) {
                    Badge(
                        text = "Active",
                        backgroundColor = Color(0xFF065F46),
                        textColor = Color(0xFFD1FAE5)
                    )
                }

                // VIP Badge
                VipLevelBadge(level)
            }
        }
    }
}

fun String.toColor(): Color = Color(this.toColorInt())

@Composable
fun VipLevelBadge(level: VipLevel) {
    val background = level.color.toColor()
    val textColor = when (level) {
        VipLevel.GOLD -> Color(0xFF92400E)
        VipLevel.DIAMOND -> Color(0xFF1E40AF)
        VipLevel.PLATINUM -> Color(0xFF3F3F46)
        VipLevel.SILVER -> Color(0xFF57534E)
        VipLevel.BRONZE -> Color(0xFF9A3412)
    }

    Badge(
        text = "${level.displayName} Member",
        backgroundColor = background,
        textColor = textColor
    )
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
fun ReviewDateSection(timestamp: Date) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Review Date",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        Text(
            text = "📅 ${dateFormat.format(timestamp)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF212121),
            lineHeight = 24.sp
        )
    }
}

@Composable
fun RatingSection(rating: Int) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Rating",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        RatingStars(rating = rating)
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            if (index < rating) {
                // Filled star (yellow)
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Star $index",
                    tint = Color(0xFFFBC40D),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                // Empty star (outlined)
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Star $index",
                    tint = Color(0xFF000000).copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "$rating.0",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1AB6B6)
        )
    }
}

@Composable
fun ContentSection(content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Content",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7F8), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = content,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF212121),
                lineHeight = 24.sp
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HotelSection(hotel: Hotel, tealColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Hotel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(266.dp)
                .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Images
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val images = hotel.imageUrl
                    if (images.isNotEmpty()) {
                        AsyncImage(
                            model = images[0],
                            contentDescription = hotel.name,
                            modifier = Modifier
                                .weight(1f)
                                .height(159.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (images.size > 1) {
                            AsyncImage(
                                model = images[1],
                                contentDescription = hotel.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(159.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Hotel Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MarqueeText(
                        text = hotel.name,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        textSize = 16.sp,
                        textColor = Color(0xFF212121)
                        )

                        Text(
                            text = "${hotel.city}, ${hotel.country}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 21.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star),
                                    contentDescription = null,
                                    tint = Color(0xFFFBC40D),
                                    modifier = Modifier.size(9.dp)
                                )
                            }

                            Text(
                                text = String.format("%.1f", hotel.rating),
                                fontSize = 12.sp,
                                color = tealColor,
                                lineHeight = 18.sp
                            )

                            Text(
                                text = "(${hotel.numberOfReviews} reviews)",
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Price Section
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val originalPrice = hotel.minPrice ?: 0.0
                        val discountPercent = 28
                        val discountAmount = 100.0
                        val discountedPrice = BigDecimal.valueOf(originalPrice - discountAmount)
                            .setScale(0, RoundingMode.HALF_UP)
                            .toDouble()

                        // Discount badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFBCFEA8), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$$discountAmount applied",
                                fontSize = 8.sp,
                                color = Color(0xFF31B439),
                                lineHeight = 18.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$$originalPrice/night",
                                fontSize = 8.sp,
                                color = Color(0xFF757576),
                                textDecoration = TextDecoration.LineThrough,
                                lineHeight = 18.sp
                            )

                            Text(
                                text = "- $discountPercent%",
                                fontSize = 8.sp,
                                color = Color(0xFFFF4A4A),
                                lineHeight = 18.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$$discountedPrice",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = tealColor,
                                lineHeight = 24.sp
                            )

                            Text(
                                text = "/night",
                                fontSize = 11.sp,
                                color = Color(0xFF757575),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}