package com.example.chillstay.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Booking
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHotelClick: (String) -> Unit = {},
    onVipClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSeeAllRecentClick: () -> Unit = {},
    onContinueItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> }
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            item {
                // Header
                HeaderSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Search Bar
                SearchBar(onClick = onSearchClick)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Category Tabs (horizontal scroll)
                CategoryTabs(
                    selected = uiState.selectedCategory,
                    onSelect = { viewModel.handleIntent(HomeIntent.ChangeHotelCategory(it)) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Horizontal list of hotels per selected category
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(uiState.hotels.size) { index ->
                        val hotel = uiState.hotels[index]
                        val minPrice = hotel.rooms.minByOrNull { it.price }?.price
                        HotelCard(
                            title = hotel.name,
                            location = "${hotel.city}, ${hotel.country}",
                            price = minPrice?.let { "$${it.toInt()}" },
                            rating = hotel.rating.toFloat(),
                            reviews = hotel.numberOfReviews,
                            imageUrl = hotel.imageUrl,
                            isBookmarked = uiState.bookmarkedHotels.contains(hotel.id),
                            onBookmarkClick = { viewModel.handleIntent(HomeIntent.ToggleBookmark(hotel.id)) },
                            onClick = { onHotelClick(hotel.id) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Removed PopularHotelsSection; list above handles vertical scroll
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // VIP Status
                VipStatusSection(onClick = onVipClick)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Continue Planning from pending bookings
                
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                var pending by remember(userId) { mutableStateOf(listOf<PendingDisplayItem>()) }
                LaunchedEffect(userId) {
                    if (userId != null) {
                        val db = FirebaseFirestore.getInstance()
                        val docs = db.collection("bookings")
                            .whereEqualTo("userId", userId)
                            .whereEqualTo("status", "PENDING")
                            .limit(10)
                            .get()
                            .await()
                        val items = mutableListOf<PendingDisplayItem>()
                        for (d in docs.documents) {
                            val dateFrom = d.getString("dateFrom") ?: continue
                            val dateTo = d.getString("dateTo") ?: continue
                            val guests = (d.getLong("guests") ?: 0).toInt()
                            val createdAt = d.getDate("createdAt")
                            val hotelId = d.getString("hotelId")
                            val roomId = d.getString("roomId")
                            val hotelName = try {
                                val hid = d.getString("hotelId")
                                if (!hid.isNullOrEmpty()) {
                                    val hdoc = db.collection("hotels").document(hid).get().await()
                                    hdoc.getString("name")
                                } else null
                            } catch (_: Exception) { null }
                            items.add(PendingDisplayItem(hotelName, dateFrom, dateTo, guests, createdAt, hotelId ?: "", roomId ?: ""))
                        }
                        pending = items.sortedByDescending { it.createdAt }
                    }
                }
                ContinuePlanningSection(items = pending, onItemClick = onContinueItemClick)
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                item {
                    // Recently Booked (user-specific)
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    var recentHotels by remember(userId) { mutableStateOf(listOf<Hotel>()) }
                    LaunchedEffect(userId) {
                        if (userId != null) {
                            val db = FirebaseFirestore.getInstance()
                    val bookingDocs = db.collection("bookings")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("status", "CONFIRMED") // Only confirmed bookings
                        .limit(10)
                        .get()
                        .await()
                    val hotelIds = bookingDocs.documents
                        .sortedByDescending { it.getDate("createdAt") }
                        .mapNotNull { it.getString("hotelId") }
                        .distinct()
                        .take(5)
                            val hotels = mutableListOf<Hotel>()
                            for (hid in hotelIds) {
                                val doc = db.collection("hotels").document(hid).get().await()
                                doc.toObject(Hotel::class.java)?.copy(id = doc.id)?.let { hotels.add(it) }
                            }
                            recentHotels = hotels
                        }
                    }
                    RecentlyBookedSection(
                        hotels = recentHotels,
                        bookmarkedHotels = uiState.bookmarkedHotels,
                        onSeeAllClick = onSeeAllRecentClick,
                        onHotelClick = onHotelClick,
                        onBookmarkClick = { hotelId -> viewModel.handleIntent(HomeIntent.ToggleBookmark(hotelId)) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1AB6B6),
                        Color(0xFF16A3A3)
                    )
                )
            )
    ) {
        Text(
            text = "Chillstay",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 21.dp)
        )
    }
}

@Composable
fun SearchBar(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
            .padding(horizontal = 24.dp)
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF828282),
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Search",
                color = Color(0xFF757575),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Filter",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun CategoryTabs(selected: Int, onSelect: (Int) -> Unit) {
    val tabs = listOf("Popular", "Recommended", "Trending")
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tabs.size) { index ->
            val tab = tabs[index]
            Box(
                modifier = Modifier
                    .background(
                        color = if (selected == index) Color(0xFF1AB6B6) else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected == index) Color(0xFF1AB65C) else Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelect(index) }
                    .padding(horizontal = 21.dp, vertical = 13.dp)
            ) {
                Text(
                    text = tab,
                    color = if (selected == index) Color.White else Color(0xFF757575),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HotelCardsSection(
    hotels: List<com.example.chillstay.domain.model.Hotel> = emptyList(),
    onHotelClick: (String) -> Unit = {}
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        items(hotels.size) { index ->
            val hotel = hotels[index]
            // Compute minimum room price if rooms are embedded; otherwise, omit price
            val minPrice = hotel.rooms.minByOrNull { it.price }?.price
            HotelCard(
                title = hotel.name,
                location = "${hotel.city}, ${hotel.country}",
                price = minPrice?.let { "$${it.toInt()}" },
                rating = hotel.rating.toFloat(),
                reviews = hotel.numberOfReviews,
                imageUrl = hotel.imageUrl,
                onClick = { onHotelClick(hotel.id) }
            )
        }
    }
}

@Composable
fun PopularHotelsSection(
    hotels: List<com.example.chillstay.domain.model.Hotel>,
    bookmarkedHotels: Set<String> = emptySet(),
    onHotelClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Popular Hotels",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            items(hotels.size) { index ->
                val hotel = hotels[index]
                HotelCard(
                    title = hotel.name,
                    location = "${hotel.city}, ${hotel.country}",
                    price = hotel.rooms.minByOrNull { it.price }?.price?.let { "$${it.toInt()}" },
                    rating = hotel.rating.toFloat(),
                    reviews = hotel.numberOfReviews,
                    imageUrl = hotel.imageUrl,
                    isBookmarked = bookmarkedHotels.contains(hotel.id),
                    onBookmarkClick = { onBookmarkClick(hotel.id) },
                    onClick = { onHotelClick(hotel.id) }
                )
            }
        }
    }
}

