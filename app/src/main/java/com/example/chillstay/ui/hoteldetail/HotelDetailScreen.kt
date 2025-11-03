package com.example.chillstay.ui.hoteldetail

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelDetailScreen(
    hotelId: String,
    onBackClick: () -> Unit = {},
    onChooseRoomClick: () -> Unit = {}
) {
    val viewModel: HotelDetailViewModel = koinInject()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // Load hotel details when screen opens
    LaunchedEffect(hotelId) {
        viewModel.onEvent(HotelDetailIntent.LoadHotelDetails(hotelId))
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.hotel?.name ?: "",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6).copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            BottomBar(
                price = uiState.minPrice ?: 0,
                onChooseRoomClick = onChooseRoomClick
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
                // Hotel Image with indicators
                HotelImageSection(
                    imageUrl = uiState.hotel?.imageUrl,
                    photoCount = uiState.hotel?.detail?.photoUrls?.size ?: 0
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Hotel Info
                HotelInfoSection(
                    name = uiState.hotel?.name.orEmpty(),
                    address = listOfNotNull(uiState.hotel?.city, uiState.hotel?.country).filter { it.isNotBlank() }.joinToString(", "),
                    rating = uiState.hotel?.rating ?: 0.0,
                    reviews = uiState.hotel?.numberOfReviews ?: 0
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Selling out fast warning
                SellingOutWarning()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!uiState.hotel?.detail?.description.isNullOrBlank()) {
                item {
                    // Description
                    DescriptionSection(description = uiState.hotel?.detail?.description.orEmpty())
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!uiState.hotel?.detail?.facilities.isNullOrEmpty()) {
                item {
                    // Facilities
                    FacilitiesSection(facilities = uiState.hotel?.detail?.facilities ?: emptyList())
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Location
                LocationSection(address = listOfNotNull(uiState.hotel?.city, uiState.hotel?.country).filter { it.isNotBlank() }.joinToString(", "))
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Languages spoken
                LanguagesSection()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                ReviewsSection(
                    rating = uiState.hotel?.rating ?: 0.0,
                    reviewCount = uiState.hotel?.numberOfReviews ?: 0,
                    reviewsWithUser = uiState.reviewsWithUser)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Hotel policies
                HotelPoliciesSection()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Contact property
                ContactPropertySection()
            }

            item {
                Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
            }
        }
    }
}

@Composable
fun HotelImageSection(imageUrl: String?, photoCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        AsyncImage(
            model = imageUrl ?: "https://placehold.co/414x250",
            contentDescription = "Hotel Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Image indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (index == 0) Color.White else Color.White.copy(alpha = 0.5f),
                            CircleShape
                        )
                )
            }
        }

        // Image count
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(
                    Color.Black.copy(alpha = 0.7f),
                    RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_photo_camera),
                    contentDescription = "Photos",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = photoCount.toString(),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HotelInfoSection(
    name: String,
    address: String,
    rating: Double,
    reviews: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Text(
            text = name,
            color = Color(0xFF212121),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = address,
            color = Color(0xFF757575),
            fontSize = 16.sp
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(5) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating Star",
                    tint = Color(0xFFFBC40D),
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = String.format("%.1f", rating),
                color = Color(0xFF1AB6B6),
                fontSize = 13.67.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SellingOutWarning() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
    ) {
        Column(
            modifier = Modifier.padding(13.dp)
        ) {
            Text(
                text = "🔥 Selling out fast!",
                color = Color(0xFFFF6B35),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Hurry! We're already sold out of 2 room types on your dates here!",
                color = Color(0xFF757575),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DescriptionSection(description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 23.dp)
    ) {
        Text(
            text = "Description",
            color = Color(0xFF212121),
            fontSize = 18.70.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(17.dp))

        Text(
            text = description,
            color = Color(0xFF757575),
            fontSize = 15.88.sp,
            lineHeight = 25.60.sp
        )
    }
}

@Composable
fun FacilitiesSection(facilities: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Facilities",
            color = Color(0xFF212121),
            fontSize = 18.70.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic facilities list
        val chunks = facilities.chunked(4)
        chunks.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowItems.forEach { name ->
                    FacilityItem(
                        icon = R.drawable.ic_check, // simple check icon placeholder
                        name = name
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FacilityItem(
    icon: Int,
    name: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.51.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(Color(0xFF39E1E1), Color(0xFF1AB6B6))
                    ),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = name,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = name,
            color = Color(0xFF757575),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun LocationSection(address: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Text(
            text = "Location",
            color = Color(0xFF212121),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = "https://placehold.co/71x70",
                contentDescription = "Location Map",
                modifier = Modifier
                    .size(71.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = address,
                color = Color(0xFF757575),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun LanguagesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Text(
            text = "Languages spoken",
            color = Color(0xFF212121),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Language flags and names
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageItem(
                flagUrl = "https://placehold.co/40x40",
                name = "English"
            )
            LanguageItem(
                flagUrl = "https://placehold.co/40x40",
                name = "Italian"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LanguageItem(
                flagUrl = "https://placehold.co/40x40",
                name = "Chinese"
            )
            LanguageItem(
                flagUrl = "https://placehold.co/40x40",
                name = "Vietnamese"
            )
        }
    }
}

@Composable
fun LanguageItem(
    flagUrl: String,
    name: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = flagUrl,
            contentDescription = "Flag",
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = name,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ReviewsSection(
    rating: Double,
    reviewCount: Int,
    reviewsWithUser: List<ReviewWithUser> = emptyList()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews",
                color = Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See all",
                color = Color(0xFF1AB6B6),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* TODO: Navigate to all reviews */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = String.format("%.1f", rating),
                color = Color(0xFF1AB6B6),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "$reviewCount reviews",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Review cards - Hiển thị với user name thật
        if (reviewsWithUser.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(reviewsWithUser) { reviewWithUser ->
                    ReviewCard(
                        name = reviewWithUser.userName,
                        location = "Recently",
                        rating = reviewWithUser.review.rating,
                        comment = reviewWithUser.review.comment,
                        photoUrl = reviewWithUser.userPhotoUrl
                    )
                }
            }
        } else {
            // Empty state nếu không có reviews
            Text(
                text = "No reviews yet",
                color = Color(0xFF757575),
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun ReviewCard(
    name: String,
    location: String,
    rating: Int,
    comment: String,
    photoUrl: String? = null
) {
    Card(
        modifier = Modifier.width(287.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User avatar - Hiển thị ảnh nếu có, fallback là chữ cái đầu
                if (photoUrl != null && photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = name,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.ic_home),
                        error = painterResource(id = R.drawable.ic_home)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1AB6B6), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (name.isNotEmpty()) name.take(1).uppercase() else "👤",
                            fontSize = 16.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = name,
                        color = Color(0xFF212121),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = location,
                        color = Color(0xFF757575),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row {
                    repeat(rating) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = "Star",
                            tint = Color(0xFFFBC40D),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment,
                color = Color(0xFF424242),
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun HotelPoliciesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hotel policies",
                color = Color(0xFF212121),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "See all",
                color = Color(0xFF1AB6B6),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { /* TODO: Navigate to all policies */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Children and extra beds",
            color = Color(0xFF212121),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Infant 0-2 year(s)",
            color = Color(0xFF212121),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Stay for free if using existing bedding. Note, if you need a cot, it may incur an extra charge and is subject to availability.",
            color = Color(0xFF757575),
            fontSize = 14.sp,
            lineHeight = 19.60.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Children 3-12 year(s)",
            color = Color(0xFF212121),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Must use an extra bed",
            color = Color(0xFF757575),
            fontSize = 14.sp,
            lineHeight = 19.60.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Guests 13 years and older are considered adults.",
            color = Color(0xFF757575),
            fontSize = 14.sp,
            lineHeight = 19.60.sp
        )
    }
}

@Composable
fun ContactPropertySection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE0E0E0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Contact property",
                color = Color(0xFF1AB6B6),
                fontSize = 15.25.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BottomBar(
    price: Int,
    onChooseRoomClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(103.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Start at",
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$$price",
                        color = Color(0xFF1AB6B6),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "/per night",
                        color = Color(0xFF757575),
                        fontSize = 14.sp
                    )
                }
            }

            Button(
                onClick = onChooseRoomClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Choose my room",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}