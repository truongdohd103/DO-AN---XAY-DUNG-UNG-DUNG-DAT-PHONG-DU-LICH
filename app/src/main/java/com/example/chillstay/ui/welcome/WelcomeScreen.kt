package com.example.chillstay.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1AB6B6),
                            Color(0xFF16A3A3)
                        )
                    )
                )
        )
        
        // Main image - Using Image instead of AsyncImage for local drawable
        Image(
            painter = painterResource(id = com.example.chillstay.R.drawable.welcome),
            contentDescription = "Welcome Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(621.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.Crop
        )
        
        // Bottom gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xCC000000) // 80% opacity black
                        )
                    )
                )
        )
        
        // Auto-advance effect
        LaunchedEffect(Unit) {
            // Simulate short loading, then advance
            delay(1500)
            onGetStartedClick()
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.BottomCenter)
                .padding(bottom = 83.dp)
        ) {
            // Welcome text
            Text(
                text = "Welcome to",
                color = Color.White,
                fontSize = 22.88.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // App name
            Text(
                text = "Chillstay",
                color = Color(0xFF39E1C2),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Description
            Text(
                text = "The best hotel booking in this century to\naccompany your vacation",
                style = TextStyle(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF39E1C2),
                    strokeWidth = 3.dp
                )
            }
        }
    }
    }
}
