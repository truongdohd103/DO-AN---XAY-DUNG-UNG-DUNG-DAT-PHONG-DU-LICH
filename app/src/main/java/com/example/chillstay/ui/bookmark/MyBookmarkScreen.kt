package com.example.chillstay.ui.bookmark

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.chillstay.R
import com.example.chillstay.domain.model.Hotel
import com.google.firebase.auth.FirebaseAuth
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookmarkScreen(
    viewModel: MyBookmarkViewModel = koinInject(),
    onBackClick: () -> Unit = {},
    onHotelClick: (String) -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUserId) {
        if (currentUserId != null) {
            viewModel.onEvent(MyBookmarkIntent.LoadBookmarks(currentUserId))
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
                                viewModel.onEvent(MyBookmarkIntent.RemoveBookmark("", hotel.id))
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onHotelClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column {
            // Image section - responsive height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp)
                    .aspectRatio(16f / 9f) // Maintain aspect ratio
            ) {
                AsyncImage(
                    model = hotel.imageUrl[0],
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_home),
                    error = painterResource(id = R.drawable.ic_home)
                )
                
                // Heart icon for remove bookmark
                IconButton(
                    onClick = onRemoveBookmark,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            color = Color(0xFFF44235),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove bookmark",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Content section - responsive padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = hotel.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${hotel.city}, ${hotel.country}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = hotel.rating.toString(),
                                fontSize = 12.sp,
                                color = Color(0xFF1AB6B6),
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Text(
                                text = "(${hotel.numberOfReviews})",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                    
                    // Price section - responsive layout
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 80.dp, max = 120.dp)
                    ) {
                        // Show actual hotel price if available
                        hotel.minPrice?.let { minPrice ->
                            // Voucher applied (simulate 5% discount)
                            val discount = minPrice * 0.05
                            val finalPrice = minPrice - discount
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFBCFEA8),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "$${discount.toInt()} off",
                                    fontSize = 10.sp,
                                    color = Color(0xFF31B439),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Original price and discount
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$${minPrice.toInt()}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF757575),
                                    textDecoration = TextDecoration.LineThrough
                                )
                                
                                Text(
                                    text = "-5%",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFF4A4A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Final price
                            Column(
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "$${finalPrice.toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1AB6B6)
                                )
                                
                                Text(
                                    text = "/night",
                                    fontSize = 10.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        } ?: run {
                            // Fallback if no price available
                            Text(
                                text = "Price on request",
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                textAlign = androidx.compose.ui.text.style.TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}