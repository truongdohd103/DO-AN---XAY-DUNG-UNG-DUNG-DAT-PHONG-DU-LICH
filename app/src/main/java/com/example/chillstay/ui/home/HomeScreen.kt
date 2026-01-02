package com.example.chillstay.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.example.chillstay.domain.model.Hotel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.example.chillstay.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHotelClick: (String) -> Unit = {},
    onVipClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSeeAllRecentClick: () -> Unit = {},
    onSeeAllContinueClick: () -> Unit = {},
    onContinueItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> },
    onRequireAuth: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is HomeEffect.ShowSuccess -> snackbarHostState.showSnackbar(effect.message)
                HomeEffect.ShowBookmarkAdded -> snackbarHostState.showSnackbar("Đã thêm vào danh sách yêu thích")
                HomeEffect.ShowBookmarkRemoved -> snackbarHostState.showSnackbar("Đã xoá khỏi danh sách yêu thích")
                HomeEffect.RequireAuthentication -> onRequireAuth()
                else -> Unit
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chillstay",
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
                    selected = uiState.selectedCategory.ordinal,
                    onSelect = { viewModel.onEvent(HomeIntent.ChangeHotelCategory(it)) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                AnimatedContent(
                    targetState = uiState.selectedCategory,
                    transitionSpec = {
                        (slideInHorizontally(animationSpec = tween(250)) + fadeIn(tween(250))) togetherWith
                        (slideOutHorizontally(animationSpec = tween(250)) + fadeOut(tween(250)))
                    },
                    label = "CategoryTransition"
                ) { selectedCategory ->
                    key(selectedCategory) {
                        if (uiState.isLoading && uiState.hotels.isEmpty()) {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                items(3) {
                                    Card(
                                        modifier = Modifier
                                            .width(240.dp)
                                            .height(280.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFF5F5F5)),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                    ) {}
                                }
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                items(
                                    items = uiState.hotels,
                                    key = { hotel -> hotel.id }
                                ) { hotel ->
                                    val imageUrl = if (hotel.imageUrl.isNotEmpty()) hotel.imageUrl[0] else ""
                                    HotelCard(
                                        title = hotel.name,
                                        location = "${hotel.city}, ${hotel.country}",
                                        price = hotel.minPrice.toString(),
                                        rating = hotel.rating.toFloat(),
                                        reviews = hotel.numberOfReviews,
                                        imageUrl = imageUrl,
                                        isBookmarked = uiState.bookmarkedHotels.contains(hotel.id),
                                        onBookmarkClick = { viewModel.onEvent(HomeIntent.ToggleBookmark(hotel.id)) },
                                        onClick = { onHotelClick(hotel.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Debug: Log UI state
            item {
                LaunchedEffect(uiState.hotels.size, uiState.isLoading) {
                    android.util.Log.d("HomeScreen", "UI State - hotels count: ${uiState.hotels.size}, isLoading: ${uiState.isLoading}")
                    if (uiState.hotels.isEmpty() && !uiState.isLoading) {
                        android.util.Log.w("HomeScreen", "⚠️ No hotels to display in UI")
                    } else if (uiState.hotels.isNotEmpty()) {
                        android.util.Log.d("HomeScreen", "Displaying ${uiState.hotels.size} hotels in UI")
                        uiState.hotels.forEachIndexed { idx, hotel ->
                            android.util.Log.d("HomeScreen", "  [$idx] ${hotel.name} - Images: ${hotel.imageUrl.size}")
                        }
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
                ContinuePlanningSection(
                    items = uiState.pendingBookings,
                    onItemClick = onContinueItemClick,
                    onViewAllClick = onSeeAllContinueClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (uiState.recentHotels.isNotEmpty()) {
                item {
                    RecentlyBookedSection(
                        hotels = uiState.recentHotels,
                        bookmarkedHotels = uiState.bookmarkedHotels,
                        onSeeAllClick = onSeeAllRecentClick,
                        onHotelClick = onHotelClick,
                        onBookmarkClick = { hotelId -> viewModel.onEvent(HomeIntent.ToggleBookmark(hotelId)) }
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
    hotels: List<Hotel> = emptyList(),
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
                val minPrice = hotel.minPrice
            val imageUrl = hotel.imageUrl.firstOrNull() ?: ""
            HotelCard(
                title = hotel.name,
                location = "${hotel.city}, ${hotel.country}",
                price = minPrice?.let { "$${it.toInt()}" },
                rating = hotel.rating.toFloat(),
                reviews = hotel.numberOfReviews,
                imageUrl = imageUrl,
                onClick = { onHotelClick(hotel.id) }
            )
        }
    }
}

@Composable
fun PopularHotelsSection(
    hotels: List<Hotel>,
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
                val imageUrl = hotel.imageUrl.firstOrNull() ?: ""
                HotelCard(
                    title = hotel.name,
                    location = "${hotel.city}, ${hotel.country}",
                    price = hotel.minPrice.toString(),
                    rating = hotel.rating.toFloat(),
                    reviews = hotel.numberOfReviews,
                    imageUrl = imageUrl,
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
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_home),
                    error = painterResource(id = R.drawable.ic_home)
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
                    color = Color(0xFF212121),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = location,
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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

@Composable
fun ContinuePlanningSection(
    items: List<PendingDisplayItem> = emptyList(),
    onItemClick: (hotelId: String, roomId: String, dateFrom: String, dateTo: String) -> Unit = { _, _, _, _ -> },
    onViewAllClick: () -> Unit = {}
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
                text = "Continue planning your trip",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            if (items.size > 3) {
                Text(
                    text = "View All",
                    fontSize = 15.62.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1AB6B6),
                    modifier = Modifier
                        .background(color = Color.Transparent, shape = RoundedCornerShape(8.dp))
                        .clickable { onViewAllClick() }
                        .padding(8.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.take(3).forEach { it ->
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
                                text = listOfNotNull(it.hotelName).joinToString(" - ").ifEmpty { "Pending booking" },
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
    hotels: List<Hotel> = emptyList(),
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
                text = "View All",
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
            hotels.take(3).forEach { hotel ->
                val imageUrl = hotel.imageUrl.firstOrNull() ?: ""
                RecentlyBookedCard(
                    title = hotel.name,
                    location = "${hotel.city}, ${hotel.country}",
                    rating = hotel.rating.toFloat(),
                    reviewsCount = hotel.numberOfReviews ?: 0,
                    originalPrice = if (hotel.minPrice != null) "$${hotel.minPrice.toInt()}/night" else "",
                    discount = if ((hotel.minPrice ?: 0.0) > 0.0) "- 5%" else "",
                    finalPrice = if (hotel.minPrice != null) "$${(hotel.minPrice * 0.95).toInt()}" else "",
                    voucherApplied = if ((hotel.minPrice ?: 0.0) > 0.0) "$${(hotel.minPrice!! * 0.05).toInt()} applied" else "",
                    imageUrl = imageUrl,
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
    reviewsCount: Int,
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
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_home),
                    error = painterResource(id = R.drawable.ic_home)
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
                            color = Color(0xFF212121),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = location,
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                                text = "(${reviewsCount} reviews)",
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

