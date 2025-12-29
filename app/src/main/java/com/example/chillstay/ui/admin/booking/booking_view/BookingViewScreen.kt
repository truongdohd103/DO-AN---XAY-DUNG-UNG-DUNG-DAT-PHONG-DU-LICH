package com.example.chillstay.ui.admin.booking.booking_view

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.domain.model.*
import com.example.chillstay.ui.admin.customer.review_view.UserInfoSection
import com.example.chillstay.ui.components.MarqueeText
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingViewScreen(
    bookingId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BookingViewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)

    LaunchedEffect(bookingId) {
        viewModel.onEvent(BookingViewIntent.LoadBooking(bookingId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BookingViewEffect.NavigateBack -> onNavigateBack()
                is BookingViewEffect.ShowError -> {
                    // Handle error if needed
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = tealColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onEvent(BookingViewIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Booking View",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 30.sp
                    )
                }
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = tealColor)
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { viewModel.onEvent(BookingViewIntent.LoadBooking(bookingId)) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = tealColor
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(top = 30.dp, bottom = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(30.dp)
                    ) {
                        // User Info
                        val level = uiState.vipStatus?.level ?: VipLevel.BRONZE
                        uiState.user?.let { user ->
                            UserInfoSection(user = user, level = level)
                        }

                        // Booking Status
                        uiState.booking?.let { booking ->
                            BookingStatusSection(booking = booking)
                        }

                        // Booking Date & Stay Details
                        uiState.booking?.let { booking ->
                            BookingDetailsSection(booking = booking)
                        }

                        // Hotel
                        uiState.hotel?.let { hotel ->
                            HotelSection(hotel = hotel, tealColor = tealColor)
                        }

                        // Payment Summary
                        uiState.booking?.let { booking ->
                            PaymentSummarySection(booking = booking, tealColor = tealColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingStatusSection(booking: Booking) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Booking Status",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        val (backgroundColor, borderColor, statusColor, statusText) = when (booking.status) {
            BookingStatus.CONFIRMED -> Tuple4(
                Color(0xFFECFDF5),
                Color(0xFF10B981),
                Color(0xFF065F46),
                "Confirmed"
            )
            BookingStatus.PENDING -> Tuple4(
                Color(0xFFFEF3C7),
                Color(0xFFF59E0B),
                Color(0xFF92400E),
                "Pending"
            )
            BookingStatus.CANCELLED -> Tuple4(
                Color(0xFFFEE2E2),
                Color(0xFFEF4444),
                Color(0xFF991B1B),
                "Cancelled"
            )
            BookingStatus.COMPLETED -> Tuple4(
                Color(0xFFDBEAFE),
                Color(0xFF3B82F6),
                Color(0xFF1E40AF),
                "Completed"
            )
            BookingStatus.CHECKED_IN -> Tuple4(
                Color(0xFFE0E7FF),
                Color(0xFF6366F1),
                Color(0xFF4338CA),
                "Checked In"
            )
            BookingStatus.CHECKED_OUT -> Tuple4(
                Color(0xFFE0E7FF),
                Color(0xFF6366F1),
                Color(0xFF4338CA),
                "Checked Out"
            )
            BookingStatus.REFUNDED -> Tuple4(
                Color(0xFFFCE7F3),
                Color(0xFFEC4899),
                Color(0xFF9F1239),
                "Refunded"
            )
        }

        Box(
            modifier = Modifier
                .width(366.dp)
                .height(70.dp)
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✓",
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Column {
                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "Booking ID: #${booking.id.takeLast(10)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = borderColor
                    )
                }
            }
        }
    }
}

@Composable
fun BookingDetailsSection(booking: Booking) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Booking Date
        Text(
            text = "Booking Date",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        val bookingDate = booking.createdAt.toDate()
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        Text(
            text = "📅 ${dateFormat.format(bookingDate)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF212121),
            lineHeight = 24.sp
        )

        // Stay Details
        Text(
            text = "Stay Details",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(top = 10.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7F8), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Check-in
                DetailRow(
                    label = "Check-in",
                    value = formatDate(booking.dateFrom)
                )

                // Check-out
                DetailRow(
                    label = "Check-out",
                    value = formatDate(booking.dateTo)
                )

                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                // Room Type
                DetailRow(
                    label = "Room Type",
                    value = booking.room?.name ?: "Deluxe Ocean View"
                )

                // Rooms
                DetailRow(
                    label = "Rooms",
                    value = booking.rooms.toString()
                )

                // Guests
                DetailRow(
                    label = "Guests",
                    value = "${booking.adults} Adults" + if (booking.children > 0) ", ${booking.children} Child${if (booking.children > 1) "ren" else ""}" else ""
                )
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )

        Text(
            text = value,
            fontSize = if (label == "Check-in" || label == "Check-out") 15.sp else 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
    }
}

fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HotelSection(hotel: Hotel, tealColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Hotel",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(266.dp)
                .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Images
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val images = hotel.imageUrl
                    if (images.isNotEmpty()) {
                        AsyncImage(
                            model = images[0],
                            contentDescription = hotel.name,
                            modifier = Modifier
                                .weight(1f)
                                .height(159.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (images.size > 1) {
                            AsyncImage(
                                model = images[1],
                                contentDescription = hotel.name,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(159.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                // Hotel Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MarqueeText(
                            text = hotel.name,
                            modifier = Modifier.fillMaxWidth(0.9f),
                            textSize = 16.sp,
                            textColor = Color(0xFF212121)
                        )

                        Text(
                            text = "${hotel.city}, ${hotel.country}",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            lineHeight = 21.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star),
                                    contentDescription = null,
                                    tint = Color(0xFFFBC40D),
                                    modifier = Modifier.size(9.dp)
                                )
                            }

                            Text(
                                text = String.format("%.1f", hotel.rating),
                                fontSize = 12.sp,
                                color = tealColor,
                                lineHeight = 18.sp
                            )

                            Text(
                                text = "(${hotel.numberOfReviews} reviews)",
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Price Section
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val originalPrice = hotel.minPrice ?: 0.0
                        val discountAmount = 100.0
                        val discountPercent = 28
                        val discountedPrice = BigDecimal.valueOf(originalPrice - discountAmount)
                            .setScale(0, RoundingMode.HALF_UP)
                            .toDouble()

                        // Discount badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFBCFEA8), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$$discountAmount applied",
                                fontSize = 8.sp,
                                color = Color(0xFF31B439),
                                lineHeight = 18.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$$originalPrice/night",
                                fontSize = 8.sp,
                                color = Color(0xFF757575),
                                textDecoration = TextDecoration.LineThrough,
                                lineHeight = 18.sp
                            )

                            Text(
                                text = "- $discountPercent%",
                                fontSize = 8.sp,
                                color = Color(0xFFFF4A4A),
                                lineHeight = 18.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "$$discountedPrice",
                                fontSize = 15.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = tealColor,
                                lineHeight = 24.sp
                            )

                            Text(
                                text = "/night",
                                fontSize = 11.sp,
                                color = Color(0xFF757575),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummarySection(booking: Booking, tealColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Payment Summary",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7F8), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // Calculate nights
                val nights = calculateNights(booking.dateFrom, booking.dateTo)
                val pricePerNight = if (nights > 0) booking.price / nights else booking.price

                // Room price
                PaymentRow(
                    label = "$${pricePerNight.toInt()} × $nights nights",
                    value = "$$${booking.price.toInt()}"
                )

                // Service fee
                PaymentRow(
                    label = "Service fee",
                    value = "$$${booking.serviceFee.toInt()}"
                )

                // Taxes
                PaymentRow(
                    label = "Taxes",
                    value = "$$${booking.taxes.toInt()}"
                )

                HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))

                // Total Paid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Paid",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Text(
                        text = "$$${booking.totalPrice.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = tealColor
                    )
                }

                // Payment Method
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "💳 ${getPaymentMethodText(booking.paymentMethod)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )

        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )
    }
}

fun calculateNights(dateFrom: String, dateTo: String): Int {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val from = format.parse(dateFrom)
        val to = format.parse(dateTo)
        if (from != null && to != null) {
            val diff = to.time - from.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } else {
            3
        }
    } catch (_: Exception) {
        3
    }
}

fun getPaymentMethodText(method: PaymentMethod): String {
    return when (method) {
        PaymentMethod.CREDIT_CARD -> "Paid with Credit Card (****3456)"
        PaymentMethod.DEBIT_CARD -> "Paid with Debit Card (****7890)"
        PaymentMethod.DIGITAL_WALLET -> "Paid with Digital Wallet"
        PaymentMethod.BANK_TRANSFER -> "Paid with Bank Transfer"
        PaymentMethod.CASH -> "Paid with Cash"
    }
}

// Helper data class for status colors
data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)