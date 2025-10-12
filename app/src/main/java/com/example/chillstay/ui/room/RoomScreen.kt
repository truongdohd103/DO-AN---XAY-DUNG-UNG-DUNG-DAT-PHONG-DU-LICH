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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomScreen(
    onBackClick: () -> Unit = {},
    onBookNowClick: () -> Unit = {}
) {
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
                                text = "Luxury Resort & Spa",
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
            
            item {
                // Basic Triple Room
                RoomCard(
                    title = "Basic Triple Room",
                    imageUrl = "https://placehold.co/374x250",
                    size = "29 m²/312 ft²",
                    maxAdults = "Max 3 adults",
                    beds = "3 single beds",
                    amenities = listOf(
                        "📶 Internet access – wireless",
                        "🚭 Non-smoking",
                        "📺 TV",
                        "❄️ Air conditioning"
                    ),
                    breakfastInfo = "Breakfast available ($25 / person)",
                    refundable = "Non-refundable (Low price!)",
                    paymentType = "Pay at hotel",
                    originalPrice = 799,
                    discount = 28,
                    finalPrice = 599,
                    roomsLeft = 4,
                    onBookNowClick = onBookNowClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Interior Double or Twin Room
                RoomCard(
                    title = "Interior Double or Twin Room",
                    subtitle = "Double with Extra Bed",
                    imageUrl = "https://placehold.co/334x223",
                    size = "20 m²/215 ft²",
                    maxAdults = "Max 3 adults",
                    beds = "2 double bed",
                    amenities = listOf(
                        "📶 Internet access – wireless",
                        "🚭 Non-smoking",
                        "📺 TV",
                        "❄️ Air conditioning"
                    ),
                    breakfastInfo = "Breakfast available ($25 / person)",
                    refundable = "Non-refundable (Low price!)",
                    paymentType = "Pay at hotel",
                    originalPrice = 899,
                    discount = 15,
                    finalPrice = 764,
                    roomsLeft = 0,
                    isSoldOut = true,
                    onBookNowClick = onBookNowClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
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
    title: String,
    subtitle: String = "",
    imageUrl: String,
    size: String,
    maxAdults: String,
    beds: String,
    amenities: List<String>,
    breakfastInfo: String,
    refundable: String,
    paymentType: String,
    originalPrice: Int,
    discount: Int,
    finalPrice: Int,
    roomsLeft: Int,
    isSoldOut: Boolean = false,
    onBookNowClick: () -> Unit
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
                text = title,
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = subtitle,
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Room image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                } else if (roomsLeft > 0) {
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
                            text = "Our last $roomsLeft!",
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
                            text = "67",
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
                    text = size,
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
                
                Text(
                    text = maxAdults,
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
                
                Text(
                    text = beds,
                    color = Color(0xFF212121),
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amenities
            Column {
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
                    if (breakfastInfo.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Breakfast",
                                tint = Color(0xFF1AB65C),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = breakfastInfo,
                                color = Color(0xFF1AB65C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (refundable.isNotEmpty()) {
                        Text(
                            text = refundable,
                            color = Color(0xFF212121),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    if (paymentType.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Payment",
                                tint = Color(0xFF1AB65C),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = paymentType,
                                color = Color(0xFF1AB65C),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Text(
                        text = "See details",
                        color = Color(0xFF1AB6B6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { /* TODO: Show details */ }
                    )
                    
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
                                    text = "1 Rooms",
                                    color = Color(0xFF212121),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                if (roomsLeft > 0) {
                                    Text(
                                        text = "Our last $roomsLeft!",
                                        color = Color(0xFFFF5722),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            
                            Icon(
                                painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                                contentDescription = "Dropdown",
                                tint = Color(0xFF212121),
                                modifier = Modifier.size(14.dp)
                            )
                            
                            Button(
                                onClick = onBookNowClick,
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

