package com.example.chillstay.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chillstay.R

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.state.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item { Header() }
            item { SearchBar() }
            item { FilterTabs() }
            item { PopularHotels() }
            item { HotelPromotions() }
            item { VIPStatus() }
            item { RecentlyBooked() }
            item { TravelAchievements() }
            item { ContinuePlanning() }
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            BottomNavigationBar()
        }
    }
}

@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF1AB5B5))
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Chillstay",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SearchBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp)
                .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = "Filter",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun FilterTabs() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Popular", "Recommended", "Trending")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
    ) {
        items(tabs.size) { index ->
            FilterChip(
                selected = selectedTab == index,
                title = tabs[index],
                onClick = { selectedTab = index }
            )
        }
    }
}

@Composable
private fun FilterChip(selected: Boolean, title: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF1AB5B5) else Color.Transparent,
        border = if (selected) null else BorderStroke(0.5.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.height(42.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else Color(0xFF757575)
            )
        }
    }
}

@Composable
private fun PopularHotels() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            item {
                HotelCard(
                    name = "Luxury Resort & Spa",
                    location = "Miami, USA - 20 km to beach",
                    price = "$299",
                    rating = "4.8",
                    imageRes = R.drawable.img_hotel_1
                )
            }
            item {
                HotelCard(
                    name = "Ocean View Hotel",
                    location = "Bali, Indonesia",
                    price = "$199",
                    rating = "4.9",
                    imageRes = R.drawable.img_hotel_2
                )
            }
        }
    }
}

@Composable
private fun HotelCard(
    name: String,
    location: String,
    price: String,
    rating: String,
    imageRes: Int
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(340.dp)
            .shadow(12.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (index < 4) Color(0xFF1AB55C) else Color(0xFFE0E0E0)
                        )
                        if (index < 4) {
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = rating,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1AB55C)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = price,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1AB5B5)
                    )
                    Text(
                        text = "/night",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                }
            }
        }
    }
}

@Composable
private fun HotelPromotions() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Hotel Promotions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            PromotionCard(
                title = "INTERNATIONAL\nDEALS",
                emoji = "✈️",
                backgroundColor = Color(0xFF87CEEB)
            )
            PromotionCard(
                title = "DOMESTIC\nDEALS",
                emoji = "☀️",
                backgroundColor = Color(0xFFFFB347)
            )
        }
    }
}

@Composable
private fun PromotionCard(title: String, emoji: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(188.dp)
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = emoji,
            fontSize = 36.sp,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun VIPStatus() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "VIP status",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFF5F5), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "As an ChillStayVIP Bronze member,\nyou receive a larger discount",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFFCC8540), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⭐ VIP\nBronze",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentlyBooked() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently Booked",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            TextButton(onClick = { }) {
                Text(
                    text = "See All",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1AB5B5)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BookingCard(
                name = "City Center Hotel",
                location = "New York, USA - 20 km to center",
                rating = "4.9",
                reviews = "45",
                originalPrice = "$700",
                discount = "28%",
                finalPrice = "$599",
                imageRes = R.drawable.img_booking_1
            )
            BookingCard(
                name = "Beachfront Villa",
                location = "Maldives - 10 km to center",
                rating = "4.9",
                reviews = "45",
                originalPrice = "$700",
                discount = "28%",
                finalPrice = "$599",
                imageRes = R.drawable.img_booking_2
            )
        }
    }
}

@Composable
private fun BookingCard(
    name: String,
    location: String,
    rating: String,
    reviews: String,
    originalPrice: String,
    discount: String,
    finalPrice: String,
    imageRes: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(266.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFA))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(161.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = location,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(
                            text = rating,
                            fontSize = 12.sp,
                            color = Color(0xFF1AB5B5)
                        )
                        Text(
                            text = " ($reviews reviews)",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFBDFFA9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$100 applied",
                            fontSize = 8.sp,
                            color = Color(0xFF30B538)
                        )
                    }
                    Row {
                        Text(
                            text = originalPrice,
                            fontSize = 8.sp,
                            color = Color(0xFF757575),
                            textDecoration = TextDecoration.LineThrough
                        )
                        Text(
                            text = " - $discount",
                            fontSize = 8.sp,
                            color = Color(0xFFFF4A4A)
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = finalPrice,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1AB5B5)
                        )
                        Text(
                            text = "/night",
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelAchievements() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Travel achievements",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementCard(
                value = "1/1",
                label = "Navigator",
                modifier = Modifier.weight(1f)
            )
            AchievementCard(
                value = "4",
                label = "Cities",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AchievementCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        }
    }
}

@Composable
private fun ContinuePlanning() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = "Continue planning your trip",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F7))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFF6B6B), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🏨", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "BeachFront Villa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📅 Sep 28 - Sep 29 👥 2",
                        fontSize = 14.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 33.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationItem(
                icon = R.drawable.ic_home_active,
                label = "Home",
                selected = true
            )
            NavigationItem(
                icon = R.drawable.ic_deal_inactive,
                label = "Deal",
                selected = false
            )
            NavigationItem(
                icon = R.drawable.ic_saved_inactive,
                label = "Saved",
                selected = false
            )
            NavigationItem(
                icon = R.drawable.ic_trips_inactive,
                label = "My trips",
                selected = false
            )
            NavigationItem(
                icon = R.drawable.ic_profile_inactive,
                label = "Profile",
                selected = false
            )
        }
    }
}

@Composable
private fun NavigationItem(icon: Int, label: String, selected: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (selected) Color(0xFF38E0E0) else Color.White,
                    RoundedCornerShape(8.dp)
                )
                .then(
                    if (!selected) Modifier.border(
                        0.5.dp,
                        Color(0xFF757575),
                        RoundedCornerShape(8.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = if (selected) Color.White else Color(0xFF757575)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color(0xFF212121) else Color(0xA6000000)
        )
    }
}