package com.example.chillstay.ui.bookmark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Hotel
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookmarkScreen(
    viewModel: MyBookmarkViewModel = get(),
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.handleIntent(MyBookmarkIntent.LoadBookmarks(currentUserId))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Bookmark",
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
            
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1AB6B6)
                        )
                    }
                }
            } else if (uiState.isEmpty) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No bookmarks yet",
                            fontSize = 16.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            } else {
                items(uiState.hotels.size) { index ->
                    val hotel = uiState.hotels[index]
                    BookmarkHotelCard(
                        hotel = hotel,
                        onHotelClick = { onHotelClick(hotel.id) },
                        onRemoveBookmark = { 
                            currentUserId?.let { userId ->
                                viewModel.handleIntent(MyBookmarkIntent.RemoveBookmark("", hotel.id))
                            }
                        }
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
fun BookmarkHotelCard(
    hotel: Hotel,
    onHotelClick: () -> Unit = {},
    onRemoveBookmark: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(266.dp)
            .padding(horizontal = 21.dp, vertical = 8.dp)
            .clickable { onHotelClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column {
            // Image section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
            ) {
                AsyncImage(
                    model = hotel.imageUrl,
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Heart icon for remove bookmark
                IconButton(
                    onClick = onRemoveBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(15.dp)
                        .size(20.dp)
                        .background(
                            color = Color(0xFFF44235),
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove bookmark",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            
            // Content section
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
                            text = hotel.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${hotel.city}, ${hotel.country}",
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
                                text = hotel.rating.toString(),
                                fontSize = 12.sp,
                                color = Color(0xFF1AB6B6)
                            )
                            
                            Text(
                                text = "(${hotel.numberOfReviews} reviews)",
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
                                text = "$100 applied",
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
                                text = "$700/night",
                                fontSize = 8.sp,
                                color = Color(0xFF757575),
                                textDecoration = TextDecoration.LineThrough
                            )
                            
                            Text(
                                text = "- 28%",
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
                                text = "$599",
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