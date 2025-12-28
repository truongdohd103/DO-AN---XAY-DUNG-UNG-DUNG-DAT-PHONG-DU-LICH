package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.net.Uri
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.PropertyType

// AccommodationEditUiState and Mode are defined in AccommodationEditUiState.kt

sealed interface AccommodationEditIntent {
    data object LoadForCreate : AccommodationEditIntent
    data class LoadForEdit(val hotelId: String) : AccommodationEditIntent
    
    data class UpdateName(val name: String) : AccommodationEditIntent
    data class UpdateDescription(val description: String) : AccommodationEditIntent
    data class UpdateType(val type: PropertyType) : AccommodationEditIntent
    
    data class UpdateFullAddress(val address: String) : AccommodationEditIntent
    data class UpdateCountry(val country: String) : AccommodationEditIntent
    data class UpdateCity(val city: String) : AccommodationEditIntent
    data class UpdateCoordinate(val coordinate: String) : AccommodationEditIntent
    
    data class SetLocalImages(val uris: List<Uri>) : AccommodationEditIntent
    data class RemoveLocalImage(val index: Int) : AccommodationEditIntent
    data class RemoveImage(val index: Int) : AccommodationEditIntent // Remove remote image
    
    data object AddPolicy : AccommodationEditIntent
    data class RemovePolicy(val index: Int) : AccommodationEditIntent
    data class UpdatePolicyTitle(val index: Int, val title: String) : AccommodationEditIntent
    data class UpdatePolicyContent(val index: Int, val content: String) : AccommodationEditIntent
    
    data class ToggleLanguage(val language: String) : AccommodationEditIntent
    data class ToggleFacility(val facility: String) : AccommodationEditIntent
    data class ToggleFeature(val feature: String) : AccommodationEditIntent
    
    data object Save : AccommodationEditIntent
    data object Create : AccommodationEditIntent
    
    data object OpenRooms : AccommodationEditIntent
    data object NavigateBack : AccommodationEditIntent
}

sealed interface AccommodationEditEffect {
    data class NavigateToRooms(val hotelId: String?) : AccommodationEditEffect
    data object NavigateBack : AccommodationEditEffect
    data class ShowSaveSuccess(val hotel: Hotel) : AccommodationEditEffect
    data class ShowCreateSuccess(val hotel: Hotel) : AccommodationEditEffect
    data class ShowError(val message: String) : AccommodationEditEffect
}
