package com.example.chillstay.ui.admin.accommodation

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.ui.components.MarqueeText
import org.koin.androidx.compose.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationManageScreen(
    onNavigateBack: () -> Unit = {},
    onCreateNew: () -> Unit = {},
    onEdit: (Hotel) -> Unit = {},
    onInvalidate: (Hotel) -> Unit = {},
    onDelete: (Hotel) -> Unit = {},
    onViewAll: () -> Unit = {},
    viewModel: AccommodationManageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tealColor = Color(0xFF1AB5B5)
    val lightGray = Color(0xFFF5F5F5)

    LaunchedEffect(Unit) {
        viewModel.onEvent(AccommodationManageIntent.LoadHotels)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccommodationManageEffect.NavigateBack -> onNavigateBack()
                is AccommodationManageEffect.NavigateToCreateNew -> onCreateNew()
                is AccommodationManageEffect.NavigateToEdit -> onEdit(effect.hotel)
                is AccommodationManageEffect.ShowInvalidateSuccess -> onInvalidate(effect.hotel)
                is AccommodationManageEffect.ShowDeleteSuccess -> onDelete(effect.hotel)
                is AccommodationManageEffect.ShowError -> {
                    // Handle error if needed
                }
                is AccommodationManageEffect.NavigateToViewAll -> onViewAll()
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
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Accommodation",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
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
                        CircularProgressIndicator()
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
                                onClick = { viewModel.onEvent(AccommodationManageIntent.LoadHotels) }
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
                        // Search Bar
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onEvent(AccommodationManageIntent.SearchQueryChanged(it)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(63.dp),
                            placeholder = {
                                Text(
                                    "Search",
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
                                onSearch = { viewModel.onEvent(AccommodationManageIntent.PerformSearch) }
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

                        // Filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Country Filter
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Country",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF383F51),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = uiState.isCountryExpanded,
                                    onExpandedChange = { viewModel.onEvent(AccommodationManageIntent.ToggleCountryDropdown) }
                                ) {
                                    OutlinedTextField(
                                        value = uiState.selectedCountry.ifEmpty { "All" },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                modifier = Modifier.rotate(
                                                    if (uiState.isCountryExpanded) 180f else 0f
                                                )
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = lightGray,
                                            unfocusedContainerColor = lightGray,
                                            disabledContainerColor = lightGray,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = uiState.isCountryExpanded,
                                        onDismissRequest = { viewModel.onEvent(AccommodationManageIntent.ToggleCountryDropdown) }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All") },
                                            onClick = {
                                                viewModel.onEvent(AccommodationManageIntent.CountryChanged(""))
                                                viewModel.onEvent(AccommodationManageIntent.ToggleCountryDropdown)
                                            }
                                        )
                                        uiState.availableCountries.forEach { country ->
                                            DropdownMenuItem(
                                                text = { Text(country) },
                                                onClick = {
                                                    viewModel.onEvent(AccommodationManageIntent.CountryChanged(country))
                                                    viewModel.onEvent(AccommodationManageIntent.ToggleCountryDropdown)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // City Filter
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "City",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF383F51),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )

                                ExposedDropdownMenuBox(
                                    expanded = uiState.isCityExpanded,
                                    onExpandedChange = { viewModel.onEvent(AccommodationManageIntent.ToggleCityDropdown) }
                                ) {
                                    OutlinedTextField(
                                        value = uiState.selectedCity.ifEmpty { "All" },
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .menuAnchor()
                                            .fillMaxWidth()
                                            .height(56.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                modifier = Modifier.rotate(
                                                    if (uiState.isCityExpanded) 180f else 0f
                                                )
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = lightGray,
                                            unfocusedContainerColor = lightGray,
                                            disabledContainerColor = lightGray,
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    ExposedDropdownMenu(
                                        expanded = uiState.isCityExpanded,
                                        onDismissRequest = { viewModel.onEvent(AccommodationManageIntent.ToggleCityDropdown) }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("All") },
                                            onClick = {
                                                viewModel.onEvent(AccommodationManageIntent.CityChanged(""))
                                                viewModel.onEvent(AccommodationManageIntent.ToggleCityDropdown)
                                            }
                                        )
                                        uiState.availableCities.forEach { city ->
                                            DropdownMenuItem(
                                                text = { Text(city) },
                                                onClick = {
                                                    viewModel.onEvent(AccommodationManageIntent.CityChanged(city))
                                                    viewModel.onEvent(AccommodationManageIntent.ToggleCityDropdown)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Statistics - CẬP NHẬT: Giờ đây hiển thị theo filtered results
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatisticCard(
                                title = "Total Properties",
                                value = uiState.totalProperties.toString(),
                                backgroundColor = Color(0xFFE3F2FD),
                                textColor = Color(0xFF1976D2),
                                modifier = Modifier.weight(1f)
                            )

                            StatisticCard(
                                title = "Active Properties",
                                value = uiState.activeProperties.toString(),
                                backgroundColor = Color(0xFFE8F5E9),
                                textColor = Color(0xFF4CAF50),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Hotel List
                        uiState.paginatedHotels.forEach { hotel ->
                            HotelCard(
                                hotel = hotel,
                                tealColor = tealColor,
                                onEdit = { viewModel.onEvent(AccommodationManageIntent.EditHotel(hotel)) },
                                onInvalidate = { viewModel.onEvent(AccommodationManageIntent.InvalidateHotel(hotel)) },
                                onDelete = { viewModel.onEvent(AccommodationManageIntent.DeleteHotel(hotel)) }
                            )
                        }

                        // Pagination
                        if (uiState.hotels.isNotEmpty()) {
                            PaginationControls(
                                currentPage = uiState.currentPage,
                                totalPages = uiState.totalPages,
                                onPageChange = { viewModel.onEvent(AccommodationManageIntent.GoToPage(it)) },
                                onPreviousPage = { viewModel.onEvent(AccommodationManageIntent.PreviousPage) },
                                onNextPage = { viewModel.onEvent(AccommodationManageIntent.NextPage) },
                                tealColor = tealColor
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.onEvent(AccommodationManageIntent.CreateNew) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = tealColor
        ) {
            Text(
                text = "+",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatisticCard(
    title: String,
    value: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
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
                color = textColor.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HotelCard(
    hotel: Hotel,
    tealColor: Color,
    onEdit: () -> Unit,
    onInvalidate: () -> Unit,
    onDelete: () -> Unit
) {
    val originalPrice = hotel.minPrice ?: 0.0
    val discountPercent = 25
    val discountedPrice = BigDecimal.valueOf( originalPrice * (100 - discountPercent / 100.0))
        .setScale(2, RoundingMode.HALF_UP)
        .toDouble()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Images
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val images = hotel.imageUrl
            if (images.isNotEmpty()) {
                AsyncImage(
                    model = images[0],
                    contentDescription = hotel.name,
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                if (images.size > 1) {
                    AsyncImage(
                        model = images[1],
                        contentDescription = hotel.name,
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                            .background(
                                Color(0xFF804549).copy(alpha = 0.5f),
                                RoundedCornerShape(20.dp)
                            )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .background(
                            Color(0xFF804549).copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .background(
                            Color(0xFF804549).copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                )
            }
        }

        // Info Section với Marquee Text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MarqueeText(text = hotel.name, modifier = Modifier.fillMaxWidth(0.9f), 16.sp, Color(0xFF212121))
                MarqueeText(text =  "${hotel.city}, ${hotel.country}", modifier = Modifier.fillMaxWidth(0.9f), 12.sp, Color(0xFF212121))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1f", hotel.rating),
                        fontSize = 12.sp,
                        color = tealColor
                    )

                    Text(
                        text = "(${hotel.numberOfReviews} reviews)",
                        fontSize = 12.sp,
                        color = Color(0xFF767676)
                    )
                }
            }

            // Price Section
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$$originalPrice/night",
                        fontSize = 8.sp,
                        color = Color(0xFF767676),
                        textDecoration = TextDecoration.LineThrough
                    )

                    Text(
                        text = "- $discountPercent%",
                        fontSize = 8.sp,
                        color = Color(0xFFFF4A4A)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$$discountedPrice",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = tealColor
                    )

                    Text(
                        text = "/night",
                        fontSize = 11.sp,
                        color = Color(0xFF767676)
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionButton(
                text = "Edit",
                backgroundColor = Color(0xFF45A3F0),
                modifier = Modifier.weight(1f),
                onClick = onEdit
            )

            ActionButton(
                text = "Invalid",
                backgroundColor = Color(0xFFF59E0A),
                modifier = Modifier.weight(1f),
                onClick = onInvalidate
            )

            ActionButton(
                text = "Delete",
                backgroundColor = Color(0xFFF04545),
                modifier = Modifier.weight(1f),
                onClick = onDelete
            )
        }
    }
}

@Composable
fun ActionButton(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

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