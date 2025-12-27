package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Hotel

sealed interface AccommodationEditEffect : UiEffect {
    object NavigateBack : AccommodationEditEffect
    data class NavigateToRooms(val hotelId: String?) : AccommodationEditEffect
    data class ShowSaveSuccess(val hotel: Hotel) : AccommodationEditEffect
    data class ShowCreateSuccess(val hotel: Hotel) : AccommodationEditEffect
    data class ShowError(val message: String) : AccommodationEditEffect
}

