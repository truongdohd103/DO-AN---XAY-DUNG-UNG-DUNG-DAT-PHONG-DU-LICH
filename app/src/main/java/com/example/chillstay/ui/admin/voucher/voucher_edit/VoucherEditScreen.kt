package com.example.chillstay.ui.admin.voucher.voucher_edit

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherType
import com.example.chillstay.ui.admin.accommodation.accommodation_edit.AccommodationEditEffect
import com.example.chillstay.ui.components.ResponsiveContainer
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherEditScreen(
    voucherId: String? = null,
    onBack: () -> Unit = {},
    onSaved: (Voucher) -> Unit = {},
    onCreated: (Voucher) -> Unit = {},
    onSelectAccommodation: (String) -> Unit = {},
    viewModel: VoucherEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onEvent(VoucherEditIntent.SetImage(it))
        }
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(voucherId) {
        if (voucherId != null) {
            viewModel.onEvent(VoucherEditIntent.LoadForEdit(voucherId))
        } else {
            viewModel.onEvent(VoucherEditIntent.LoadForCreate)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VoucherEditEffect.ShowSaveSuccess -> {
                    Toast.makeText(context, "Voucher updated successfully", Toast.LENGTH_SHORT).show()
                    onSaved(effect.voucher)
                }
                is VoucherEditEffect.ShowCreateSuccess -> {
                    Toast.makeText(context, "Voucher created successfully", Toast.LENGTH_SHORT).show()
                    onCreated(effect.voucher)
                }
                is VoucherEditEffect.NavigateToAccommodations -> effect.voucherId.let(onSelectAccommodation)
                is VoucherEditEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                VoucherEditEffect.NavigateBack -> onBack()
            }
        }
    }

    val headerTitle = if (uiState.mode == Mode.Edit) "Edit Voucher" else "Create Voucher"

    ResponsiveContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = headerTitle,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.onEvent(VoucherEditIntent.NavigateBack) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1AB6B6)
                    )
                )
            },
            containerColor = Color.White,
            bottomBar = {
                BottomActionBar(
                    mode = uiState.mode,
                    isSaving = uiState.isSaving,
                    onSaveClick = { viewModel.onEvent(VoucherEditIntent.Save) },
                    onCreateClick = { viewModel.onEvent(VoucherEditIntent.Save) },
                    onSelectAccommodationClick = { onSelectAccommodation(uiState.voucherId ?: "") },
                    uiState = uiState
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                BasicInformationSection(
                    title = uiState.title,
                    code = uiState.code,
                    description = uiState.description,
                    imageUri = uiState.imageUri,
                    isLoadingImage = uiState.isLoadingImage,
                    onTitleChange = { viewModel.onEvent(VoucherEditIntent.UpdateTitle(it)) },
                    onCodeChange = { viewModel.onEvent(VoucherEditIntent.UpdateCode(it)) },
                    onDescriptionChange = { viewModel.onEvent(VoucherEditIntent.UpdateDescription(it)) },
                    onPickImage = { imagePickerLauncher.launch("image/*") },
                    onRemoveImage = { viewModel.onEvent(VoucherEditIntent.RemoveImage) }
                )

                DiscountValueSection(
                    type = uiState.type,
                    value = uiState.value,
                    onTypeChange = { viewModel.onEvent(VoucherEditIntent.SelectType(it)) },
                    onValueChange = { viewModel.onEvent(VoucherEditIntent.UpdateValue(it)) }
                )

                ValidityPeriodSection(
                    validFrom = uiState.validFrom,
                    validTo = uiState.validTo,
                    onValidFromClick = { showStartDatePicker = true },
                    onValidToClick = { showEndDatePicker = true }
                )

                UsageLimitsSection(
                    maxTotalUsage = uiState.maxTotalUsage,
                    maxUsagePerUser = uiState.maxUsagePerUser,
                    onMaxTotalUsageChange = { viewModel.onEvent(VoucherEditIntent.UpdateMaxTotalUsage(it)) },
                    onMaxUsagePerUserChange = { viewModel.onEvent(VoucherEditIntent.UpdateMaxUsagePerUser(it)) }
                )

                ConditionsSection(
                    minBookingAmount = uiState.minBookingAmount,
                    maxDiscountAmount = uiState.maxDiscountAmount,
                    minNights = uiState.minNights,
                    isStackable = uiState.isStackable,
                    onMinBookingAmountChange = { viewModel.onEvent(VoucherEditIntent.UpdateMinBookingAmount(it)) },
                    onMaxDiscountAmountChange = { viewModel.onEvent(VoucherEditIntent.UpdateMaxDiscountAmount(it)) },
                    onMinNightsChange = { viewModel.onEvent(VoucherEditIntent.UpdateMinNights(it)) },
                    onStackableToggle = { viewModel.onEvent(VoucherEditIntent.ToggleStackable) }
                )

                CustomerSegmentSection(
                    selectedLevel = uiState.requiredUserLevel,
                    onLevelSelect = { viewModel.onEvent(VoucherEditIntent.SelectUserLevel(it)) }
                )

                AdvancedConditionsSection(
                    validDays = uiState.validDays,
                    validTimeSlots = uiState.validTimeSlots,
                    onToggleValidDay = { viewModel.onEvent(VoucherEditIntent.ToggleValidDay(it)) },
                    onToggleTimeSlot = { viewModel.onEvent(VoucherEditIntent.ToggleTimeSlot(it)) }
                )

                // Extra spacing for bottom bar
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { date ->
                viewModel.onEvent(VoucherEditIntent.UpdateValidFrom(date))
                showStartDatePicker = false
            },
            initialDate = uiState.validFrom
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { date ->
                viewModel.onEvent(VoucherEditIntent.UpdateValidTo(date))
                showEndDatePicker = false
            },
            initialDate = uiState.validTo
        )
    }
}


