package com.example.chillstay.ui.admin.statistics.accommodation_statistics

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.HotelBookingStats
import com.example.chillstay.ui.components.ResponsiveContainer
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationStatisticsScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: AccommodationStatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F7F8)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccommodationStatisticsEffect.NavigateBack -> onNavigateBack()
                is AccommodationStatisticsEffect.ShowError -> {
                    Log.e("AccommodationStats", "Error: ${effect.message}")
                }
                is AccommodationStatisticsEffect.ShowSuccess -> {
                    Log.d("AccommodationStats", "Success: ${effect.message}")
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
                                text = "Revenue Analytics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.onEvent(AccommodationStatisticsIntent.NavigateBack)
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
                            // Filters Section
                            FiltersSection(
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

                            // Revenue Trend Chart
                            RevenueTrendSection(
                                uiState = uiState,
                                tealColor = tealColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Revenue by Hotel Table
                            RevenueByHotelSection(
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
fun FiltersSection(
    uiState: AccommodationStatisticsUiState,
    tealColor: Color,
    lightGray: Color,
    onEvent: (AccommodationStatisticsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Time Period",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        // Year, Quarter, Month filters in one row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Year Dropdown
            TimeFilterDropdown(
                label = "Year",
                value = uiState.selectedYear?.toString() ?: "All",
                isExpanded = uiState.isYearExpanded,
                onExpandedChange = { onEvent(AccommodationStatisticsIntent.ToggleYearDropdown) },
                options = listOf("All") + uiState.availableYears.map { it.toString() },
                onOptionSelected = { selected ->
                    val year = if (selected == "All") null else selected.toIntOrNull()
                    onEvent(AccommodationStatisticsIntent.YearChanged(year))
                    onEvent(AccommodationStatisticsIntent.ToggleYearDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            // Quarter Dropdown
            TimeFilterDropdown(
                label = "Quarter",
                value = uiState.selectedQuarter?.let { "Q$it" } ?: "All",
                isExpanded = uiState.isQuarterExpanded,
                onExpandedChange = { onEvent(AccommodationStatisticsIntent.ToggleQuarterDropdown) },
                options = listOf("All") + uiState.availableQuarters.map { "Q$it" },
                onOptionSelected = { selected ->
                    val quarter = if (selected == "All") null else selected.removePrefix("Q").toIntOrNull()
                    onEvent(AccommodationStatisticsIntent.QuarterChanged(quarter))
                    onEvent(AccommodationStatisticsIntent.ToggleQuarterDropdown)
                },
                lightGray = lightGray,
                enabled = uiState.selectedYear != null,
                modifier = Modifier.weight(1f)
            )

            // Month Dropdown
            TimeFilterDropdown(
                label = "Month",
                value = uiState.selectedMonth?.let { getMonthShortName(it) } ?: "All",
                isExpanded = uiState.isMonthExpanded,
                onExpandedChange = { onEvent(AccommodationStatisticsIntent.ToggleMonthDropdown) },
                options = listOf("All") + uiState.availableMonths.map { getMonthShortName(it) },
                onOptionSelected = { selected ->
                    val month = if (selected == "All") null else getMonthNumber(selected)
                    onEvent(AccommodationStatisticsIntent.MonthChanged(month))
                    onEvent(AccommodationStatisticsIntent.ToggleMonthDropdown)
                },
                lightGray = lightGray,
                enabled = uiState.selectedYear != null && uiState.selectedQuarter == null,
                modifier = Modifier.weight(1f)
            )
        }

        // Location Filters
        Text(
            text = "Location",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(top = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Country Dropdown
            DropdownField(
                label = "Country",
                value = uiState.selectedCountry.ifEmpty { "All Countries" },
                isExpanded = uiState.isCountryExpanded,
                onExpandedChange = { onEvent(AccommodationStatisticsIntent.ToggleCountryDropdown) },
                options = listOf("All Countries") + uiState.availableCountries,
                onOptionSelected = {
                    val country = if (it == "All Countries") "" else it
                    onEvent(AccommodationStatisticsIntent.CountryChanged(country))
                    onEvent(AccommodationStatisticsIntent.ToggleCountryDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            // City Dropdown
            DropdownField(
                label = "City",
                value = uiState.selectedCity.ifEmpty { "All Cities" },
                isExpanded = uiState.isCityExpanded,
                onExpandedChange = { onEvent(AccommodationStatisticsIntent.ToggleCityDropdown) },
                options = listOf("All Cities") + uiState.availableCities,
                onOptionSelected = {
                    val city = if (it == "All Cities") "" else it
                    onEvent(AccommodationStatisticsIntent.CityChanged(city))
                    onEvent(AccommodationStatisticsIntent.ToggleCityDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )
        }

        // Apply Filters Button
        Button(
            onClick = { onEvent(AccommodationStatisticsIntent.ApplyFilters) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tealColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Apply Filters",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeFilterDropdown(
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    lightGray: Color,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) Color(0xFF757575) else Color(0xFFBDBDBD),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded && enabled,
            onExpandedChange = { if (enabled) onExpandedChange() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = if (enabled) Color(0xFF757575) else Color(0xFFBDBDBD)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = lightGray,
                    unfocusedContainerColor = lightGray,
                    disabledContainerColor = Color(0xFFF0F0F0),
                    focusedBorderColor = Color(0xFF1AB5B5),
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )

            ExposedDropdownMenu(
                expanded = isExpanded && enabled,
                onDismissRequest = { onExpandedChange() }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    lightGray: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF757575),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange() }
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = Color(0xFF757575)
                    )
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

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange() }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}

private fun getMonthShortName(month: Int): String {
    return when (month) {
        1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
        5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
        9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
        else -> "Unknown"
    }
}

private fun getMonthNumber(shortName: String): Int {
    return when (shortName) {
        "Jan" -> 1; "Feb" -> 2; "Mar" -> 3; "Apr" -> 4
        "May" -> 5; "Jun" -> 6; "Jul" -> 7; "Aug" -> 8
        "Sep" -> 9; "Oct" -> 10; "Nov" -> 11; "Dec" -> 12
        else -> 1
    }
}

@Composable
fun TopPerformersSection(
    uiState: AccommodationStatisticsUiState,
    tealColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Top Performers",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top by Bookings
            uiState.topByBookings?.let { topBookings ->
                TopPerformerCard(
                    title = "Most Bookings",
                    hotelName = topBookings.hotelName,
                    value = "${topBookings.bookings} bookings",
                    icon = Icons.Default.Favorite,
                    tealColor = tealColor,
                    modifier = Modifier.weight(1f)
                )
            } ?: run {
                EmptyTopPerformerCard(
                    title = "Most Bookings",
                    icon = Icons.Default.Favorite,
                    modifier = Modifier.weight(1f)
                )
            }

            // Top by Revenue
            uiState.topByRevenue?.let { topRevenue ->
                TopPerformerCard(
                    title = "Highest Revenue",
                    hotelName = topRevenue.hotelName,
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
        }
    }
}

@Composable
fun TopPerformerCard(
    title: String,
    hotelName: String,
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
                    tint = tealColor,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575)
                )
            }

            Text(
                text = hotelName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                fontSize = 18.sp,
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
                    modifier = Modifier.size(24.dp)
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
fun StatisticsSection(
    uiState: AccommodationStatisticsUiState,
    tealColor: Color,
    lightGray: Color
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
            StatCard(
                title = "Total Revenue",
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
                title = "Cancel Rate",
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
fun RevenueTrendSection(
    uiState: AccommodationStatisticsUiState,
    tealColor: Color
) {
    // Debug logging
    LaunchedEffect(uiState.chartData, uiState.maxRevenueValue) {
        Log.d("AccommodationStats", "Chart data size: ${uiState.chartData.size}")
        Log.d("AccommodationStats", "Max revenue: ${uiState.maxRevenueValue}")
        Log.d("AccommodationStats", "Period labels: ${uiState.periodLabels}")
        Log.d("AccommodationStats", "Period revenue: ${uiState.periodRevenue}")
        uiState.chartData.forEach { (label, revenue) ->
            Log.d("AccommodationStats", "Chart point: $label = $revenue")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Text(
            text = uiState.chartTypeDisplayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Chart
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F8)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (uiState.chartData.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No data available for this period",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Try adjusting your filters",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                RevenueChart(
                    data = uiState.chartData,
                    maxValue = uiState.maxRevenueValue,
                    tealColor = tealColor
                )
            }
        }
    }
}

@Composable
fun RevenueChart(
    data: List<Pair<String, Double>>,
    maxValue: Double,
    tealColor: Color
) {
    if (data.isEmpty() || maxValue == 0.0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No revenue data available", color = Color.Gray)
        }
        return
    }

    val scrollState = rememberScrollState()
    val isScrollable = data.size > 7

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .then(
                if (isScrollable) {
                    Modifier.horizontalScroll(scrollState)
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = if (isScrollable) Arrangement.spacedBy(16.dp) else Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, revenue) ->
            Column(
                modifier = if (isScrollable) {
                    // Fixed width when scrollable
                    Modifier
                        .width(60.dp)
                        .padding(horizontal = 2.dp)
                } else {
                    // Weight-based width when not scrollable
                    Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 2.dp)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Revenue value at top
                Text(
                    text = when {
                        revenue >= 1000000 -> "$${String.format("%.1f", revenue / 1000000)}M"
                        revenue >= 1000 -> "$${String.format("%.0f", revenue / 1000)}K"
                        else -> "$${String.format("%.0f", revenue)}"
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = tealColor,
                    modifier = Modifier.padding(bottom = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bar
                val barHeight = if (maxValue > 0) {
                    ((revenue / maxValue) * 160).coerceIn(20.0, 160.0).dp
                } else 20.dp

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(barHeight)
                        .background(
                            tealColor,
                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                        )
                )

                // Label at bottom
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 50.dp)
                )
            }
        }
    }
}

@Composable
fun RevenueByHotelSection(
    uiState: AccommodationStatisticsUiState,
    tealColor: Color,
    onEvent: (AccommodationStatisticsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            text = "Revenue by Hotel",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        // Table
        HotelStatsTable(
            stats = uiState.paginatedHotelStats
        )

        // Pagination
        if (uiState.hotelStats.isNotEmpty()) {
            PaginationControls(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPageChange = { onEvent(AccommodationStatisticsIntent.GoToPage(it)) },
                onPreviousPage = { onEvent(AccommodationStatisticsIntent.PreviousPage) },
                onNextPage = { onEvent(AccommodationStatisticsIntent.NextPage) },
                tealColor = tealColor,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HotelStatsTable(
    stats: List<HotelBookingStats>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F7F8))
        ) {
            TableHeaderCell("Hotel", Modifier.weight(2f))
            TableHeaderCell("Bookings", Modifier.weight(1f))
            TableHeaderCell("Revenue", Modifier.weight(1f))
            TableHeaderCell("Cancel %", Modifier.weight(1f))
        }

        // Table Rows
        stats.forEach { stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                TableCell(stat.hotelName, Modifier.weight(2f), isBold = true)
                TableCell("${stat.bookings}", Modifier.weight(1f))
                TableCell("$${String.format("%,.0f", stat.revenue)}", Modifier.weight(1f), isBold = true)
                TableCell(String.format("%.1f%%", stat.cancellationRate), Modifier.weight(1f))
            }
            Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
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