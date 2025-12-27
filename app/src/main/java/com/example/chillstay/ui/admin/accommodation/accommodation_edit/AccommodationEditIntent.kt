package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.net.Uri
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.PropertyType

sealed interface AccommodationEditIntent : UiEvent {
    data object LoadForCreate : AccommodationEditIntent
    data class LoadForEdit(val hotelId: String) : AccommodationEditIntent

    data class UpdateName(val value: String) : AccommodationEditIntent
    data class UpdateType(val value: PropertyType) : AccommodationEditIntent
    data class UpdateDescription(val value: String) : AccommodationEditIntent
    data class UpdateFullAddress(val value: String) : AccommodationEditIntent
    data class UpdateCountry(val value: String) : AccommodationEditIntent
    data class UpdateCity(val value: String) : AccommodationEditIntent
    data class UpdateCoordinate(val value: String) : AccommodationEditIntent

    data class AddImages(val uris: List<Uri>) : AccommodationEditIntent

    data class RemoveImage(val index: Int) : AccommodationEditIntent
    // Ảnh mới chọn từ thiết bị (URI tạm thời)

    data object AddPolicy : AccommodationEditIntent
    data class UpdatePolicyTitle(val index: Int, val value: String) : AccommodationEditIntent
    data class UpdatePolicyContent(val index: Int, val value: String) : AccommodationEditIntent
    data class RemovePolicy(val index: Int) : AccommodationEditIntent

    data class ToggleLanguage(val value: String) : AccommodationEditIntent
    data class ToggleFacility(val value: String) : AccommodationEditIntent
    data class ToggleFeature(val value: String) : AccommodationEditIntent

    data object Save : AccommodationEditIntent
    data object Create : AccommodationEditIntent
    data object OpenRooms : AccommodationEditIntent
    data object NavigateBack : AccommodationEditIntent
    data object ClearError : AccommodationEditIntent
}

