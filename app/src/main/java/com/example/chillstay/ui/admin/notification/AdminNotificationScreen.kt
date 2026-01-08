package com.example.chillstay.ui.admin.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.Notification
import org.koin.androidx.compose.koinViewModel
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNotificationScreen(
    onBackClick: () -> Unit,
    viewModel: AdminNotificationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is AdminNotificationEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Manage Notifications", 
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(AdminNotificationIntent.OpenCreateDialog) },
                containerColor = Color(0xFF1AB6B6),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Notification")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.notifications.isEmpty()) {
                Text(
                    text = "No notifications found",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        AdminNotificationItem(notification = notification)
                    }
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateNotificationDialog(
            uiState = uiState,
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
fun AdminNotificationItem(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(notification.createdAt.toDate()),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.message,
                fontSize = 14.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "To: ${notification.userId}",
                fontSize = 12.sp,
                color = Color(0xFF1AB6B6)
            )
        }
    }
}

@Composable
fun CreateNotificationDialog(
    uiState: AdminNotificationUiState,
    onEvent: (AdminNotificationIntent) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onEvent(AdminNotificationIntent.DismissCreateDialog) },
        title = { Text("Create Notification") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.newNotificationTitle,
                    onValueChange = { onEvent(AdminNotificationIntent.TitleChanged(it)) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = uiState.newNotificationMessage,
                    onValueChange = { onEvent(AdminNotificationIntent.MessageChanged(it)) },
                    label = { Text("Message") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { 
                        onEvent(AdminNotificationIntent.SendToAllChanged(!uiState.isSendToAll)) 
                    }
                ) {
                    Checkbox(
                        checked = uiState.isSendToAll,
                        onCheckedChange = { onEvent(AdminNotificationIntent.SendToAllChanged(it)) }
                    )
                    Text("Send to All Users")
                }

                if (!uiState.isSendToAll) {
                    OutlinedTextField(
                        value = uiState.newNotificationUserId,
                        onValueChange = { onEvent(AdminNotificationIntent.UserIdChanged(it)) },
                        label = { Text("User ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onEvent(AdminNotificationIntent.SendNotification) },
                enabled = !uiState.isSending
            ) {
                if (uiState.isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(AdminNotificationIntent.DismissCreateDialog) }
            ) {
                Text("Cancel")
            }
        }
    )
}
