package com.example.chillstay.ui.admin.accommodation.accommodation_edit

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.PropertyType
import org.koin.androidx.compose.koinViewModel

@Suppress("DUPLICATE_BRANCH_CONDITION_IN_WHEN")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationEditScreen(
    hotelId: String? = null,
    onBack: () -> Unit = {},
    onSaved: (Hotel) -> Unit = {},
    onCreated: (Hotel) -> Unit = {},
    onOpenRooms: (String) -> Unit = {},
    viewModel: AccommodationEditViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(AccommodationEditIntent.SetLocalImages(uris))
        }
    }

    LaunchedEffect(hotelId) {
        viewModel.onEvent(
            if (hotelId == null) AccommodationEditIntent.LoadForCreate
            else AccommodationEditIntent.LoadForEdit(hotelId)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AccommodationEditEffect.NavigateToRooms -> effect.hotelId?.let(onOpenRooms)
                AccommodationEditEffect.NavigateBack -> onBack()
                is AccommodationEditEffect.ShowSaveSuccess -> {
                    Toast.makeText(context, "Accommodation updated successfully", Toast.LENGTH_SHORT).show()
                    onSaved(effect.hotel)
                }
                is AccommodationEditEffect.ShowCreateSuccess -> {
                    Toast.makeText(context, "Accommodation created successfully", Toast.LENGTH_SHORT).show()
                    onCreated(effect.hotel)
                }
                is AccommodationEditEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                AccommodationEditEffect.NavigateBack ->
                    onBack()
            }
        }
    }

    val headerTitle =
        if (uiState.mode == Mode.Edit) "Edit Accommodation" else "Create Accommodation"

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        val contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
            top = 0.dp,
            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = innerPadding.calculateBottomPadding()
        )

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .background(Color.White)
                .shadow(
                    40.dp,
                    RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(Color(0xFF1AB5B5))
                    .clip(RoundedCornerShape(20.dp))
                    .border(0.5.dp, Color(0xFFF0F0F0), RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.onEvent(AccommodationEditIntent.NavigateBack) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                    Text(
                        text = headerTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 30.sp
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 124.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                // Basic Information Section
                BasicInformationSection(
                    state = uiState,
                    onNameChange = { viewModel.onEvent(AccommodationEditIntent.UpdateName(it)) },
                    onTypeToggle = {
                        val nextType =
                            if (uiState.propertyType == PropertyType.HOTEL) PropertyType.RESORT else PropertyType.HOTEL
                        viewModel.onEvent(AccommodationEditIntent.UpdateType(nextType))
                    },
                    onDescriptionChange = {
                        viewModel.onEvent(
                            AccommodationEditIntent.UpdateDescription(
                                it
                            )
                        )
                    }
                )

                // Location Section
                LocationSection(
                    state = uiState,
                    onAddressChange = {
                        viewModel.onEvent(
                            AccommodationEditIntent.UpdateFullAddress(
                                it
                            )
                        )
                    },
                    onCountryChange = { viewModel.onEvent(AccommodationEditIntent.UpdateCountry(it)) },
                    onCityChange = { viewModel.onEvent(AccommodationEditIntent.UpdateCity(it)) },
                    onCoordinateChange = {
                        viewModel.onEvent(
                            AccommodationEditIntent.UpdateCoordinate(
                                it
                            )
                        )
                    }
                )

                // Images Section
                ImagesSection(
                    images = uiState.images,
                    localImages = uiState.localImageUris,
                    onPickImages = { imagePickerLauncher.launch("image/*") },
                    onRemoveLocal = { index ->
                        viewModel.onEvent(AccommodationEditIntent.RemoveLocalImage(index))
                    },
                    onRemoveImage = { index ->
                        viewModel.onEvent(AccommodationEditIntent.RemoveImage(index))
                    }
                )

                // Policies Section
                PoliciesSection(
                    policies = uiState.policies,
                    onAdd = { viewModel.onEvent(AccommodationEditIntent.AddPolicy) },
                    onRemove = { viewModel.onEvent(AccommodationEditIntent.RemovePolicy(it)) },
                    onTitleChange = { index, value ->
                        viewModel.onEvent(AccommodationEditIntent.UpdatePolicyTitle(index, value))
                    },
                    onContentChange = { index, value ->
                        viewModel.onEvent(AccommodationEditIntent.UpdatePolicyContent(index, value))
                    }
                )

                // Languages Section
                LanguagesSection(
                    languages = uiState.availableLanguages,
                    selected = uiState.selectedLanguages,
                    onToggle = { viewModel.onEvent(AccommodationEditIntent.ToggleLanguage(it)) }
                )

                // Facilities Section
                FacilitiesSection(
                    facilities = uiState.availableFacilities,
                    selected = uiState.selectedFacilities,
                    onToggle = { viewModel.onEvent(AccommodationEditIntent.ToggleFacility(it)) }
                )

                // Features Section
                FeaturesSection(
                    features = uiState.availableFeatures,
                    selected = uiState.selectedFeatures,
                    onToggle = { viewModel.onEvent(AccommodationEditIntent.ToggleFeature(it)) }
                )

                if (uiState.mode == Mode.Edit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.onEvent(AccommodationEditIntent.Save) },
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
                            onClick = { viewModel.onEvent(AccommodationEditIntent.OpenRooms) },
                            modifier = Modifier
                                .weight(1f)
                                .height(51.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0EA5E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Room",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.onEvent(AccommodationEditIntent.Create) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(51.dp)
                            .shadow(
                                12.dp,
                                RoundedCornerShape(12.dp),
                                spotColor = Color(0xFF1AB5B5).copy(alpha = 0.3f)
                            ),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB5B5)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !uiState.isSaving
                    ) {
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
    }
}

@Composable
private fun BasicInformationSection(
    state: AccommodationEditUiState,
    onNameChange: (String) -> Unit,
    onTypeToggle: () -> Unit,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Basic Information")

        InputField(
            label = "Hotel Name *",
            placeholder = "Enter hotel name",
            value = state.name,
            onValueChange = onNameChange
        )

        DropdownField(
            label = "Type *",
            value = state.propertyType.name,
            onClick = onTypeToggle
        )

        InputField(
            label = "Description *",
            placeholder = "Enter property description",
            value = state.description,
            onValueChange = onDescriptionChange,
            minHeight = 80.dp
        )
    }
}

