package com.example.chillstay.ui.admin.statistics.customer_statistics

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
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
import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.ui.components.ResponsiveContainer
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerStatisticsScreen(
    onNavigateBack: () -> Unit = {},
    onViewCustomer: (String) -> Unit = {},
    viewModel: CustomerStatisticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F7F8)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CustomerStatisticsEffect.NavigateBack -> onNavigateBack()
                is CustomerStatisticsEffect.NavigateToCustomer -> onViewCustomer(effect.userId)
                is CustomerStatisticsEffect.ShowError -> {
                    Log.e("CustomerStats", "Error: ${effect.message}")
                }
                is CustomerStatisticsEffect.ShowSuccess -> {
                    Log.d("CustomerStats", "Success: ${effect.message}")
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
                                text = "Customer Analytics",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                viewModel.onEvent(CustomerStatisticsIntent.NavigateBack)
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

                            // Top Customers Table
                            TopCustomersSection(
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
    uiState: CustomerStatisticsUiState,
    tealColor: Color,
    lightGray: Color,
    onEvent: (CustomerStatisticsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Date Range",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121)
        )

        // Year, Quarter, Month filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeFilterDropdown(
                label = "Year",
                value = uiState.selectedYear?.toString() ?: "All",
                isExpanded = uiState.isYearExpanded,
                onExpandedChange = { onEvent(CustomerStatisticsIntent.ToggleYearDropdown) },
                options = listOf("All") + uiState.availableYears.map { it.toString() },
                onOptionSelected = { selected ->
                    val year = if (selected == "All") null else selected.toIntOrNull()
                    onEvent(CustomerStatisticsIntent.YearChanged(year))
                    onEvent(CustomerStatisticsIntent.ToggleYearDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            TimeFilterDropdown(
                label = "Quarter",
                value = uiState.selectedQuarter?.let { "Q$it" } ?: "All",
                isExpanded = uiState.isQuarterExpanded,
                onExpandedChange = { onEvent(CustomerStatisticsIntent.ToggleQuarterDropdown) },
                options = listOf("All") + uiState.availableQuarters.map { "Q$it" },
                onOptionSelected = { selected ->
                    val quarter = if (selected == "All") null else selected.removePrefix("Q").toIntOrNull()
                    onEvent(CustomerStatisticsIntent.QuarterChanged(quarter))
                    onEvent(CustomerStatisticsIntent.ToggleQuarterDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )

            TimeFilterDropdown(
                label = "Month",
                value = uiState.selectedMonth?.let { getMonthShortName(it) } ?: "All",
                isExpanded = uiState.isMonthExpanded,
                onExpandedChange = { onEvent(CustomerStatisticsIntent.ToggleMonthDropdown) },
                options = listOf("All") + uiState.availableMonths.map { getMonthShortName(it) },
                onOptionSelected = { selected ->
                    val month = if (selected == "All") null else getMonthFromShortName(selected)
                    onEvent(CustomerStatisticsIntent.MonthChanged(month))
                    onEvent(CustomerStatisticsIntent.ToggleMonthDropdown)
                },
                lightGray = lightGray,
                modifier = Modifier.weight(1f)
            )
        }

        // Apply Button
        Button(
            onClick = { onEvent(CustomerStatisticsIntent.ApplyFilters) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tealColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Apply",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun TimeFilterDropdown(
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    lightGray: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clickable { onExpandedChange() }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f),
                        tint = Color.Black
                    )
                }
            }

            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange() },
                modifier = Modifier.fillMaxWidth(0.3f)
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

fun getMonthShortName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> "Unknown"
    }
}

