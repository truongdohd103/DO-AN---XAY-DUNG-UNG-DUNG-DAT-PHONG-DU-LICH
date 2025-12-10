package com.example.chillstay.ui.trip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.google.firebase.auth.FirebaseAuth
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

fun formatBookingDates(dateFrom: String, dateTo: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        
        val fromDate = inputFormat.parse(dateFrom)
        val toDate = inputFormat.parse(dateTo)
        
        if (fromDate != null && toDate != null) {
            val fromFormatted = outputFormat.format(fromDate)
            val toFormatted = outputFormat.format(toDate)
            
            // Calculate nights
            val nights = ((toDate.time - fromDate.time) / (1000 * 60 * 60 * 24)).toInt()
            
            "$fromFormatted - $toFormatted • $nights nights"
        } else {
            "$dateFrom - $dateTo"
        }
    } catch (e: Exception) {
        "$dateFrom - $dateTo"
    }
}

@Composable
fun TripTab(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp)
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF1AB6B6) else Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripScreen(
    onHotelClick: (String, Boolean) -> Unit = { _, _ -> },
    onBookingClick: (String) -> Unit = {},
    onWriteReview: (String) -> Unit = {},
    onViewBill: (String) -> Unit = {},
    onCancelBooking: (String) -> Unit = {},
    initialTab: Int = 0, // 0: PENDING, 1: COMPLETED, 2: CANCELED
    viewModel: MyTripViewModel = koinInject()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    
    // Initialize with initial tab
    LaunchedEffect(initialTab) {
        viewModel.onEvent(MyTripIntent.ChangeTab(initialTab))
    }
    
    // Load bookings when user or tab changes
    LaunchedEffect(currentUserId, uiState.selectedTab) {
        val status = when (uiState.selectedTab) {
            0 -> "PENDING"
            1 -> "COMPLETED"
            2 -> "CANCELLED"
            else -> null
        }

        if (currentUserId != null && status != null) {
            viewModel.onEvent(MyTripIntent.LoadBookings(currentUserId, status))
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
            // Tab row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    TripTab(
                        text = "Pending",
                        isSelected = uiState.selectedTab == 0,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.onEvent(MyTripIntent.ChangeTab(0)) }
                    TripTab(
                        text = "Completed",
                        isSelected = uiState.selectedTab == 1,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.onEvent(MyTripIntent.ChangeTab(1)) }
                    TripTab(
                        text = "Canceled",
                        isSelected = uiState.selectedTab == 2,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.onEvent(MyTripIntent.ChangeTab(2)) }
                }
            }
            
            // Loading indicator
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF1AB6B6)
                        )
                    }
                }
            }
            
            // Error state
            if (uiState.error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading trips",
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
                                    currentUserId?.let { userId ->
                                        val status = when (uiState.selectedTab) {
                                            0 -> "PENDING"
                                            1 -> "COMPLETED"
                                            2 -> "CANCELLED"
                                            else -> null
                                        }
                                        if (status != null) {
                                            viewModel.onEvent(MyTripIntent.RetryLoad(userId, status))
                                        }
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
            
            // Empty state or bookings list
            if (!uiState.isLoading && uiState.error == null) {
                if (uiState.bookings.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No ${when (uiState.selectedTab) {
                                        0 -> "pending"
                                        1 -> "completed"
                                        2 -> "canceled"
                                        else -> ""
                                    }} trips",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF666666)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = when (uiState.selectedTab) {
                                        0 -> "Your pending bookings will appear here"
                                        1 -> "Your completed trips will appear here"
                                        2 -> "Your canceled trips will appear here"
                                        else -> ""
                                    },
                                    fontSize = 14.sp,
                                    color = Color(0xFF999999),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.bookings.size) { index ->
                        val booking = uiState.bookings[index]
                        val room = uiState.roomMap[booking.roomId]
                        val hotel = uiState.hotelMap[booking.hotelId]
                        
                        val hotelName = hotel?.name ?: "Hotel not found"
                        val roomType = room?.detail?.name ?: room?.type ?: "Room type not available"
                        val location = if (hotel != null) "${hotel.city}, ${hotel.country}" else "Coordinate not available"
                        val hotelImageUrl = hotel?.imageUrl[0]
                        
                        val status = when (uiState.selectedTab) {
                            0 -> "PENDING"
                            1 -> "COMPLETED"
                            2 -> "CANCELLED"
                            else -> booking.status.name
                        }
                        NewTripCard(
                            hotelName = hotelName,
                            roomType = roomType,
                            location = location,
                            dates = formatBookingDates(booking.dateFrom, booking.dateTo),
                            totalPrice = booking.price.toInt(),
                            status = status,
                            hotelImageUrl = hotelImageUrl,
                            hasReview = uiState.userReviewedHotels.contains(booking.hotelId),
                            onHotelClick = { 
                                android.util.Log.d("MyTripScreen", "Trip card clicked - status: $status, bookingId: ${booking.id}")
                                when (status) {
                                    "PENDING" -> {
                                        android.util.Log.d("MyTripScreen", "Navigating to booking detail for booking: ${booking.id}")
                                        onBookingClick(booking.id)
                                    }
                                    "COMPLETED", "CANCELLED" -> {
                                        android.util.Log.d("MyTripScreen", "Navigating to hotel detail for hotel: ${booking.hotelId}")
                                        onHotelClick(booking.hotelId, true) // fromMyTrip = true
                                    }
                                }
                            },
                            onWriteReview = { onWriteReview(booking.id) },
                            onViewBill = { onViewBill(booking.id) },
                            onCancelBooking = { 
                                viewModel.onEvent(MyTripIntent.ShowCancelDialog(booking))
                            }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom navigation
            }
        }
    }
    
    // Cancel confirmation dialog
    if (uiState.showCancelDialog && uiState.bookingToCancel != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.onEvent(MyTripIntent.HideCancelDialog)
            },
            title = {
                Text(
                    text = "Cancel Booking",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel this booking? This action cannot be undone.",
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        uiState.bookingToCancel?.let { booking ->
                            viewModel.onEvent(MyTripIntent.CancelBooking(booking.id))
                            onCancelBooking(booking.id)
                        }
                        viewModel.onEvent(MyTripIntent.HideCancelDialog)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4444)
                    )
                ) {
                    Text(
                        text = "Cancel Booking",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.onEvent(MyTripIntent.HideCancelDialog)
                    }
                ) {
                    Text(text = "Keep Booking")
                }
            }
        )
    }
}

