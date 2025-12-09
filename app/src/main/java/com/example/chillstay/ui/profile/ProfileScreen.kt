package com.example.chillstay.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.example.chillstay.R
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import com.example.chillstay.ui.profile.ProfileIntent
import com.example.chillstay.ui.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onEvent: (ProfileIntent) -> Unit,
    onLogoutClick: () -> Unit
) {
    var isDarkTheme by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val currentUser = state.currentUser
    val userEmail = currentUser?.email ?: "demo@chillstay.com"
    val userName = currentUser?.fullName?.takeIf { it.isNotBlank() } ?: "User"

    LaunchedEffect(Unit) {
        onEvent(ProfileIntent.LoadProfile)
    }
    LaunchedEffect(state.profileMessage) {
        val msg = state.profileMessage
        if (msg != null && msg.contains("success", ignoreCase = true)) {
            showEditDialog = false
        }
    }
    
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
        // Removed inline Profile details; it will be shown in a dialog when editing

        // Menu items
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_person),
                title = "Edit Profile",
                onClick = { showEditDialog = true }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_payment),
                title = "Payment method",
                onClick = { onEvent(ProfileIntent.OpenPaymentMethod) }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_notifications),
                title = "Notifications",
                onClick = { onEvent(ProfileIntent.OpenNotifications) }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_star),
                title = "My Reviews",
                onClick = { onEvent(ProfileIntent.OpenMyReviews) }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_language),
                title = "Language",
                onClick = { onEvent(ProfileIntent.OpenLanguage) }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_lock),
                title = "Change password",
                onClick = { onEvent(ProfileIntent.OpenChangePassword) }
            )
        }
        
        item {
            ProfileMenuItem(
                icon = painterResource(id = R.drawable.ic_help),
                title = "Help",
                onClick = { onEvent(ProfileIntent.OpenHelp) }
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
                
                TextButton(onClick = onLogoutClick) {
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

    if (showEditDialog) {
        ProfileEditDialog(
            state = state,
            onEvent = onEvent,
            onDismiss = { showEditDialog = false }
        )
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
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
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

@Composable
fun ProfileEditDialog(
    state: ProfileUiState,
    onEvent: (ProfileIntent) -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Edit Profile",
                    color = Color(0xFF212121),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = state.profileFullName,
                    onValueChange = { onEvent(ProfileIntent.ProfileFullNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Full name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1AB6B6),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.profileGender,
                    onValueChange = { onEvent(ProfileIntent.ProfileGenderChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Gender (Male / Female / Other)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1AB6B6),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.profileDateOfBirth,
                    onValueChange = { onEvent(ProfileIntent.ProfileDateOfBirthChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Date of birth (yyyy-MM-dd)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1AB6B6),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = state.profilePhotoUrl,
                    onValueChange = { onEvent(ProfileIntent.ProfilePhotoUrlChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Photo URL") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1AB6B6),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (state.profileMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.profileMessage.contains("success", ignoreCase = true)) {
                                Color(0xFFE8F5E8)
                            } else {
                                Color(0xFFFFEBEE)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = state.profileMessage,
                            modifier = Modifier.padding(16.dp),
                            color = if (state.profileMessage.contains("success", ignoreCase = true)) {
                                Color(0xFF2E7D32)
                            } else {
                                Color(0xFFC62828)
                            },
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(text = "Cancel", color = Color(0xFF424242))
                    }

                    Button(
                        onClick = { onEvent(ProfileIntent.SaveProfile) },
                        modifier = Modifier.weight(1f),
                        enabled = state.currentUser != null && !state.isProfileLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB6B6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.isProfileLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Save changes",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
