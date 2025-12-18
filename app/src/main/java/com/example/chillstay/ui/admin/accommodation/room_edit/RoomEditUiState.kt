package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState

@Immutable
data class RoomEditUiState(
    val mode: Mode = Mode.Create,
    val roomId: String? = null,
    val hotelId: String? = null,
    val roomName: String = "",
    val area: String = "",
    val doubleBeds: String = "",
    val singleBeds: String = "",
    val maxOccupancy: String = "",
    val pricePerNight: String = "",
    val discount: String = "",
    val availableQuantity: String = "",
    val breakfastPrice: String = "",
    val imageUrls: List<String> = emptyList(),
    val availableFeatures: List<String> = listOf(
        "TV",
        "Air Conditioning",
        "Mini Bar",
        "Private Pool",
        "Kitchen",
        "Balcony/Terrace",
        "Washing Machine",
        "Bathtub",
        "Hair Dryer",
        "Electric Kettle",
        "Work Desk",
        "Coffee / Tea Maker",
        "Smoking Allowed",
        "Pay Later",
        "Breakfast Included"
    ),
    val selectedFeatures: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
) : UiState

enum class Mode {
    Create,
    Edit
}