fun getMonthFromShortName(shortName: String): Int {
    return when (shortName) {
        "Jan" -> 1
        "Feb" -> 2
        "Mar" -> 3
        "Apr" -> 4
        "May" -> 5
        "Jun" -> 6
        "Jul" -> 7
        "Aug" -> 8
        "Sep" -> 9
        "Oct" -> 10
        "Nov" -> 11
        "Dec" -> 12
        else -> 1
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun StatisticsSection(
    uiState: CustomerStatisticsUiState,
    tealColor: Color,
    lightGray: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Spent",
            value = "$${String.format("%,.0f", uiState.totalSpent)}",
            icon = Icons.Default.Star,
            tealColor = tealColor,
            lightGray = lightGray,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Total Bookings",
            value = "${uiState.totalBookings}",
            icon = Icons.Default.Favorite,
            tealColor = tealColor,
            lightGray = lightGray,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            title = "Total Customers",
            value = "${uiState.totalCustomers}",
            icon = Icons.Default.Person,
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
    icon: ImageVector,
    tealColor: Color,
    lightGray: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = lightGray),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun TopPerformersSection(
    uiState: CustomerStatisticsUiState,
    tealColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            text = "Top Performers",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TopPerformerCard(
                title = "Most Bookings",
                customerName = uiState.topByBookings?.name ?: "N/A",
                value = "${uiState.topByBookings?.totalBookings ?: 0} bookings",
                tealColor = tealColor,
                modifier = Modifier.weight(1f)
            )

            TopPerformerCard(
                title = "Highest Revenue",
                customerName = uiState.topBySpent?.name ?: "N/A",
                value = "$${String.format("%,.0f", uiState.topBySpent?.totalSpent ?: 0.0)}",
                tealColor = tealColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun TopPerformerCard(
    title: String,
    customerName: String,
    value: String,
    tealColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7F8)),
        elevation = CardDefaults.cardElevation(0.dp)
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
                color = Color(0xFF757575),
                fontWeight = FontWeight.Medium
            )

            Text(
                text = customerName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tealColor
            )
        }
    }
}

@Composable
fun RevenueTrendSection(
    uiState: CustomerStatisticsUiState,
    tealColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Text(
            text = uiState.chartTypeDisplayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212121),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(256.dp)
                .background(
                    color = Color(0xFFF0F9FF),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            BarChart(
                data = uiState.chartData,
                maxValue = uiState.maxRevenueValue,
                tealColor = tealColor
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<Pair<String, Double>>,
    maxValue: Double,
    tealColor: Color
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

    val scrollState = rememberScrollState()
    val isScrollable = data.size > 7

    Row(
        modifier = Modifier
            .fillMaxSize()
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
                    Modifier
                        .width(60.dp)
                        .padding(horizontal = 2.dp)
                } else {
                    Modifier
                        .weight(1f, fill = false)
                        .padding(horizontal = 2.dp)
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF757575),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TopCustomersSection(
    uiState: CustomerStatisticsUiState,
    tealColor: Color,
    onEvent: (CustomerStatisticsIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = "👥 Top Customers by Revenue",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        CustomerStatsTable(
            stats = uiState.paginatedCustomerStats,
            onCustomerClick = { userId ->
                onEvent(CustomerStatisticsIntent.ViewCustomer(userId))
            }
        )

        if (uiState.customerStats.isNotEmpty()) {
            PaginationControls(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                onPreviousPage = { onEvent(CustomerStatisticsIntent.PreviousPage) },
                onNextPage = { onEvent(CustomerStatisticsIntent.NextPage) },
                tealColor = tealColor,
                modifier = Modifier.padding(24.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CustomerStatsTable(
    stats: List<CustomerStats>,
    onCustomerClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .background(Color(0xFFF5F7F8))
        ) {
            TableHeaderCell("Customer", Modifier.weight(2f))
            TableHeaderCell("Bookings", Modifier.weight(1f))
            TableHeaderCell("Revenue", Modifier.weight(1f))
        }

        stats.forEach { stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .clickable { onCustomerClick(stat.id) }
                    .background(Color.White)
            ) {
                TableCell(stat.name, Modifier.weight(2f), isBold = true)
                TableCell("${stat.totalBookings}", Modifier.weight(1f))
                TableCell("$${String.format("%,.0f", stat.totalSpent)}", Modifier.weight(1f), isBold = true)
            }
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF0F0F0))
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
        modifier = modifier.padding(12.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun TableCell(text: String?, modifier: Modifier = Modifier, isBold: Boolean = false, maxLines: Int = 1) {
        Text(
            text = text ?: "",
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF212121),
            modifier = modifier.padding(12.dp),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
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