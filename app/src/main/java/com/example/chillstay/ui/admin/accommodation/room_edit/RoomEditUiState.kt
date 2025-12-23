package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState

@Immutable
data class RoomEditUiState(
    val mode: Mode = Mode.Create,
    val roomId: String? = null,
    val hotelId: String? = null,
    val name: String = "",
    val area: String = "",
    val doubleBed: String = "",
    val singleBed: String = "",
    val availableQuantity: String = "",
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
    val breakfastPrice: String = "",
    val pricePerNight: String = "",
    val discount: String = "",
    val maxOccupancy: String = "",
    val exteriorView: List<String> = emptyList(),
    val dining: List<String> = emptyList(),
    val thisRoom: List<String> = emptyList(),
    val selectedFeatures: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null
) : UiState

enum class Mode {
    Create,
    Edit
}