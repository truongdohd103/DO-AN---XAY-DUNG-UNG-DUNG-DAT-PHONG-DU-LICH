package com.example.chillstay.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.res.painterResource
import com.example.chillstay.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.koin.compose.koinInject
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import org.koin.androidx.compose.get
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    hotelId: String = "",
    roomId: String = "",
    dateFrom: String = "",
    dateTo: String = "",
    onBackClick: () -> Unit = {}
) {
    val viewModel: BookingViewModel = koinInject()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    
    // Load booking data when screen opens
    LaunchedEffect(hotelId, roomId, dateFrom, dateTo) {
        if (hotelId.isNotEmpty() && roomId.isNotEmpty() && dateFrom.isNotEmpty() && dateTo.isNotEmpty()) {
            val fromDate = java.time.LocalDate.parse(dateFrom)
            val toDate = java.time.LocalDate.parse(dateTo)
            viewModel.handleIntent(BookingIntent.LoadBookingData(hotelId, roomId, fromDate, toDate))
        }
    }
    var cardNumber by remember { mutableStateOf("1234 5678 9012 3456") }
    var expiryDate by remember { mutableStateOf("MM/YY") }
    var cvv by remember { mutableStateOf("123") }
    var cardholderName by remember { mutableStateOf("John Doe") }
    
    // Show loading state
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF1AB6B6))
        }
        return
    }
    
    // Show error state
    if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    color = Color.Red,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (hotelId.isNotEmpty() && roomId.isNotEmpty() && dateFrom.isNotEmpty() && dateTo.isNotEmpty()) {
                            val fromDate = java.time.LocalDate.parse(dateFrom)
                            val toDate = java.time.LocalDate.parse(dateTo)
                            viewModel.handleIntent(BookingIntent.LoadBookingData(hotelId, roomId, fromDate, toDate))
                        }
                    }
                ) {
                    Text("Retry")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Book Your Stay",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "${uiState.hotel?.name ?: "Hotel"} - ${uiState.hotel?.city ?: "City"}, ${uiState.hotel?.country ?: "Country"}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            item {
                // Stay Details
                StayDetailsSection(
                    hotel = uiState.hotel,
                    room = uiState.room,
                    dateFrom = uiState.dateFrom,
                    dateTo = uiState.dateTo,
                    rooms = uiState.rooms,
                    adults = uiState.adults,
                    children = uiState.children,
                    onRoomsChange = { viewModel.handleIntent(BookingIntent.UpdateGuests(uiState.adults, uiState.children, it)) },
                    onAdultsChange = { viewModel.handleIntent(BookingIntent.UpdateGuests(it, uiState.children, uiState.rooms)) },
                    onChildrenChange = { viewModel.handleIntent(BookingIntent.UpdateGuests(uiState.adults, it, uiState.rooms)) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Special Requests
                SpecialRequestsSection(
                    specialRequests = uiState.specialRequests,
                    onSpecialRequestsChange = { viewModel.handleIntent(BookingIntent.UpdateSpecialRequests(it)) },
                    preferences = uiState.preferences,
                    onPreferencesChange = { viewModel.handleIntent(BookingIntent.UpdatePreferences(it)) }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Payment Method
                PaymentMethodSection(
                    selectedPaymentMethod = when (uiState.paymentMethod) {
                        com.example.chillstay.domain.model.PaymentMethod.CREDIT_CARD -> 0
                        com.example.chillstay.domain.model.PaymentMethod.DIGITAL_WALLET -> 1
                        else -> 0
                    },
                    onPaymentMethodChange = { 
                        val method = if (it == 0) com.example.chillstay.domain.model.PaymentMethod.CREDIT_CARD 
                                   else com.example.chillstay.domain.model.PaymentMethod.DIGITAL_WALLET
                        viewModel.handleIntent(BookingIntent.UpdatePaymentMethod(method))
                    },
                    cardNumber = cardNumber,
                    onCardNumberChange = { cardNumber = it },
                    expiryDate = expiryDate,
                    onExpiryDateChange = { expiryDate = it },
                    cvv = cvv,
                    onCvvChange = { cvv = it },
                    cardholderName = cardholderName,
                    onCardholderNameChange = { cardholderName = it }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Price Summary
                PriceSummarySection(
                    priceBreakdown = uiState.priceBreakdown,
                    appliedVouchers = uiState.appliedVouchers
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Book Button
                Button(
                    onClick = { 
                        if (!uiState.isCreatingBooking) {
                            viewModel.handleIntent(BookingIntent.CreateBooking)
                        }
                    },
                    enabled = !uiState.isCreatingBooking && uiState.hotel != null && uiState.room != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isCreatingBooking) Color.Gray else Color(0xFF1AB6B6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isCreatingBooking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "BOOK",
                            color = Color.White,
                            fontSize = 17.58.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StayDetailsSection(
    hotel: com.example.chillstay.domain.model.Hotel?,
    room: com.example.chillstay.domain.model.Room?,
    dateFrom: java.time.LocalDate,
    dateTo: java.time.LocalDate,
    rooms: Int,
    adults: Int,
    children: Int,
    onRoomsChange: (Int) -> Unit,
    onAdultsChange: (Int) -> Unit,
    onChildrenChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Stay Details",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Check-in and Check-out
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Check-in",
                        color = Color(0xFF757575),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = dateFrom.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Check-out",
                        color = Color(0xFF757575),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = dateTo.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        onValueChange = { },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0E0E0),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Room Type
            Column {
                Text(
                    text = "Room Type",
                    color = Color(0xFF757575),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = "${room?.detail?.name ?: room?.type ?: "Room"} - $${room?.price?.toInt() ?: 0}/night",
                    onValueChange = { },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_keyboard_arrow_down),
                            contentDescription = "Dropdown",
                            tint = Color(0xFF6B7280)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rooms counter
            CounterRow(
                label = "Rooms",
                value = rooms,
                onValueChange = onRoomsChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Adults counter
            CounterRow(
                label = "Adults",
                subtitle = "Ages 13+",
                value = adults,
                onValueChange = onAdultsChange
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Children counter
            CounterRow(
                label = "Children",
                subtitle = "Ages 2-12",
                value = children,
                onValueChange = onChildrenChange
            )
        }
    }
}

@Composable
fun CounterRow(
    label: String,
    subtitle: String = "",
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                color = Color(0xFF212121),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color(0xFF1AB6B6), CircleShape)
            ) {
                Text(
                    text = "−",
                    color = Color(0xFF1AB6B6),
                    fontSize = 18.sp
                )
            }
            
            Text(
                text = value.toString(),
                color = Color(0xFF212121),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp)
            )
            
            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color(0xFF1AB6B6), CircleShape)
            ) {
                Text(
                    text = "+",
                    color = Color(0xFF1AB6B6),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun SpecialRequestsSection(
    specialRequests: String,
    onSpecialRequestsChange: (String) -> Unit,
    preferences: com.example.chillstay.domain.model.BookingPreferences,
    onPreferencesChange: (com.example.chillstay.domain.model.BookingPreferences) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Special Requests",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional Notes
            Column {
                Text(
                    text = "Additional Notes",
                    color = Color(0xFF757575),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = specialRequests,
                    onValueChange = onSpecialRequestsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preferences
            Column {
                Text(
                    text = "Preferences",
                    color = Color(0xFF757575),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                PreferenceCheckbox(
                    text = "High floor room",
                    checked = preferences.highFloor,
                    onCheckedChange = { 
                        onPreferencesChange(preferences.copy(highFloor = it))
                    }
                )
                
                PreferenceCheckbox(
                    text = "Quiet room away from elevator",
                    checked = preferences.quietRoom,
                    onCheckedChange = { 
                        onPreferencesChange(preferences.copy(quietRoom = it))
                    }
                )
                
                PreferenceCheckbox(
                    text = "Extra pillows",
                    checked = preferences.extraPillows,
                    onCheckedChange = { 
                        onPreferencesChange(preferences.copy(extraPillows = it))
                    }
                )
                
                PreferenceCheckbox(
                    text = "Airport shuttle service",
                    checked = preferences.airportShuttle,
                    onCheckedChange = { 
                        onPreferencesChange(preferences.copy(airportShuttle = it))
                    }
                )
            }
        }
    }
}

@Composable
fun PreferenceCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF1AB6B6),
                uncheckedColor = Color(0xFF767676)
            )
        )
        
        Text(
            text = text,
            color = Color(0xFF212121),
            fontSize = 14.sp
        )
    }
}

@Composable
fun PaymentMethodSection(
    selectedPaymentMethod: Int,
    onPaymentMethodChange: (Int) -> Unit,
    cardNumber: String,
    onCardNumberChange: (String) -> Unit,
    expiryDate: String,
    onExpiryDateChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    cardholderName: String,
    onCardholderNameChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Payment Method",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payment method options
            PaymentMethodOption(
                title = "Credit Card",
                subtitle = "Visa, Mastercard, AMEX",
                icon = "💳",
                isSelected = selectedPaymentMethod == 0,
                onClick = { onPaymentMethodChange(0) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            PaymentMethodOption(
                title = "Digital Wallet",
                subtitle = "Apple Pay, Google Pay",
                icon = "📱",
                isSelected = selectedPaymentMethod == 1,
                onClick = { onPaymentMethodChange(1) }
            )
            
            if (selectedPaymentMethod == 0) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Credit card form
                Column {
                    // Card Number
                    Column {
                        Text(
                            text = "Card Number",
                            color = Color(0xFF757575),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = onCardNumberChange,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Expiry Date and CVV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Expiry Date",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = onExpiryDateChange,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0E0E0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "CVV",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = cvv,
                                onValueChange = onCvvChange,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFE0E0E0),
                                    unfocusedBorderColor = Color(0xFFE0E0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Cardholder Name
                    Column {
                        Text(
                            text = "Cardholder Name",
                            color = Color(0xFF757575),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = cardholderName,
                            onValueChange = onCardholderNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedBorderColor = Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodOption(
    title: String,
    subtitle: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF8FFFE) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            if (isSelected) Color(0xFF1AB6B6) else Color(0xFFE0E0E0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 10.sp
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = Color(0xFF212121),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = subtitle,
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )
            }
            
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF1AB6B6),
                    unselectedColor = Color(0xFFE0E0E0)
                )
            )
        }
    }
}

@Composable
fun PriceSummarySection(
    priceBreakdown: PriceBreakdown,
    appliedVouchers: List<com.example.chillstay.domain.model.Voucher> = emptyList()
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Price Summary",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Price breakdown
            val nights = 3 // Default to 3 nights for display
            PriceRow("$${priceBreakdown.roomPrice.toInt()} × $nights nights", "$${priceBreakdown.roomPrice.toInt()}")
            PriceRow("Service fee", "$${priceBreakdown.serviceFee.toInt()}")
            PriceRow("Taxes", "$${priceBreakdown.taxes.toInt()}")
            
            // Show applied vouchers
            appliedVouchers.forEach { voucher ->
                PriceRow("Voucher: ${voucher.code}", "-$${priceBreakdown.voucherDiscount.toInt()}")
            }
            
            Spacer(modifier = Modifier.height(13.dp))
            
            Divider(color = Color(0xFFE0E0E0))
            
            Spacer(modifier = Modifier.height(13.dp))
            
            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total",
                    color = Color(0xFF757575),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$${priceBreakdown.finalTotal.toInt()}",
                    color = Color(0xFF1AB6B6),
                    fontSize = 17.44.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    price: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFF757575),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = price,
            color = Color(0xFF212121),
            fontSize = 15.50.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
