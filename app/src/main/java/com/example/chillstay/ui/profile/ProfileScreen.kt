package com.example.chillstay.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
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
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {}
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "demo@chillstay.com"
    val userName = currentUser?.displayName ?: "User"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
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
                .background(Color.White),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Profile header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFF5F5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profile",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(40.dp)
                    )
                    // Camera icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color.Black, CircleShape)
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_photo_camera),
                            contentDescription = "Camera",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = userName,
                    color = Color(0xFF212121),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = userEmail,
                    color = Color(0xFF424242),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(
                color = Color(0xFFE0E0E0),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
        
        // Menu items
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_person),
                title = "Edit Profile",
                onClick = { /* TODO: Navigate to edit profile */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_payment),
                title = "Payment method",
                onClick = { /* TODO: Navigate to payment */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_notifications),
                title = "Notifications",
                onClick = { /* TODO: Navigate to notifications */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_star),
                title = "My Reviews",
                onClick = { /* TODO: Navigate to reviews */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_language),
                title = "Language",
                onClick = { /* TODO: Navigate to language settings */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_lock),
                title = "Change password",
                onClick = { /* TODO: Navigate to change password */ }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_help),
                title = "Help",
                onClick = { /* TODO: Navigate to help */ }
            )
        }
        
        item {
            // Dark theme toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_dark_mode),
                    contentDescription = "Dark Theme",
                    tint = Color(0xFF1AB6B6),
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(Modifier.width(24.dp))
                
                Text(
                    text = "Dark Theme",
                    color = Color(0xFF424242),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF1AB6B6),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE0E0E0)
                    )
                )
            }
        }
        
        item {
            // Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .padding(bottom = 80.dp), // Thêm padding bottom để tránh bị che bởi bottom nav
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_exit_to_app),
                    contentDescription = "Logout",
                    tint = Color(0xFFF75555),
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(Modifier.width(24.dp))
                
                TextButton(onClick = { 
                    onLogout()
                }) {
                    Text(
                        text = "Logout",
                        color = Color(0xFFF75555),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    }
}

@Composable
fun ProfileMenuItem(
    icon: Painter,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            tint = Color(0xFF1AB6B6),
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(Modifier.width(24.dp))
        
        Text(
            text = title,
            color = Color(0xFF424242),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        
        Icon(
            painter = painterResource(id = R.drawable.ic_keyboard_arrow_right),
            contentDescription = "Arrow",
            tint = Color(0xFF9E9E9E),
            modifier = Modifier.size(20.dp)
        )
    }
}