@Composable
fun HotelCard(
    title: String,
    location: String,
    price: String?,
    rating: Float?,
    reviews: Int?,
    imageUrl: String,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(339.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Bookmark button
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .size(24.dp)
                        .background(
                            color = if (isBookmarked) Color(0xFFF44235) else Color.White,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = if (isBookmarked) Color.White else Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = title,
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
                
                if (rating != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rating.toString(),
                            fontSize = 13.67.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1AB65C)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Row {
                            repeat(5) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = Color(0xFFFBC40D),
                                    modifier = Modifier.size(9.dp)
                                )
                            }
                        }

                        if (reviews != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${reviews})",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (!price.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = price,
                            fontSize = 19.38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1AB6B6)
                        )
                        
                        Text(
                            text = "/night",
                            fontSize = 12.91.sp,
                            color = Color(0xFF757575),
                            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HotelPromotionsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Hotel Promotions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(2) { index ->
                PromotionCard(
                    title = if (index == 0) "INTERNATIONAL\nDEALS" else "DOMESTIC\nDEALS",
                    gradient = if (index == 0) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF87CEEB),
                                Color(0xFF6A5ACD)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFB347),
                                Color(0xFFFF8C00)
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PromotionCard(
    title: String,
    gradient: Brush
) {
    Box(
        modifier = Modifier
            .width(366.dp)
            .height(188.dp)
            .background(
                brush = gradient,
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun VipStatusSection(onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
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
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFF5F5),
                            Color(0xFFFFE4E1)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { onClick() }
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
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFCD853F),
                                    Color(0xFFD2B48C)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⭐ VIP\nBronze",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class PendingDisplayItem(
    val hotelName: String?,
    val dateFrom: String,
    val dateTo: String,
    val guests: Int,
    val createdAt: java.util.Date?,
    val hotelId: String,
    val roomId: String
)

@Composable
fun ContinuePlanningSection(items: List<PendingDisplayItem> = emptyList(), onItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> }) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = "Continue planning your trip",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { it ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(it.hotelId, it.roomId, it.dateFrom, it.dateTo) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B6B),
                                            Color(0xFFFF8E53)
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🏨",
                                fontSize = 20.sp
                            )
                        }
                        Column {
                            Text(
                                text = it.hotelName ?: "Pending booking",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "📅 ${it.dateFrom} - ${it.dateTo} 👥 ${it.guests}",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentlyBookedSection(
    hotels: List<com.example.chillstay.domain.model.Hotel> = emptyList(), 
    bookmarkedHotels: Set<String> = emptySet(),
    onSeeAllClick: () -> Unit = {}, 
    onHotelClick: (String) -> Unit = {},
    onBookmarkClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
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
            
            Text(
                text = "See All",
                fontSize = 15.62.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1AB6B6),
                modifier = Modifier
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSeeAllClick() }
                    .padding(8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            hotels.forEach { hotel ->
                RecentlyBookedCard(
                    title = hotel.name,
                    location = "${hotel.city}, ${hotel.country}",
                    rating = hotel.rating.toFloat(),
                    originalPrice = if (hotel.minPrice != null) "$${hotel.minPrice?.toInt()}/night" else "",
                    discount = if ((hotel.minPrice ?: 0.0) > 0.0) "- 5%" else "",
                    finalPrice = if (hotel.minPrice != null) "$${(hotel.minPrice!! * 0.95).toInt()}" else "",
                    voucherApplied = if ((hotel.minPrice ?: 0.0) > 0.0) "$${(hotel.minPrice!! * 0.05).toInt()} applied" else "",
                    imageUrl = hotel.imageUrl,
                    isBookmarked = bookmarkedHotels.contains(hotel.id),
                    onBookmarkClick = { onBookmarkClick(hotel.id) },
                    onClick = { onHotelClick(hotel.id) }
                )
            }
        }
    }
}

