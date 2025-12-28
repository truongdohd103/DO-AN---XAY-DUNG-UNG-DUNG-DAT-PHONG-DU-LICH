package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.Room
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomEditScreen(
    hotelId: String? = null,
    roomId: String? = null,
    onBackClick: () -> Unit = {},
    onCreateClick: (Room) -> Unit = {},
    onSaveClick: (Room) -> Unit = {},
    viewModel: RoomEditViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(roomId, hotelId) {
        if (roomId != null) {
            viewModel.onEvent(RoomEditIntent.LoadForEdit(roomId))
        } else if (hotelId != null) {
            viewModel.onEvent(RoomEditIntent.LoadForCreate(hotelId))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RoomEditEffect.NavigateBack -> onBackClick()
                is RoomEditEffect.ShowSaveSuccess -> onSaveClick(effect.room)
                is RoomEditEffect.ShowCreateSuccess -> onCreateClick(effect.room)
                is RoomEditEffect.ShowError -> {
                    // Can surface snackbar/toast here if needed
                }
            }
        }
    }

    val headerTitle = if (uiState.mode == Mode.Edit) "Edit Room" else "Create Room"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = headerTitle,
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.onEvent(RoomEditIntent.NavigateBack) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1AB6B6)
            )
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Basic Information
            FormSection(title = "Basic Information") {
                InputField(
                    label = "Room Name",
                    value = uiState.roomName,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateRoomName(it)) },
                    placeholder = "Enter room name",
                    required = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                InputField(
                    label = "Area (m²)",
                    value = uiState.area,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateArea(it)) },
                    placeholder = "0",
                    required = true,
                    helperText = "Room area in square meters",
                    keyboardType = KeyboardType.Decimal
                )
            }

            // Bed Configuration
            FormSection(title = "Bed Configuration") {
                InputField(
                    label = "Double Beds",
                    value = uiState.doubleBeds,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateDoubleBeds(it)) },
                    placeholder = "0",
                    required = true,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    label = "Single Beds",
                    value = uiState.singleBeds,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateSingleBeds(it)) },
                    placeholder = "0",
                    required = true,
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                InputField(
                    label = "Max Occupancy",
                    value = uiState.maxOccupancy,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateMaxOccupancy(it)) },
                    placeholder = "0",
                    required = true,
                    helperText = "Maximum number of guests",
                    keyboardType = KeyboardType.Number
                )
            }

            // Pricing
            FormSection(title = "Pricing") {
                InputField(
                    label = "Price per Night",
                    value = uiState.pricePerNight,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdatePricePerNight(it)) },
                    placeholder = "0.00",
                    required = true,
                    helperText = "Price in USD",
                    keyboardType = KeyboardType.Decimal
                )

                Spacer(modifier = Modifier.height(12.dp))

                InputField(
                    label = "Discount (%)",
                    value = uiState.discount,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateDiscount(it)) },
                    placeholder = "0",
                    required = false,
                    helperText = "Discount percentage (0-100)",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(12.dp))

                InputField(
                    label = "Available Quantity",
                    value = uiState.availableQuantity,
                    onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateAvailableQuantity(it)) },
                    placeholder = "0",
                    required = true,
                    helperText = "Number of rooms available",
                    keyboardType = KeyboardType.Number
                )
            }

            // Room Images
            FormSection(title = "Room Images") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.imageUrls.forEachIndexed { index, url ->
                        ImageUrlItem(
                            url = url,
                            onDeleteClick = { viewModel.onEvent(RoomEditIntent.RemoveImage(index)) }
                        )
                    }

                    OutlinedButton(
                        onClick = { /* Add image URL dialog - TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF1AB6B6)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFF1AB6B6),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add Image URL",
                            style = TextStyle(
                                fontSize = 12.6.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1AB6B6)
                            )
                        )
                    }
                }
            }

            // Room Features
            FormSection(title = "Room Features") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.availableFeatures.forEach { feature ->
                        FeatureChip(
                            text = feature,
                            isSelected = uiState.selectedFeatures.contains(feature),
                            onClick = { viewModel.onEvent(RoomEditIntent.ToggleFeature(feature)) }
                        )
                    }
                }
            }

            // Breakfast Options
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF0FDFA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFF99F6E4),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.size(18.dp))
                        Text(
                            text = "Breakfast Options",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        )
                    }

                    InputField(
                        label = "Breakfast Price (per person)",
                        value = uiState.breakfastPrice,
                        onValueChange = { viewModel.onEvent(RoomEditIntent.UpdateBreakfastPrice(it)) },
                        placeholder = "0.00",
                        required = false,
                        helperText = "Only applicable if \"Breakfast Included\" is not selected. Leave\nempty if breakfast is free or not available.",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            // Save/Create Button
            Button(
                onClick = {
                    if (uiState.mode == Mode.Edit) {
                        viewModel.onEvent(RoomEditIntent.Save)
                    } else {
                        viewModel.onEvent(RoomEditIntent.Create)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = Color(0xFF1AB6B6).copy(alpha = 0.3f)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.mode == Mode.Edit) "Save" else "Create",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

        }
    }
}

@Composable
fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(18.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            )
        }

        Column(content = content)
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    required: Boolean,
    helperText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
            )
            if (required) {
                Text(
                    text = " *",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 38.dp)
                .padding(horizontal = 12.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                )
            },
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF212121)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFD1D5DB),
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )

        if (helperText != null) {
            Text(
                text = helperText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF6B7280)
                )
            )
        }
    }
}

@Composable
fun ImageUrlItem(
    url: String,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = Color(0xFFE5E7EB),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = url,
            style = TextStyle(
                fontSize = 12.sp,
                color = Color(0xFF757575)
            ),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = Color(0xFFEF4444),
                    shape = RoundedCornerShape(6.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeatureChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(33.dp)
            .background(
                color = if (isSelected) Color(0xFF1AB6B6) else Color.White,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF1AB6B6) else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 13.sp,
                color = if (isSelected) Color.White else Color(0xFF374151)
            )
        )
    }
}

