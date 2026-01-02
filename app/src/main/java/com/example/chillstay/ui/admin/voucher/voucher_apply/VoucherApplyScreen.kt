package com.example.chillstay.ui.admin.voucher.voucher_apply

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Voucher
import org.koin.androidx.compose.koinViewModel

import com.example.chillstay.ui.components.ResponsiveContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationSelectScreen(
    voucherId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: () -> Unit,
    viewModel: VoucherApplyViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(voucherId) {
        voucherId?.let { viewModel.onEvent(VoucherApplyIntent.LoadData(it)) }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VoucherApplyEffect.NavigateBack -> onNavigateBack()
                is VoucherApplyEffect.NavigateToConfirmation -> onNavigateBack()
                is VoucherApplyEffect.ShowSuccess -> {

                }
                is VoucherApplyEffect.ShowError -> {

                }
            }
        }
    }

    ResponsiveContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Apply Voucher Code",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onEvent(VoucherApplyIntent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1AB6B6),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            },
            containerColor = Color.White
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 140.dp)
                ) {
                // Voucher Card
                item {
                    uiState.voucher?.let { voucher ->
                        VoucherCard(
                            voucher = voucher,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                // Applied Hotels Info Card
                item {
                    if (uiState.appliedHotelsCount > 0) {
                        AppliedHotelsInfoCard(
                            appliedCount = uiState.appliedHotelsCount,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                // Search Bar
                item {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = {
                            viewModel.onEvent(VoucherApplyIntent.SearchQueryChanged(it))
                        },
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                // Country & City Filters
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CountryDropdown(
                            selectedCountry = uiState.selectedCountry,
                            countries = uiState.availableCountries,
                            isExpanded = uiState.isCountryExpanded,
                            onExpandedChange = {
                                viewModel.onEvent(VoucherApplyIntent.ToggleCountryDropdown)
                            },
                            onCountrySelected = {
                                viewModel.onEvent(VoucherApplyIntent.CountryChanged(it))
                            },
                            modifier = Modifier.weight(1f)
                        )

                        CityDropdown(
                            selectedCity = uiState.selectedCity,
                            cities = uiState.availableCities,
                            isExpanded = uiState.isCityExpanded,
                            onExpandedChange = {
                                viewModel.onEvent(VoucherApplyIntent.ToggleCityDropdown)
                            },
                            onCitySelected = {
                                viewModel.onEvent(VoucherApplyIntent.CityChanged(it))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Available Hotels Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Hotels",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE5E7EB)
                        ) {
                            Text(
                                text = "${uiState.availableHotelsCount} hotels",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Available Hotels List (Horizontal Scroll)
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                       items(uiState.paginatedHotels) { hotel ->
                            AvailableHotelCard(
                                hotel = hotel,
                                voucher = uiState.voucher,
                                uiState = uiState,
                                onAddClick = {
                                    viewModel.onEvent(VoucherApplyIntent.AddHotel(hotel))
                                }
                            )
                        }
                    }
                }

                item {
                    if (uiState.availableHotelsCount > uiState.itemsPerPage) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Previous Button
                            IconButton(
                                onClick = { viewModel.onEvent(VoucherApplyIntent.PreviousPage) },
                                enabled = uiState.currentPage > 1
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous",
                                    tint = if (uiState.currentPage > 1) Color(0xFF1AB6B6) else Color.Gray
                                )
                            }

                            // Page Numbers
                            (1..uiState.totalPages).forEach { page ->
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { viewModel.onEvent(VoucherApplyIntent.GoToPage(page)) },
                                    shape = CircleShape,
                                    color = if (page == uiState.currentPage) Color(0xFF1AB6B6) else Color.Transparent
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = page.toString(),
                                            color = if (page == uiState.currentPage) Color.White else Color(0xFF6B7280),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            // Next Button
                            IconButton(
                                onClick = { viewModel.onEvent(VoucherApplyIntent.NextPage) },
                                enabled = uiState.currentPage < uiState.totalPages
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next",
                                    tint = if (uiState.currentPage < uiState.totalPages) Color(0xFF1AB6B6) else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Selected Accommodations Header
                if (uiState.selectedHotels.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(top = 32.dp, bottom = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Accommodation Selected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1AB6B6)
                            ) {
                                Text(
                                    text = "${uiState.selectedHotelsCount}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Selected Hotels List
                items(uiState.selectedHotels) { hotel ->
                    SelectedHotelCard(
                        hotel = hotel,
                        voucher = uiState.voucher,
                        uiState = uiState,
                        onRemoveClick = {
                            viewModel.onEvent(VoucherApplyIntent.RemoveHotel(hotel))
                        },
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            // Bottom Section (Total + Confirm Button)
            if (uiState.selectedHotels.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    // Total Discount Card
                    TotalDiscountCard(
                        totalDiscount = uiState.totalDiscount,
                        accommodationCount = uiState.selectedHotelsCount,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )

                    // Confirm Button
                    Button(
                        onClick = {
                            viewModel.onEvent(VoucherApplyIntent.ConfirmAndApplyVoucher)
                        },
                        enabled = uiState.canConfirm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF059669),
                            disabledContainerColor = Color(0xFFE5E7EB)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Confirm & Apply Voucher",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Loading Indicator
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            }
        }
    }
}

@Composable
private fun VoucherCard(
    voucher: Voucher,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFCD34D),
                            Color(0xFFF59E0B)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = voucher.code,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = voucher.description,
                    fontSize = 14.sp,
                    color = Color.White
                )

                Text(
                    text = "Valid until: ${formatDate(voucher.validTo)}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(63.dp),
        placeholder = {
            Text(
                text = "Search",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF828282)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color(0xFFF5F5F5),
            focusedContainerColor = Color(0xFFF5F5F5),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color(0xFF1AB6B6)
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountryDropdown(
    selectedCountry: String,
    countries: List<String>,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    onCountrySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Country",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange() }
        ) {
            OutlinedTextField(
                value = selectedCountry.ifBlank { "Select country" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFEFEFEF),
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedBorderColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange() }
            ) {
                DropdownMenuItem(
                    text = { Text("All Countries") },
                    onClick = {
                        onCountrySelected("")
                        onExpandedChange()
                    }
                )
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            onCountrySelected(country)
                            onExpandedChange()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CityDropdown(
    selectedCity: String,
    cities: List<String>,
    isExpanded: Boolean,
    onExpandedChange: () -> Unit,
    onCitySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "City",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF374151),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { onExpandedChange() }
        ) {
            OutlinedTextField(
                value = selectedCity.ifBlank { "Select city" },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFEFEFEF),
                    focusedContainerColor = Color(0xFFEFEFEF),
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedBorderColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { onExpandedChange() }
            ) {
                DropdownMenuItem(
                    text = { Text("All Cities") },
                    onClick = {
                        onCitySelected("")
                        onExpandedChange()
                    }
                )
                cities.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onCitySelected(city)
                            onExpandedChange()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AvailableHotelCard(
    hotel: Hotel,
    voucher: Voucher?,
    uiState: VoucherApplyUiState,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val originalPrice = hotel.minPrice ?: 0.0
    val discountAmount = uiState.calculateDiscountForHotel(hotel, voucher)
    val priceAfterDiscount = uiState.calculatePriceAfterDiscount(hotel, voucher)

    Surface(
        modifier = modifier
            .width(200.dp)
            .height(260.dp), // Increased height for price display
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8F9FA),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFE5E7EB))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Hotel Image
            AsyncImage(
                model = hotel.imageUrl.firstOrNull(), // FIXED: Using firstOrNull()
                contentDescription = hotel.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E7EB)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hotel Name
            Text(
                text = hotel.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Location
            Text(
                text = "${hotel.city}, ${hotel.country}",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(1.dp),
                    color = Color(0xFFFBC40D)
                ) {}

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = hotel.rating.toString(),
                    fontSize = 11.sp,
                    color = Color(0xFF1AB6B6)
                )
            }

            // Price Information
            if (originalPrice > 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Original Price (strikethrough)
                    if (discountAmount > 0) {
                        Text(
                            text = "$$originalPrice",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }

                    // Price After Discount
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$$priceAfterDiscount",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (discountAmount > 0) Color(0xFF059669) else Color(0xFF1F2937)
                        )

                        if (discountAmount > 0) {
                            Text(
                                text = "-$$discountAmount",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Add Button
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "+ Add",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectedHotelCard(
    hotel: Hotel,
    voucher: Voucher?,
    uiState: VoucherApplyUiState,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountAmount = uiState.calculateDiscountForHotel(hotel, voucher)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(127.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE0F2F1),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1AB6B6))
    ) {
        Row(
            modifier = Modifier.padding(14.dp)
        ) {
            // Hotel Image - FIXED: Using firstOrNull()
            AsyncImage(
                model = hotel.imageUrl.firstOrNull(),
                contentDescription = hotel.name,
                modifier = Modifier
                    .size(80.dp, 60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD1D5DB)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Hotel Name
                        Text(
                            text = hotel.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Location
                        Text(
                            text = "${hotel.city}, ${hotel.country}",
                            fontSize = 12.sp,
                            color = Color(0xFF757575),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = RoundedCornerShape(1.dp),
                                color = Color(0xFFFBC40D)
                            ) {}

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = hotel.rating.toString(),
                                fontSize = 11.sp,
                                color = Color(0xFF1AB6B6)
                            )
                        }
                    }

                    // Remove Button
                    Button(
                        onClick = onRemoveClick,
                        modifier = Modifier
                            .width(60.dp)
                            .height(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Remove",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Discount Badge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discount applied",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )

                        Text(
                            text = "- $$discountAmount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalDiscountCard(
    totalDiscount: Double,
    accommodationCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(66.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1AB6B6),
                            Color(0xFF0D9488)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Discount",
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Text(
                        text = "$accommodationCount accommodations",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Text(
                    text = "$$${totalDiscount.toInt()}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

}

@Composable
private fun AppliedHotelsInfoCard(
    appliedCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFFFF3CD), // Light yellow
        border = BorderStroke(1.dp, Color(0xFFFFC107))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Info Icon
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFC107).copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Already Applied",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = "This voucher is already applied to $appliedCount hotel${if (appliedCount > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = Color(0xFF92400E).copy(alpha = 0.8f)
                )
            }

            // Count Badge
            Surface(
                shape = CircleShape,
                color = Color(0xFFF59E0B)
            ) {
                Text(
                    text = appliedCount.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// Helper function to format date
private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val date = timestamp.toDate()
    val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return dateFormat.format(date)
}