@Composable
fun RecentlyBookedCard(
    title: String,
    location: String,
    rating: Float,
    originalPrice: String,
    discount: String,
    finalPrice: String,
    voucherApplied: String,
    imageUrl: String,
    isBookmarked: Boolean = false,
    onBookmarkClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(266.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.clickable { onClick() }) {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Bookmark button
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(15.dp)
                        .size(20.dp)
                        .background(
                            color = if (isBookmarked) Color(0xFFF44235) else Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
                        tint = if (isBookmarked) Color.White else Color(0xFF757575),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
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
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star",
                                        tint = Color(0xFFFBC40D),
                                        modifier = Modifier.size(9.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = rating.toString(),
                                fontSize = 12.sp,
                                color = Color(0xFF1AB6B6)
                            )
                            
                            Text(
                                text = "(45 reviews)",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    
                    // Price section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Voucher applied
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFBCFEA8),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = voucherApplied,
                                fontSize = 8.sp,
                                color = Color(0xFF31B439)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Original price and discount
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = originalPrice,
                                fontSize = 8.sp,
                                color = Color(0xFF757575),
                                textDecoration = TextDecoration.LineThrough
                            )
                            
                            Text(
                                text = discount,
                                fontSize = 8.sp,
                                color = Color(0xFFFF4A4A)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Final price
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = finalPrice,
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1AB6B6)
                            )
                            
                            Text(
                                text = "/night",
                                fontSize = 11.06.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigation(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(horizontal = 33.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = true
            )
            
            BottomNavItem(
                icon = Icons.Default.LocationOn,
                label = "Deal",
                isSelected = false
            )
            
            BottomNavItem(
                icon = Icons.Default.FavoriteBorder,
                label = "Saved",
                isSelected = false
            )
            
            BottomNavItem(
                icon = Icons.Default.Star,
                label = "My trips",
                isSelected = false
            )
            
            BottomNavItem(
                icon = Icons.Default.Favorite,
                label = "Profile",
                isSelected = false
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = if (isSelected) {
                        Color(0xFF1AB6B6)
                    } else {
                        Color.Transparent
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = if (isSelected) 0.dp else 1.dp,
                    color = if (isSelected) Color.Transparent else Color(0xFF757575),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color(0xFF757575),
                modifier = Modifier.size(18.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF212121) else Color(0x66000000)
        )
    }
}
