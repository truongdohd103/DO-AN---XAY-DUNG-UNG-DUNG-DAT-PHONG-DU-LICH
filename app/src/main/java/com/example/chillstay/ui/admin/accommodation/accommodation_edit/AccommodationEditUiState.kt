package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.PropertyType

@Immutable
data class PolicyUi(
    val title: String = "",
    val content: String = ""
)

@Immutable
data class AccommodationEditUiState(
    val mode: Mode = Mode.Create,
    val hotelId: String? = null,
    val name: String = "",
    val propertyType: PropertyType = PropertyType.HOTEL,
    val description: String = "",
    val address: String = "",
    val country: String = "",
    val city: String = "",
    val coordinate: String = "",
    val images: List<String> = emptyList(),

    // Ảnh người dùng vừa chọn từ thiết bị (chưa upload)
    val localImageUris: List<Uri> = emptyList(),

    val policies: List<PolicyUi> = emptyList(),

    val availableLanguages: List<String> = listOf(
        "English",
        "Vietnamese",
        "Chinese",
        "Spanish",
        "French",
        "Japanese",
        "Indonesian",
        "Italian",
        "Malaysian",
        "Hindi"
    ),
    val selectedLanguages: Set<String> = setOf("English"),

    val availableFacilities: List<String> = listOf(
        "Swimming Pool",
        "Restaurant",
        "Gym",
        "Nightclub",
        "Spa",
        "Beachfront"
    ),

    val availableFeatures: List<String> = listOf(
        "City Center",
        "Nature",
        "Near Airport",
        "Pet Allowed",
        "Shopping District",
        "Smoking Area",
        "Stylish Area",
        "Historic Area",
        "24h Reception"
    ),
    val selectedFeatures: Set<String> = emptySet(),

    val isSaving: Boolean = false,
    val error: String? = null
) : UiState

enum class Mode {
    Create,
    Edit
}

