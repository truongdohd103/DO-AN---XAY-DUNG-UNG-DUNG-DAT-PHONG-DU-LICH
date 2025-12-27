package com.example.chillstay.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    hotelId: String = "",
    onBackClick: () -> Unit = {},
    onBookNowClick: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onOpenGalleryClick: (roomId: String) -> Unit = {}
) {
    val viewModel: RoomViewModel = koinInject()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(hotelId) { if (hotelId.isNotEmpty()) viewModel.onEvent(RoomIntent.LoadRooms(hotelId)) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_home),
                            contentDescription = "Hotel",
                            tint = Color(0xFF828282),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Column {
                            Text(
                                text = uiState.hotelName ?: "",
                                color = Color(0xFF212121),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Sep 21 - Sep 22, 3 guests",
                                color = Color(0xFF757575),
                                fontSize = 12.sp
                            )
                        }
                    }
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
                // Filter section
                FilterSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(uiState.rooms.size) { index ->
                val room = uiState.rooms[index]
                val roomId = null
                RoomCard(
                    roomId = room.id,
                    name = room.name,
                    imageUrl = room.gallery?.thisRoom[0],
                    area = room.area.let { "${it.toInt()} m²" },
                    maxAdults = "Max ${room.capacity} adults",
                    doubleBed = room.doubleBed,
                    singleBed = room.singleBed,
                    amenities = room.feature,
                    breakfastPrice = room.breakfastPrice.toInt(),
                    originalPrice = room.price.toInt(),
                    discount = room.discount.toInt(),
                    finalPrice = room.price.toInt(),
                    roomsLeft = roomLeft(room.id, "2025-12-25", "2025-12-28"),
                    imagesCount = (room.gallery?.totalCount ?: 1),
                    onBookNowClick = { roomId, dateFrom, dateTo, _ ->
                        onBookNowClick(hotelId, roomId, dateFrom, dateTo)
                    },
                    onOpenGalleryClick = { onOpenGalleryClick(room.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

private fun roomLeft(roomId: String, dateFrom: String, dateTo: String): Int {
    return 4
}

@Composable
fun FilterSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Filter",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilterChip(
                    text = "Free breakfast",
                    isSelected = true,
                    onClick = { }
                )
                
                FilterChip(
                    text = "Non-smoking",
                    isSelected = true,
                    onClick = { }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FilterChip(
                text = "Pay later",
                isSelected = true,
                onClick = { }
            )
        }
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (isSelected) Color(0xFFE8F8F8) else Color.Transparent,
                RoundedCornerShape(24.dp)
            )
            .border(
                2.dp,
                if (isSelected) Color(0xFF1AB6B6) else Color.Transparent,
                RoundedCornerShape(24.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color(0xFF1AB6B6) else Color(0xFF757575),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun RoomCard(
    roomId: String,
    name: String,
    imageUrl: String?,
    area: String,
    maxAdults: String,
    doubleBed: Int,
    singleBed: Int,
    amenities: List<String>,
    breakfastPrice: Int? = null,
    originalPrice: Int,
    discount: Int,
    finalPrice: Int,
    roomsLeft: Int,
    isSoldOut: Boolean = false,
    imagesCount: Int = 1,
    onBookNowClick: (String, String, String, String) -> Unit,
    onOpenGalleryClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = name,
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Room image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onOpenGalleryClick(roomId) }
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Room Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                if (isSoldOut) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                Color(0xFFFFD322),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Sold out!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (roomsLeft == 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .background(
                                Color(0xFFFF5722),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Our last 1!",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Image count
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .clickable { onOpenGalleryClick(roomId) }
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
                            text = imagesCount.toString(),
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Room details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = area,
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
                
                Text(
                    text = maxAdults,
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
                
                Text(
                    text = doubleBed.toString(),
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )

                Text(
                    text = singleBed.toString(),
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amenities
            Column {
                Text(
                    text = "Facilities",
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                amenities.forEach { amenity ->
                    Text(
                        text = amenity,
                        color = Color(0xFF757575),
                        fontSize = 13.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Booking info - always show for consistency
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (breakfastPrice != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Breakfast",
                                tint = Color(0xFF1AB65C),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = breakfastPrice.toString(),
                                color = Color(0xFF1AB65C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (originalPrice > 0) {
                                Text(
                                    text = "$$originalPrice",
                                    color = Color(0xFF757575),
                                    fontSize = 12.sp,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }
                            
                            if (discount > 0) {
                                Text(
                                    text = "-$discount%",
                                    color = Color(0xFFFF5722),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Text(
                            text = "$$finalPrice",
                            color = Color(0xFFFF5722),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Only show booking section if not sold out
                    if (!isSoldOut) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Rooms left: $roomsLeft",
                                    color = Color(0xFF212121),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (roomsLeft == 1) {
                                    Text(
                                        text = "Our last 1!",
                                        color = Color(0xFFFF5722),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Button(
                                onClick = { 
                                    onBookNowClick(roomId, "2024-12-25", "2024-12-28", "2024-12-30")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1AB6B6)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(121.dp)
                            ) {
                                Text(
                                    text = "Book now!",
                                    color = Color.White,
                                    fontSize = 13.89.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}