@Composable
fun NewTripCard(
    hotelName: String,
    roomType: String,
    location: String,
    dates: String,
    totalPrice: Int,
    status: String,
    hotelImageUrl: String? = null,
    hasReview: Boolean = false,
    onHotelClick: () -> Unit = {},
    onWriteReview: () -> Unit = {},
    onViewBill: () -> Unit = {},
    onCancelBooking: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(161.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onHotelClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Hotel image
            Box(
                modifier = Modifier.size(80.dp)
            ) {
                if (hotelImageUrl != null) {
                    // Real hotel image
                    AsyncImage(
                        model = hotelImageUrl,
                        contentDescription = "Hotel Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = null,
                        placeholder = null
                    )
                } else {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF666666), Color(0xFF444444))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏨",
                            fontSize = 32.sp
                        )
                    }
                }

                // Status badge on image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .background(
                            color = Color(0xE6666666),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Hotel name (main info)
                Text(
                    text = hotelName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Room type (secondary info)
                Text(
                    text = roomType,
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Coordinate
                Text(
                    text = location,
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Dates and nights
                Text(
                    text = dates,
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Total price
                Text(
                    text = "$${totalPrice} total",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action buttons section
            Column(
                modifier = Modifier.width(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF666666),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons based on status
                when (status) {
                    "COMPLETED" -> {
                        // Write/Edit Review button
                        Button(
                            onClick = onWriteReview,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB6B6)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (hasReview) "Edit Review" else "Write Review",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // View Bill button
                        OutlinedButton(
                            onClick = onViewBill,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(29.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF999999)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF666666)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = "View Bill",
                                color = Color(0xFF999999),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    "PENDING" -> {
                        // Cancel Booking button
                        OutlinedButton(
                            onClick = onCancelBooking,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(27.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFFF4444)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4444)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Cancel Booking",
                                color = Color(0xFFFF4444),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    "CANCELLED" -> {
                        // Rebook button
                        Button(
                            onClick = onHotelClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(27.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1AB6B6)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Rebook",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
