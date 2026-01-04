package com.example.chillstay.ui.admin.statistics.room_view

import android.annotation.SuppressLint
import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.domain.model.Room
import com.example.chillstay.ui.components.ResponsiveContainer
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomViewScreen(
    roomId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: RoomViewViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F7F8)
    val greenColor = Color(0xFF4CAF50)

    LaunchedEffect(roomId) {
        viewModel.onEvent(RoomViewIntent.LoadRoomStatistics(roomId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RoomViewEffect.NavigateBack -> onNavigateBack()
                is RoomViewEffect.ShowError -> {
                    Log.e("RoomView", "Error: ${effect.message}")
                }
                is RoomViewEffect.ShowSuccess -> {
                    Log.d("RoomView", "Success: ${effect.message}")
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
                                text = uiState.room?.name?.ifBlank { "Room Statistics" }
                                    ?: "Room Statistics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.onEvent(RoomViewIntent.NavigateBack)
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
                            // Room Card Section
                            uiState.room?.let { room ->
                                RoomCardSection(room = room)
                            }

                            Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFF5F7F8)))

                            // Date Filters Section
                            DateFiltersSection(
                                uiState = uiState,
                                tealColor = tealColor,
                                lightGray = lightGray,
                                onEvent = viewModel::onEvent
                            )

                            // Statistics Cards
                            StatisticsSection(
                                uiState = uiState,
                                tealColor = tealColor,
                                lightGray = lightGray
                            )

                            Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFF5F7F8)))

                            // Booking Trend Chart
                            BookingTrendSection(
                                uiState = uiState,
                                greenColor = greenColor
                            )

                            Spacer(modifier = Modifier.height(8.dp).background(Color(0xFFF5F7F8)))

                            // Recent Bookings Table
                            RecentBookingsSection(
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

@SuppressLint("DefaultLocale")
@Composable
fun RoomCardSection(room: Room) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Room Image
            room.gallery?.thisRoom?.firstOrNull()?.let { imageUrl ->
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = room.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Favorite button
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .background(Color.White, RoundedCornerShape(20.dp))
                    ) {
                        Text("♡", fontSize = 20.sp, color = Color(0xFFFF9800))
                    }

                    // Gallery count
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.7f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${room.gallery.totalCount}",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Room Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = room.name.ifBlank { "Unknown Room" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )

                // Room details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "${room.area} m²",
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Max ${room.capacity} adults",
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    val beds = buildString {
                        if (room.doubleBed > 0) append("${room.doubleBed} double ")
                        if (room.singleBed > 0) append("${room.singleBed} single")
                    }.trim()
                    if (beds.isNotEmpty()) {
                        Text(
                            text = beds,
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )
                    }
                }

                // Features
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    room.feature.take(4).forEach { feature ->
                        Text(
                            text = feature,
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }

                // Price section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F8))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (room.breakfastPrice > 0) {
                                Text(
                                    text = "✓ Breakfast available ($$$${String.format("%.0f", room.breakfastPrice)} / person)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                            Text(
                                text = "Non-refundable (Low price!)",
                                fontSize = 14.sp,
                                color = Color(0xFF212121),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            if (room.discount > 0) {
                                Text(
                                    text = "-${String.format("%.0f", room.discount * 100)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFF9800)
                                )
                            }
                            Text(
                                text = "$$$${String.format("%.0f", room.price)}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800)
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
fun DateFiltersSection(
    uiState: RoomViewUiState,
    tealColor: Color,
    lightGray: Color,
    onEvent: (RoomViewIntent) -> Unit
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
            OutlinedTextField(
                value = uiState.dateFrom?.let { dateFormatter.format(Date(it)) } ?: "Select date",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = { onEvent(RoomViewIntent.ToggleDateFromPicker) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Select date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF757575)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = lightGray,
                    unfocusedContainerColor = lightGray,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Date To
            OutlinedTextField(
                value = uiState.dateTo?.let { dateFormatter.format(Date(it)) } ?: "Select date",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.weight(1f),
                trailingIcon = {
                    IconButton(onClick = { onEvent(RoomViewIntent.ToggleDateToPicker) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = "Select date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF757575)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = lightGray,
                    unfocusedContainerColor = lightGray,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        // Date Pickers
        if (uiState.showDateFromPicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { onEvent(RoomViewIntent.ToggleDateFromPicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(RoomViewIntent.DateFromChanged(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(RoomViewIntent.ToggleDateFromPicker) }) {
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
                onDismissRequest = { onEvent(RoomViewIntent.ToggleDateToPicker) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(RoomViewIntent.DateToChanged(datePickerState.selectedDateMillis))
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(RoomViewIntent.ToggleDateToPicker) }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Apply Button
        Button(
            onClick = { onEvent(RoomViewIntent.ApplyFilters) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tealColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Apply",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StatisticsSection(
    uiState: RoomViewUiState,
    tealColor: Color,
    lightGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Room Revenue",
            value = "$$$${String.format("%,.0f", uiState.totalRevenue)}",
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF757575)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = tealColor
            )
        }
    }
}

@Composable
fun BookingTrendSection(
    uiState: RoomViewUiState,
    greenColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "📈 Booking Trend",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Line Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LineChart(
                    data = uiState.chartData,
                    maxValue = uiState.maxRevenueValue,
                    greenColor = greenColor
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun LineChart(
    data: List<Pair<String, Double>>,
    maxValue: Double,
    greenColor: Color
) {
    if (data.isEmpty() || maxValue <= 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                color = Color(0xFF757575),
                fontSize = 14.sp
            )
        }
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 60.dp, end = 20.dp, top = 30.dp, bottom = 60.dp)
    ) {
        val width = size.width
        val height = size.height

        if (data.isEmpty()) return@Canvas

        // Calculate step for X axis
        val step = width / (data.size - 1).coerceAtLeast(1)

        // Draw Y axis
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(0f, 0f),
            end = Offset(0f, height),
            strokeWidth = 3f
        )

        // Draw X axis
        drawLine(
            color = Color.Gray.copy(alpha = 0.5f),
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 3f
        )

        // Draw Y axis labels and grid lines
        val ySteps = 5
        for (i in 0..ySteps) {
            val yValue = maxValue * i / ySteps
            val y = height - (height * i / ySteps)

            // Grid line
            drawLine(
                color = Color.Gray.copy(alpha = 0.15f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )

            // Y axis label
            drawContext.canvas.nativeCanvas.drawText(
                "${String.format("%.0f", yValue)}",
                -15f,
                y + 8f,
                Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 32f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        // Draw lines connecting points
        val path = Path()
        data.forEachIndexed { index, (_, value) ->
            val x = index * step
            val y = height - (value / maxValue * height).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = greenColor,
            style = Stroke(width = 5f)
        )

        // Draw points
        data.forEachIndexed { index, (_, value) ->
            val x = index * step
            val y = height - (value / maxValue * height).toFloat()

            // Outer circle (border)
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = Offset(x, y)
            )
            // Inner circle
            drawCircle(
                color = greenColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }

        // Draw X axis labels (dates/months/years)
        val maxVisibleLabels = 6
        val labelInterval = (data.size / maxVisibleLabels).coerceAtLeast(1)

// small, reusable paint for X labels
        val xLabelPaint = Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 14f // giảm kích thước chữ (px). Nếu trên màn hình lớn muốn to hơn -> tăng nhẹ
            textAlign = Paint.Align.CENTER
        }

        data.forEachIndexed { index, (label, _) ->
            // chỉ vẽ 1 label mỗi labelInterval (và luôn vẽ label cuối cùng)
            if (index % labelInterval == 0 || index == data.lastIndex) {
                val x = index * step
                // tránh vẽ quá sát 2 mép
                val clampedX = x.coerceIn(0f, width)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    clampedX,
                    height + 28f, // đẩy label gần hơn vào trục (giảm từ 40f -> 28f)
                    xLabelPaint
                )
            }
        }
    }
}

@Composable
fun RecentBookingsSection(
    uiState: RoomViewUiState,
    tealColor: Color,
    onEvent: (RoomViewIntent) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_recent),
                        contentDescription = null,
                        tint = tealColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Recent Bookings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }

                // Total count badge
                if (uiState.recentBookings.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tealColor.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${uiState.recentBookings.size} total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = tealColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = tealColor)
                    }
                }
                uiState.recentBookings.isEmpty() -> {
                    EmptyBookingsState()
                }
                else -> {
                    Column {
                        // Table with horizontal scroll
                        BookingsTable(
                            bookings = uiState.paginatedBookings
                        )

                        // Pagination
                        if (uiState.totalPages > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            PaginationControls(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                onPreviousPage = { onEvent(RoomViewIntent.PreviousPage) },
                                onNextPage = { onEvent(RoomViewIntent.NextPage) },
                                tealColor = tealColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyBookingsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_recent),
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No Bookings Found",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575)
        )
        Text(
            text = "There are no bookings for this room yet",
            fontSize = 14.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun BookingsTable(
    bookings: List<BookingInfo>
) {
    // Define column widths
    val idWidth = 120.dp
    val guestWidth = 200.dp
    val dateWidth = 100.dp
    val nightsWidth = 80.dp
    val amountWidth = 110.dp
    val statusWidth = 100.dp
    val spacing = 12.dp

    // Calculate total width: sum of all columns + spacing between them
    val totalWidth = idWidth + guestWidth + dateWidth + nightsWidth + amountWidth + statusWidth + (spacing * 5)

    // Wrap the table in horizontal scroll
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.width(totalWidth)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F7F8), RoundedCornerShape(8.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                TableHeaderCell("Booking ID", Modifier.width(idWidth))
                TableHeaderCell("Guest Name", Modifier.width(guestWidth))
                TableHeaderCell("Check-in", Modifier.width(dateWidth))
                TableHeaderCell("Nights", Modifier.width(nightsWidth))
                TableHeaderCell("Amount", Modifier.width(amountWidth))
                TableHeaderCell("Status", Modifier.width(statusWidth))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rows
            bookings.forEachIndexed { index, booking ->
                if (index > 0) {
                    HorizontalDivider(
                        color = Color(0xFFF0F0F0),
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) Color.White else Color(0xFFFAFAFA)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Booking ID
                    Text(
                        text = booking.bookingId.take(10),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1976D2),
                        modifier = Modifier.width(idWidth),
                        maxLines = 1
                    )

                    // Guest Name
                    Text(
                        text = booking.guestName,
                        fontSize = 13.sp,
                        color = Color(0xFF212121),
                        modifier = Modifier.width(guestWidth),
                        maxLines = 1
                    )

                    // Check-in Date
                    Text(
                        text = booking.checkInDate,
                        fontSize = 13.sp,
                        color = Color(0xFF616161),
                        modifier = Modifier.width(dateWidth)
                    )

                    // Nights
                    Text(
                        text = "${booking.nights}n",
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.width(nightsWidth)
                    )

                    // Amount
                    Text(
                        text = "${String.format("%,.0f", booking.amount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.width(amountWidth)
                    )

                    // Status Badge
                    StatusBadge(
                        status = booking.status,
                        modifier = Modifier.width(statusWidth)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "confirmed" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "cancelled" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "completed" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "pending" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        else -> Color(0xFFF5F5F5) to Color(0xFF757575)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun TableHeaderCell(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF757575),
        modifier = modifier
    )
}

@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
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
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Previous", fontSize = 13.sp)
        }

        Text(
            text = "Page $currentPage of $totalPages",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF212121)
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Next", fontSize = 13.sp)
        }
    }
}