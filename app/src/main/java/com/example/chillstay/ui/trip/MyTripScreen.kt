package com.example.chillstay.ui.trip

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import com.google.firebase.auth.FirebaseAuth
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    onHotelClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val getUserBookingsUseCase: GetUserBookingsUseCase = koinInject()
    val getRoomByIdUseCase: GetRoomByIdUseCase = koinInject()
    val getHotelByIdUseCase: GetHotelByIdUseCase = koinInject()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var roomMap by remember { mutableStateOf<Map<String, Room>>(emptyMap()) }
    var hotelMap by remember { mutableStateOf<Map<String, Hotel>>(emptyMap()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Trips",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->

    LaunchedEffect(currentUserId, selectedTab) {
        val status = when (selectedTab) {
            1 -> "UPCOMING"
            2 -> "COMPLETED"
            3 -> "CANCELLED"
            else -> null
        }
        if (currentUserId != null) {
            runCatching { getUserBookingsUseCase(currentUserId, status) }
                .onSuccess { result ->
                    val list = when (result) {
                        is com.example.chillstay.core.common.Result.Success -> result.data
                        is com.example.chillstay.core.common.Result.Error -> emptyList()
                    }
                    bookings = list
                    // Fetch rooms and hotels for display
                    val rooms = mutableMapOf<String, Room>()
                    val hotels = mutableMapOf<String, Hotel>()
                    for (b in list) {
                        runCatching { getRoomByIdUseCase(b.roomId) }
                            .onSuccess { r ->
                                when (r) {
                                    is com.example.chillstay.core.common.Result.Success -> rooms[r.data.id] = r.data
                                    is com.example.chillstay.core.common.Result.Error -> {}
                                }
                            }
                    }
                    // For each room, fetch hotel if hotelId exists in Room detail (assume detail may include)
                    // NOTE: Room model không có hotelId hiện tại -> cần lấy từ schema rooms: field `hotelId`
                    // Đã bổ sung hotelId vào Room model; có thể fetch Hotel khi cần.
                    roomMap = rooms
                    hotelMap = hotels
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Trip",
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
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TripTab(text = "All", isSelected = selectedTab == 0) { selectedTab = 0 }
                        TripTab(text = "Upcoming", isSelected = selectedTab == 1) { selectedTab = 1 }
                        TripTab(text = "Completed", isSelected = selectedTab == 2) { selectedTab = 2 }
                        TripTab(text = "Cancelled", isSelected = selectedTab == 3) { selectedTab = 3 }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            
            items(bookings.size) { index ->
                val booking = bookings[index]
                val room = roomMap[booking.roomId]
                val hotelName = room?.detail?.name ?: booking.roomId
                val location = ""
                TripCard(
                    hotelName = hotelName,
                    location = location,
                    dates = "${booking.dateFrom} - ${booking.dateTo}",
                    totalPrice = booking.price.toInt(),
                    status = booking.status.name,
                    statusColor = Color(0xFF1AB6B6),
                    gradientColors = listOf(Color(0xFF1AB6B6), Color(0xFF159999)),
                    onClick = onHotelClick
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
            }
        }
    }
}


@Composable
fun TripTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF1AB6B6) else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.White
    val borderColor = if (isSelected) Color(0xFF1AB6B6) else Color(0xFF666666)

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(43.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(25.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TripCard(
    hotelName: String,
    location: String,
    dates: String,
    totalPrice: Int,
    status: String,
    statusColor: Color,
    gradientColors: List<Color>,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1AB6B6))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = hotelName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(gradientColors),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = location,
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                text = dates,
                color = Color(0xFFCCCCCC),
                fontSize = 14.sp
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${totalPrice}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(
                    onClick = { /* TODO: View details */ }
                ) {
                    Text(
                        text = "View Details",
                        color = Color(0xFF1AB6B6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    }
}

@Composable
fun TripTab(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFF1AB6B6) else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF1AB6B6) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF757575),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TripCard(
    hotelName: String,
    location: String,
    dates: String,
    totalPrice: Int,
    status: String,
    statusColor: Color,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = hotelName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                if (location.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = location,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = dates,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
                
                Spacer(Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: $$totalPrice",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "View Details",
                        color = Color(0xFF1AB6B6),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}