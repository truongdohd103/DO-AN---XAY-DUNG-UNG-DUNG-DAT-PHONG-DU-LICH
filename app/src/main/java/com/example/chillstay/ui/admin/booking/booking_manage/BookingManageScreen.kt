package com.example.chillstay.ui.admin.booking.booking_manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.R
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.BookingSummary
import com.example.chillstay.ui.components.MarqueeText
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingManageScreen(
    onNavigateBack: () -> Unit = {},
    onViewBooking: (String) -> Unit = {},
    viewModel: BookingManageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F5F5)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is BookingManageEffect.NavigateBack -> onNavigateBack()
                is BookingManageEffect.NavigateToView -> onViewBooking(effect.bookingId)
                is BookingManageEffect.ShowError -> {
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
                        .height(72.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onEvent(BookingManageIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Booking Management",
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
                                onClick = { viewModel.onEvent(BookingManageIntent.LoadBookings) },
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
                            .padding(top = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Search Bar - GIỐNG AccommodationManage
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(BookingManageIntent.SearchQueryChanged(it))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(63.dp),
                            placeholder = {
                                Text(
                                    "Search by phone or email",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF767676)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0xFF767676)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.onEvent(BookingManageIntent.PerformSearch)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = lightGray,
                                unfocusedContainerColor = lightGray,
                                disabledContainerColor = lightGray,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Date Pickers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date From Picker
                            DatePickerField(
                                selectedDate = uiState.dateFrom,
                                onDateSelected = { date ->
                                    viewModel.onEvent(BookingManageIntent.DateFromChanged(date))
                                },
                                isOpen = uiState.isDateFromPickerOpen,
                                onToggle = {
                                    viewModel.onEvent(BookingManageIntent.ToggleDateFromPicker)
                                },
                                modifier = Modifier.weight(1f)
                            )

                            // Date To Picker
                            DatePickerField(
                                selectedDate = uiState.dateTo,
                                onDateSelected = { date ->
                                    viewModel.onEvent(BookingManageIntent.DateToChanged(date))
                                },
                                isOpen = uiState.isDateToPickerOpen,
                                onToggle = {
                                    viewModel.onEvent(BookingManageIntent.ToggleDateToPicker)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Booking List
                        uiState.paginatedBookings.forEach { booking ->
                            BookingCard(
                                booking = booking,
                                onClick = {
                                    viewModel.onEvent(BookingManageIntent.ViewBooking(booking.id))
                                }
                            )
                        }

                        if (uiState.paginatedBookings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No bookings found",
                                    fontSize = 16.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }

                        // Pagination - GIỐNG AccommodationManage
                        if (uiState.bookings.isNotEmpty()) {
                            PaginationControls(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                onPageChange = {
                                    viewModel.onEvent(BookingManageIntent.GoToPage(it))
                                },
                                onPreviousPage = {
                                    viewModel.onEvent(BookingManageIntent.PreviousPage)
                                },
                                onNextPage = {
                                    viewModel.onEvent(BookingManageIntent.NextPage)
                                },
                                tealColor = tealColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: Long?,
    onDateSelected: (Long?) -> Unit,
    isOpen: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )

    Box(modifier = modifier) {
        // Date Display Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDate != null) {
                        formatDateToDisplay(selectedDate)
                    } else {
                        "mm/dd/yyyy"
                    },
                    fontSize = 14.sp,
                    color = Color(0xFF212121)
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Date Picker Dialog
        if (isOpen) {
            DatePickerDialog(
                onDismissRequest = { onToggle() },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDateSelected(datePickerState.selectedDateMillis)
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onToggle() }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
            }
        }
    }
}

fun formatDateToDisplay(dateMillis: Long): String {
    val date = Date(dateMillis)
    val format = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return format.format(date)
}

@Composable
fun BookingCard(
    booking: BookingSummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Booking ID and Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            MarqueeText(
                text = "#${booking.id}",
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                textSize = 16.sp,
                textColor = Color(0xFF212121),
                fontWeight = FontWeight.Bold
            )

            StatusBadge(status = booking.status)
        }

        // Guest Name
        Text(
            text = buildAnnotatedString {
                append("Guest: ")
                append("Guest User")
            },
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 21.sp
        )

        // Phone number (placeholder)
        Text(
            text = "+1 234 567 8900",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 21.sp
        )

        // Hotel and Room
        Text(
            text = "${booking.hotelName ?: "Hotel"} - ${booking.roomName ?: "Room"}",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 21.sp
        )

        // Date range
        val nights = calculateNights(booking.dateFrom, booking.dateTo)
        Text(
            text = "${formatDateShort(booking.dateFrom)} - ${formatDateShort(booking.dateTo)} ($nights night${if (nights > 1) "s" else ""})",
            fontSize = 14.sp,
            color = Color(0xFF666666),
            lineHeight = 21.sp
        )
    }
}

@Composable
fun StatusBadge(status: BookingStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        BookingStatus.CONFIRMED -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32),
            "Confirmed"
        )

        BookingStatus.PENDING -> Triple(
            Color(0xFFFFF3E0),
            Color(0xFFE65100),
            "Pending"
        )

        BookingStatus.CANCELLED -> Triple(
            Color(0xFFFFEBEE),
            Color(0xFFC62828),
            "Cancelled"
        )

        BookingStatus.COMPLETED -> Triple(
            Color(0xFFE3F2FD),
            Color(0xFF1565C0),
            "Completed"
        )

        BookingStatus.CHECKED_IN -> Triple(
            Color(0xFFE8EAF6),
            Color(0xFF283593),
            "Checked In"
        )

        BookingStatus.CHECKED_OUT -> Triple(
            Color(0xFFE8EAF6),
            Color(0xFF283593),
            "Checked Out"
        )

        BookingStatus.REFUNDED -> Triple(
            Color(0xFFFCE4EC),
            Color(0xFFC2185B),
            "Refunded"
        )
    }

    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

// Pagination Controls - GIỐNG AccommodationManage
@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    tealColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Previous")
        }

        Text(
            text = "Page $currentPage of $totalPages",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Next")
        }
    }
}

fun formatDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (_: Exception) {
        dateString
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
            1
        }
    } catch (_: Exception) {
        1
    }
}