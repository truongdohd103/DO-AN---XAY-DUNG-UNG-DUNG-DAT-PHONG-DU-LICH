package com.example.chillstay.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    bookingId: String,
    onBackClick: () -> Unit = {},
    viewModel: ReviewViewModel = koinInject()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    
    // Load booking details when screen opens
    LaunchedEffect(bookingId) {
        viewModel.onEvent(ReviewIntent.LoadBookingDetails(bookingId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditing) "Edit Review" else "Write Review",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1AB6B6)
                    )
                }
            }
            
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error loading review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF4444)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            fontSize = 14.sp,
                            color = Color(0xFF999999),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                viewModel.onEvent(ReviewIntent.RetryLoad)
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            uiState.isSubmitted -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Success",
                            tint = Color(0xFF1AB6B6),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Review Submitted!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1AB6B6)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Thank you for your feedback",
                            fontSize = 16.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onBackClick
                        ) {
                            Text("Back to Trips")
                        }
                    }
                }
            }
            
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Booking info card
                    BookingInfoCard(
                        booking = uiState.booking,
                        hotel = uiState.hotel,
                        isEligible = uiState.isEligible
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Rating section
                    Text(
                        text = "Rate your experience",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { 
                                    viewModel.onEvent(ReviewIntent.UpdateRating(index + 1))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star ${index + 1}",
                                    tint = if (index < uiState.rating) Color(0xFFFFD700) else Color(0xFFCCCCCC),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Comment section
                    Text(
                        text = "Write a comment (optional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF333333)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = uiState.comment,
                        onValueChange = { 
                            viewModel.onEvent(ReviewIntent.UpdateComment(it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text("Share your experience...")
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1AB6B6),
                            unfocusedBorderColor = Color(0xFFCCCCCC)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Submit button
                    Button(
                        onClick = { 
                            viewModel.onEvent(ReviewIntent.SubmitReview(uiState.rating, uiState.comment))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = uiState.rating > 0 && !uiState.isSubmitting && uiState.isEligible,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1AB6B6)
                        )
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = if (uiState.isEditing) "Update Review" else "Submit Review",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingInfoCard(
    booking: com.example.chillstay.domain.model.Booking?,
    hotel: com.example.chillstay.domain.model.Hotel?,
    isEligible: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            // Hotel image
            val imageUrl = hotel?.imageUrl?.firstOrNull() ?: ""
            AsyncImage(
                model = imageUrl,
                contentDescription = hotel?.name ?: "Hotel",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Booking details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Hotel name
                Text(
                    text = hotel?.name ?: "Unknown Hotel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Room name (if available)
                booking?.room?.let { room ->
                    Text(
                        text = room.name,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                // Dates
                booking?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 ${it.dateFrom} - ${it.dateTo}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Guests and rooms
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👥 ${it.guests} guests",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                        if (it.rooms > 1) {
                            Text(
                                text = " • ${it.rooms} rooms",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Eligibility status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEligible) "✓ Eligible to review" else "✗ Not eligible",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isEligible) Color(0xFF1AB6B6) else Color(0xFFFF4444)
                    )
                }
            }
        }
    }
}
