package com.example.chillstay.ui.voucher

import androidx.compose.foundation.background
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
fun VoucherScreen(
    onBackClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            VoucherCard(
                title = "UP TO\n5% OFF",
                description = "Save up to $800 on hotel bookings",
                promoCode = "AGODADEAL5",
                expiresIn = "3 days left",
                gradientColors = listOf(Color(0xFF87CEEB), Color(0xFF4169E1))
            )
        }
        item {
            VoucherCard(
                title = "UP TO\n8% OFF",
                description = "Save up to $1,000 on hotel bookings",
                promoCode = "SAVE8NOW",
                expiresIn = "3 days left",
                gradientColors = listOf(Color(0xFF1AB6B6), Color(0xFF159999))
            )
        }
        item {
            VoucherCard(
                title = "DOMESTIC DEALS",
                description = "Enjoy special prices at local hotels and resorts.",
                promoCode = "Apply now",
                expiresIn = "",
                gradientColors = listOf(Color(0xFFFFB347), Color(0xFFFF8C00))
            )
        }
        item {
            VoucherCard(
                title = "INTERNATIONAL DEALS",
                description = "Enjoy special prices at international hotels and\nresorts.",
                promoCode = "Apply now",
                expiresIn = "",
                gradientColors = listOf(Color(0xFF87CEEB), Color(0xFF6A5ACD))
            )
        }
    }
}

@Composable
fun VoucherCard(
    title: String,
    description: String,
    promoCode: String,
    expiresIn: String,
    gradientColors: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(brush = Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = description,
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (promoCode == "Apply now") promoCode else "Promo Code: $promoCode",
                    color = Color(0xFF9E9E9E),
                    fontSize = 14.sp
                )
                if (expiresIn.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = expiresIn,
                        color = Color(0xFF1AB6B6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}