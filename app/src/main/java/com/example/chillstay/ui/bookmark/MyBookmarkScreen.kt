package com.example.chillstay.ui.bookmark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.chillstay.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.get

@Composable
fun MyBookmarkScreen(
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {}
) {
    val getUserBookmarksUseCase: GetUserBookmarksUseCase = get()
    val getHotelByIdUseCase: GetHotelByIdUseCase = get()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var hotels by remember { mutableStateOf<List<Hotel>>(emptyList()) }

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            runCatching { getUserBookmarksUseCase(currentUserId) }
                .onSuccess { result ->
                    val bookmarks = when (result) {
                        is com.example.chillstay.core.common.Result.Success -> result.data
                        is com.example.chillstay.core.common.Result.Error -> emptyList()
                    }
                    val fetched = mutableListOf<Hotel>()
                    for (bm in bookmarks) {
                        runCatching { getHotelByIdUseCase(bm.hotelId) }
                            .onSuccess { res ->
                                when (res) {
                                    is com.example.chillstay.core.common.Result.Success -> fetched.add(res.data)
                                    is com.example.chillstay.core.common.Result.Error -> {}
                                }
                            }
                    }
                    hotels = fetched
                }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 21.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(hotels.size) { index ->
            val hotel = hotels[index]
            RecentlyBookedCard(
                imageUrl = hotel.imageUrl,
                name = hotel.name,
                location = "${hotel.city}, ${hotel.country}",
                rating = hotel.rating,
                reviews = hotel.numberOfReviews,
                // Giá/voucher hiện không có trong schema bookmark/hotel: ẩn hoặc set 0
                originalPrice = 0,
                discountedPrice = 0,
                voucherApplied = 0,
                onClick = { onHotelClick(hotel.id) }
            )
        }
    }
}

@Composable
fun RecentlyBookedCard(
    imageUrl: String,
    name: String,
    location: String,
    rating: Double,
    reviews: Int,
    originalPrice: Int,
    discountedPrice: Int,
    voucherApplied: Int,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(266.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Recently Booked Hotel Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(159.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = name,
                color = Color(0xFF212121),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = location,
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Rating Star",
                        tint = Color(0xFFFBC40D),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = rating.toString(),
                    color = Color(0xFF1AB6B6),
                    fontSize = 12.sp
                )
                Text(
                    text = " ($reviews reviews)",
                    color = Color(0xFF757575),
                    fontSize = 12.sp
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "Voucher Applied",
                            tint = Color(0xFF31B439),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$$voucherApplied applied",
                            color = Color(0xFF31B439),
                            fontSize = 8.sp
                        )
                    }
                    Row {
                        Text(
                            text = "$$originalPrice/night",
                            color = Color(0xFF757575),
                            fontSize = 8.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = " -${((originalPrice - discountedPrice).toFloat() / originalPrice * 100).toInt()}%",
                            color = Color(0xFFFF4A4A),
                            fontSize = 8.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$$discountedPrice",
                            color = Color(0xFF1AB6B6),
                            fontSize = 15.50.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/night",
                            color = Color(0xFF757575),
                            fontSize = 11.06.sp
                        )
                    }
                }
                // Heart icon for bookmark (filled)
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = "Bookmark",
                    tint = Color(0xFFF44235),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}