// ==================== Section Components ====================
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFF1AB6B6), CircleShape)
        )
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
    }
}

@Composable
fun FormInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    required: Boolean = false,
    helperText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            },
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1AB6B6),
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )

        helperText?.let {
            Text(
                text = it,
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun BasicInformationSection(
    title: String,
    code: String,
    description: String,
    imageUri: Uri?,
    isLoadingImage: Boolean,
    onTitleChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Basic Information")

        FormInputField(
            label = "Code Name",
            value = title,
            onValueChange = onTitleChange,
            placeholder = "e.g., Summer Sale 2024",
            required = true
        )

        FormInputField(
            label = "Discount Code",
            value = code,
            onValueChange = onCodeChange,
            placeholder = "e.g., SUMMER2024",
            required = true,
            helperText = "Code that customers will enter at checkout"
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(
                    text = "Image",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }

            // Loading indicator
            if (isLoadingImage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFF1AB6B6)
                        )
                        Text(
                            text = "Loading image...",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            } else if (imageUri != null) {
                // Display image with remove button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF3F4F6))
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Voucher Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Remove button
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(alpha = 0.9f))
                            .clickable { onRemoveImage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "×",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // No image - show placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                        .clickable { onPickImage() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Image",
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Click to add image",
                            fontSize = 14.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            // Add Image Button (always show when no image)
            if (imageUri == null || imageUri == Uri.EMPTY) {
                Button(
                    onClick = onPickImage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF3F4F6),
                        contentColor = Color(0xFF1AB6B6)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Select Image",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        FormInputField(
            label = "Description",
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = "Enter discount description",
            singleLine = false,
            maxLines = 4
        )
    }
}

@Composable
fun DiscountValueSection(
    type: VoucherType,
    value: String,
    onTypeChange: (VoucherType) -> Unit,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Discount Value")

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(
                    text = "Discount Type",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeButton(
                    text = "Percentage (%)",
                    isSelected = type == VoucherType.PERCENTAGE,
                    onClick = { onTypeChange(VoucherType.PERCENTAGE) },
                    modifier = Modifier.weight(1f)
                )
                TypeButton(
                    text = "Fixed Amount ($)",
                    isSelected = type == VoucherType.FIXED_AMOUNT,
                    onClick = { onTypeChange(VoucherType.FIXED_AMOUNT) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FormInputField(
            label = "Value",
            value = value,
            onValueChange = onValueChange,
            placeholder = "0",
            required = true,
            keyboardType = KeyboardType.Decimal
        )
    }
}

@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFFF0FDFA) else Color.White)
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF0D9488) else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF0D9488) else Color(0xFF374151)
        )
    }
}

