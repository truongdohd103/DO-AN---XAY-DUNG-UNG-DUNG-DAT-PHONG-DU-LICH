package com.example.chillstay.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun MyTripScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
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

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TripCard(
                    hotelName = "Grand Hotel Saigon",
                    location = "Ho Chi Minh City, Vietnam",
                    dates = "Dec 15-18, 2024 • 3 nights",
                    totalPrice = 320,
                    status = "Confirmed",
                    statusColor = Color(0xFF1AB6B6),
                    gradientColors = listOf(Color(0xFF1AB6B6), Color(0xFF159999))
                )
            }
            item {
                TripCard(
                    hotelName = "Fusion Resort Phu Quoc",
                    location = "Phu Quoc Island, Vietnam",
                    dates = "Nov 20-25, 2024 • 5 nights",
                    totalPrice = 850,
                    status = "Completed",
                    statusColor = Color(0xFF666666),
                    gradientColors = listOf(Color(0xFF666666), Color(0xFF444444))
                )
            }
            item {
                Button(
                    onClick = { /* TODO: Load more trips */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(43.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF1AB6B6)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1AB6B6)),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(text = "Load More Trips", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
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
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp),
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