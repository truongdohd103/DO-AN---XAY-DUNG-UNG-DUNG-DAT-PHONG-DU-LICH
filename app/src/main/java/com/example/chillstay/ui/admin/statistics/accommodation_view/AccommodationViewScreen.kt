package com.example.chillstay.ui.admin.statistics.accommodation_view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.ui.components.ResponsiveContainer
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationViewScreen(
    hotelId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToRoom: (roomId : String) -> Unit = {},
    viewModel: AccommodationViewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F7F8)

    LaunchedEffect(hotelId) {
        viewModel.onEvent(AccommodationViewIntent.LoadHotelStatistics(hotelId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccommodationViewEffect.NavigateBack -> onNavigateBack()
                is AccommodationViewEffect.ShowError -> {
                    Log.e("AccommodationView", "Error: ${effect.message}")
                }
                is AccommodationViewEffect.ShowSuccess -> {
                    Log.d("AccommodationView", "Success: ${effect.message}")
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        ResponsiveContainer {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = uiState.hotel?.name ?: "Hotel Statistics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.onEvent(AccommodationViewIntent.NavigateBack)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = tealColor
                        )
                    )
                },
                containerColor = Color.White
            ) { paddingValues ->
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = tealColor)
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Hotel Card Section
                            uiState.hotel?.let { hotel ->
                                HotelCardSection(hotel = hotel)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Date Filters Section
                            DateFiltersSection(
                                uiState = uiState,
                                tealColor = tealColor,
                                lightGray = lightGray,
                                onEvent = viewModel::onEvent
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Statistics Cards
                            StatisticsSection(
                                uiState = uiState,
                                tealColor = tealColor,
                                lightGray = lightGray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Top Performers Section
                            TopPerformersSection(
                                uiState = uiState,
                                tealColor = tealColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Revenue by Room Type (Pie Chart)
                            RevenueByRoomTypeSection(
                                uiState = uiState,
                                tealColor = tealColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Revenue by Room Table
                            RevenueByRoomSection(
                                uiState = uiState,
                                tealColor = tealColor,
                                onEvent = viewModel::onEvent
                            )

                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HotelCardSection(
    hotel: com.example.chillstay.domain.model.Hotel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Hotel Image
            if (hotel.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = hotel.imageUrl.first(),
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Hotel Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = hotel.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${hotel.city}, ${hotel.country}",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )

                    // Rating
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "${hotel.rating}",
                            fontSize = 12.sp,
                            color = Color(0xFF1AB5B5),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${hotel.numberOfReviews} reviews)",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                hotel.minPrice?.let { price ->
                    Text(
                        text = "From $${String.format("%.0f", price)}/night",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1AB5B5)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFiltersSection(
    uiState: AccommodationViewUiState,
    tealColor: Color,
    lightGray: Color,
    onEvent: (AccommodationViewIntent) -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Filter",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date From
            DatePickerField(
                label = "Check-in",
                value = uiState.dateFrom?.let { dateFormatter.format(Date(it)) } ?: "Select date",
                onClick = { onEvent(AccommodationViewIntent.ToggleDateFromPicker) },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            // Date To
            DatePickerField(
                label = "Check-out",
                value = uiState.dateTo?.let { dateFormatter.format(Date(it)) } ?: "Select date",
                onClick = { onEvent(AccommodationViewIntent.ToggleDateToPicker) },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )
        }

        // Date Pickers
        if (uiState.showDateFromPicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { onEvent(AccommodationViewIntent.ToggleDateFromPicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(AccommodationViewIntent.DateFromChanged(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(AccommodationViewIntent.ToggleDateFromPicker) }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (uiState.showDateToPicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { onEvent(AccommodationViewIntent.ToggleDateToPicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(AccommodationViewIntent.DateToChanged(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(AccommodationViewIntent.ToggleDateToPicker) }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Apply Filters Button
        Button(
            onClick = { onEvent(AccommodationViewIntent.ApplyFilters) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tealColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Apply",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DatePickerField(
    label: String,
    value: String,
    onClick: () -> Unit,
    lightGray: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = "Select date",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFF757575)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = lightGray,
            unfocusedContainerColor = lightGray,
            focusedBorderColor = Color(0xFF1AB5B5),
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun StatisticsSection(
    uiState: AccommodationViewUiState,
    tealColor: Color,
    lightGray: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Hotel Revenue",
                value = "$${String.format("%,.0f", uiState.totalRevenue)}",
                tealColor = tealColor,
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Total Bookings",
                value = "${uiState.totalBookings}",
                tealColor = tealColor,
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Cancellation Rate",
                value = "${String.format("%.1f", uiState.cancellationRate)}%",
                tealColor = tealColor,
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    tealColor: Color,
    lightGray: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = tealColor
            )
        }
    }
}

@Composable
fun TopPerformersSection(
    uiState: AccommodationViewUiState,
    tealColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top by Revenue
            uiState.topByRevenue?.let { topRevenue ->
                TopPerformerCard(
                    title = "Highest Revenue",
                    roomType = topRevenue.roomType,
                    value = "$${String.format("%,.0f", topRevenue.revenue)}",
                    icon = Icons.Default.Star,
                    tealColor = tealColor,
                    modifier = Modifier.weight(1f)
                )
            } ?: run {
                EmptyTopPerformerCard(
                    title = "Highest Revenue",
                    icon = Icons.Default.Star,
                    modifier = Modifier.weight(1f)
                )
            }

            // Top by Bookings
            uiState.topByBookings?.let { topBookings ->
                TopPerformerCard(
                    title = "Most bookings",
                    roomType = topBookings.roomType,
                    value = "${topBookings.bookings} bookings",
                    icon = Icons.Default.Favorite,
                    tealColor = tealColor,
                    modifier = Modifier.weight(1f)
                )
            } ?: run {
                EmptyTopPerformerCard(
                    title = "Most bookings",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun TopPerformerCard(
    title: String,
    roomType: String,
    value: String,
    icon: ImageVector,
    tealColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
            }

            Text(
                text = roomType,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = tealColor
            )
        }
    }
}

@Composable
fun EmptyTopPerformerCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7F8)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF757575),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
            }

            Text(
                text = "No data",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun RevenueByRoomTypeSection(
    uiState: AccommodationViewUiState,
    tealColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFF5F7F8)))

        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "📊 Revenue by Room Type",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (uiState.roomTypeRevenue.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No revenue data available",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            } else {
                // Pie Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PieChart(
                        data = uiState.roomTypeRevenue,
                        modifier = Modifier.size(180.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.roomTypeRevenue.forEach { item ->
                            val percentage = if (uiState.totalRoomTypeRevenue > 0) {
                                (item.revenue / uiState.totalRoomTypeRevenue) * 100
                            } else 0.0

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(
                                                Color(item.color),
                                                RoundedCornerShape(4.dp)
                                            )
                                    )
                                    Text(
                                        text = item.roomType,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF757575)
                                    )
                                }

                                Text(
                                    text = "${String.format("%.0f", percentage)}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChart(
    data: List<RoomTypeRevenue>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.revenue }
    if (total == 0.0) return

    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2
        val center = Offset(size.width / 2, size.height / 2)

        var currentAngle = -90f // Start from top

        data.forEach { item ->
            val sweepAngle = ((item.revenue / total) * 360).toFloat()

            drawArc(
                color = Color(item.color),
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2)
            )

            currentAngle += sweepAngle
        }

        // Draw white circle in center for donut effect
        drawCircle(
            color = Color.White,
            radius = radius * 0.5f,
            center = center
        )
    }
}

@Composable
fun RevenueByRoomSection(
    uiState: AccommodationViewUiState,
    tealColor: Color,
    onEvent: (AccommodationViewIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFF5F7F8)))

        Text(
            text = "️ Revenue by Room",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        // Table
        RoomStatsTable(
            stats = uiState.paginatedRoomStats
        )

        // Pagination
        if (uiState.roomStats.isNotEmpty()) {
            PaginationControls(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPageChange = { onEvent(AccommodationViewIntent.GoToPage(it)) },
                onPreviousPage = { onEvent(AccommodationViewIntent.PreviousPage) },
                onNextPage = { onEvent(AccommodationViewIntent.NextPage) },
                tealColor = tealColor,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RoomStatsTable(
    stats: List<RoomStats>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7F8))
        ) {
            TableHeaderCell("Room Type", Modifier.weight(2f))
            TableHeaderCell("Bookings", Modifier.weight(1f))
            TableHeaderCell("Revenue", Modifier.weight(1f))
            TableHeaderCell("Avg Rate", Modifier.weight(1f))
        }

        // Table Rows
        if (stats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No room data available",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            stats.forEach { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    TableCell(stat.roomType, Modifier.weight(2f), isBold = true)
                    TableCell("${stat.bookings}", Modifier.weight(1f))
                    TableCell("$${String.format("%,.0f", stat.revenue)}", Modifier.weight(1f), isBold = true)
                    TableCell("$${String.format("%.0f", stat.avgRate)}", Modifier.weight(1f))
                }
                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF757575),
        modifier = modifier.padding(12.dp)
    )
}

@Composable
fun TableCell(text: String, modifier: Modifier = Modifier, isBold: Boolean = false) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = Color(0xFF212121),
        modifier = modifier.padding(12.dp)
    )
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    tealColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1,
            colors = ButtonDefaults.buttonColors(containerColor = tealColor)
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
            colors = ButtonDefaults.buttonColors(containerColor = tealColor)
        ) {
            Text("Next")
        }
    }
}