@Composable
private fun LocationSection(
    state: AccommodationEditUiState,
    onAddressChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onCoordinateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Location")

        InputField(
            label = "Full Address *",
            placeholder = "Enter complete address",
            value = state.address,
            onValueChange = onAddressChange,
            minHeight = 80.dp
        )

        InputField(
            label = "Country *",
            placeholder = "Select country",
            value = state.country,
            onValueChange = onCountryChange
        )

        InputField(
            label = "City *",
            placeholder = "Select city",
            value = state.city,
            onValueChange = onCityChange
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Coordinates *",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF383E52)
            )
            OutlinedTextField(
                value = state.coordinate,
                onValueChange = onCoordinateChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFD1D5DB),
                    focusedBorderColor = Color(0xFF1AB5B5)
                ),
                shape = RoundedCornerShape(8.dp)
            )
            Text(
                text = "Get from Google Maps (format: latitude,longitude)",
                fontSize = 12.sp,
                color = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
private fun ImagesSection(
    images: List<String>,
    localImages: List<Uri>,
    onPickImages: () -> Unit,
    onRemoveLocal: (Int) -> Unit,
    onRemoveImage: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Images")

        // Ảnh URL đã lưu (từ Firestore / Cloudinary)
        if (images.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                images.forEachIndexed { index, url ->
                    ImageItem(url = url, onRemove = { onRemoveImage(index) })
                }
            }
        }

        // Ảnh người dùng vừa chọn từ máy (URI)
        if (localImages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                localImages.forEachIndexed { index, uri ->
                    ImageItem(url = uri.toString(), onRemove = { onRemoveLocal(index) })
                }
            }
        }

        // Nút chọn ảnh mới từ thiết bị
        AddButton(text = "Pick images from device", onClick = onPickImages)
    }
}

@Composable
private fun ImageItem(url: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = url,
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(Color(0xFFF04545), RoundedCornerShape(6.dp))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "−", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PoliciesSection(
    policies: List<PolicyUi>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onTitleChange: (Int, String) -> Unit,
    onContentChange: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Policies")

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            policies.forEachIndexed { index, policy ->
                PolicyCard(
                    title = policy.title,
                    content = policy.content,
                    onTitleChange = { onTitleChange(index, it) },
                    onContentChange = { onContentChange(index, it) },
                    onRemove = { onRemove(index) }
                )
            }
        }

        AddButton(text = "Add Policy", onClick = onAdd)
    }
}

@Composable
private fun PolicyCard(
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(6.dp)),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF757575)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedBorderColor = Color(0xFF1AB5B5)
            ),
            shape = RoundedCornerShape(6.dp)
        )

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White, RoundedCornerShape(6.dp)),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 13.sp,
                color = Color(0xFF757575)
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedBorderColor = Color(0xFF1AB5B5)
            ),
            shape = RoundedCornerShape(6.dp)
        )

        Row(
            modifier = Modifier
                .background(Color(0xFFF04545), RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable { onRemove() },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remove",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun LanguagesSection(
    languages: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.heightIn(min = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Languages")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            languages.forEach { language ->
                FeatureChip(
                    text = language,
                    isSelected = selected.contains(language),
                    onClick = { onToggle(language) }
                )
            }
        }
    }
}

@Composable
private fun FacilitiesSection(
    facilities: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.heightIn(min = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Facilities")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            facilities.forEach { f ->
                FeatureChip(
                    text = f,
                    isSelected = selected.contains(f),
                    onClick = { onToggle(f) }
                )
            }
        }
    }
}

@Composable
private fun FeaturesSection(
    features: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier.heightIn(min = 120.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Features")

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            features.forEach { feature ->
                FeatureChip(
                    text = feature,
                    isSelected = selected.contains(feature),
                    onClick = { onToggle(feature) }
                )
            }
        }
    }
}

@Composable
private fun FeatureChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) Color(0xFF1AB5B5) else Color.White)
            .border(0.5.dp, if (isSelected) Color(0xFF1AB5B5) else Color(0xFFD1D5DB), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clickable { onClick() }
            // giới hạn chiều rộng tối đa để không kéo layout (tùy chỉnh theo UI)
            .widthIn(max = 160.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (isSelected) Color.White else Color(0xFF383E52),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis

        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(18.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937)
        )
    }
}

@Composable
private fun InputField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    minHeight: Dp = 0.dp
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF383E52)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)
                .background(Color.White, RoundedCornerShape(8.dp)),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD1D5DB),
                focusedBorderColor = Color(0xFF1AB5B5)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF383E52)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF0F0F0), RoundedCornerShape(8.dp))
                .border(0.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clickable { onClick() }
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AddButton(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
            .border(0.5.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
            .padding(10.dp)
            .clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(16.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1AB5B5)
        )
    }
}