@Composable
fun ValidityPeriodSection(
    validFrom: Date?,
    validTo: Date?,
    onValidFromClick: () -> Unit,
    onValidToClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd/yyyy", Locale.US) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Validity Period")

        DatePickerField(
            label = "Start Date",
            date = validFrom,
            dateFormat = dateFormat,
            onClick = onValidFromClick,
            required = true
        )

        DatePickerField(
            label = "End Date",
            date = validTo,
            dateFormat = dateFormat,
            onClick = onValidToClick,
            required = true
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    date: Date?,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit,
    required: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF374151)
            )
            if (required) {
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .clickable { onClick() }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date?.let { dateFormat.format(it) } ?: "mm/dd/yyyy",
                    fontSize = 14.sp,
                    color = if (date != null) Color.Black else Color(0xFF757575)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar),
                    contentDescription = "Calendar",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UsageLimitsSection(
    maxTotalUsage: String,
    maxUsagePerUser: String,
    onMaxTotalUsageChange: (String) -> Unit,
    onMaxUsagePerUserChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Usage Limits")

        FormInputField(
            label = "Total Usage Limit",
            value = maxTotalUsage,
            onValueChange = onMaxTotalUsageChange,
            placeholder = "Unlimited",
            helperText = "Leave empty for unlimited usage",
            keyboardType = KeyboardType.Number
        )

        FormInputField(
            label = "Usage Limit per Customer",
            value = maxUsagePerUser,
            onValueChange = onMaxUsagePerUserChange,
            placeholder = "1",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun ConditionsSection(
    minBookingAmount: String,
    maxDiscountAmount: String,
    minNights: String,
    isStackable: Boolean,
    onMinBookingAmountChange: (String) -> Unit,
    onMaxDiscountAmountChange: (String) -> Unit,
    onMinNightsChange: (String) -> Unit,
    onStackableToggle: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Conditions")

        FormInputField(
            label = "Minimum Order Value ($)",
            value = minBookingAmount,
            onValueChange = onMinBookingAmountChange,
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal
        )

        FormInputField(
            label = "Maximum Discount Amount ($)",
            value = maxDiscountAmount,
            onValueChange = onMaxDiscountAmountChange,
            placeholder = "0.00",
            helperText = "Optional - Maximum discount cap",
            keyboardType = KeyboardType.Decimal
        )

        FormInputField(
            label = "Minimum Nights",
            value = minNights,
            onValueChange = onMinNightsChange,
            placeholder = "0",
            helperText = "Minimum number of nights for booking",
            keyboardType = KeyboardType.Number
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row {
                Text(
                    text = "Stackable with Other Codes?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF374151)
                )
                Text(
                    text = " *",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TypeButton(
                    text = "Yes",
                    isSelected = isStackable,
                    onClick = { if (!isStackable) onStackableToggle() },
                    modifier = Modifier.weight(1f)
                )
                TypeButton(
                    text = "No",
                    isSelected = !isStackable,
                    onClick = { if (isStackable) onStackableToggle() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomerSegmentSection(
    selectedLevel: String?,
    onLevelSelect: (String?) -> Unit
) {
    val levels = listOf("Bronze", "Silver", "Gold", "All")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader("Customer Segment")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            levels.forEach { level ->
                val isSelected = when {
                    level == "All" -> selectedLevel == null
                    else -> selectedLevel == level
                }

                SegmentChip(
                    text = level,
                    isSelected = isSelected,
                    onClick = {
                        onLevelSelect(if (level == "All") null else level)
                    }
                )
            }
        }
    }
}

@Composable
fun SegmentChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFFF0FDFA) else Color.White)
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF0D9488) else Color(0xFFD1D5DB),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) Color(0xFF0D9488) else Color(0xFF374151)
        )
    }
}

@Composable
fun AdvancedConditionsSection(
    validDays: Set<String>,
    validTimeSlots: Set<String>,
    onToggleValidDay: (String) -> Unit,
    onToggleTimeSlot: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader("Advanced Conditions (Optional)")
            Text(
                text = if (isExpanded) "▼" else "▶",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1AB6B6)
            )
        }

        if (isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Valid Days (Leave empty for all days)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        days.forEach { day ->
                            SegmentChip(
                                text = day.take(3),
                                isSelected = validDays.contains(day),
                                onClick = { onToggleValidDay(day) }
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Valid Time Slots (Leave empty for all times)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF374151)
                    )

                    val slots = listOf("MORNING", "AFTERNOON", "EVENING")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        slots.forEach { slot ->
                            SegmentChip(
                                text = slot,
                                isSelected = validTimeSlots.contains(slot),
                                onClick = { onToggleTimeSlot(slot) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==================== Fixed Bottom Action Bar ====================
@Composable
fun BottomActionBar(
    mode: Mode,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onCreateClick: () -> Unit,
    onSelectAccommodationClick: () -> Unit,
    uiState: VoucherEditUiState
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                1.dp,
                Color(0xFFE5E7EB),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .padding(20.dp)
    ) {
        if (mode == Mode.Edit) {
            // Edit mode: 2 buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(51.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB5B5)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSaving
                ) {
                    Text(
                        text = "Save",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Button(
                    onClick = onSelectAccommodationClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(51.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0EA5E9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Accom",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            // Create mode: 1 button
            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1AB6B6)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Create",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (Date) -> Unit,
    initialDate: Date?
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate?.time ?: System.currentTimeMillis()
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onDateSelected(Date(millis))
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            DatePicker(state = datePickerState)
        }
    )
}