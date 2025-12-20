package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.model.PropertyType
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.image.UploadAccommodationImagesUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class AccommodationEditViewModel(
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val uploadAccommodationImagesUseCase: UploadAccommodationImagesUseCase
) :
    BaseViewModel<AccommodationEditUiState, AccommodationEditIntent, AccommodationEditEffect>(
        AccommodationEditUiState()
    ) {

    companion object {
        private const val LOG_TAG = "ChillStayImageUpload"
    }

    val uiState = state

    override fun onEvent(event: AccommodationEditIntent) {
        when (event) {
            AccommodationEditIntent.LoadForCreate -> resetForCreate()
            is AccommodationEditIntent.LoadForEdit -> loadHotelById(event.hotelId)

            is AccommodationEditIntent.UpdateName -> _state.value = _state.value.copy(name = event.value)
            is AccommodationEditIntent.UpdateType -> _state.value = _state.value.copy(propertyType = event.value)
            is AccommodationEditIntent.UpdateDescription -> _state.value = _state.value.copy(description = event.value)
            is AccommodationEditIntent.UpdateFullAddress -> _state.value = _state.value.copy(address = event.value)
            is AccommodationEditIntent.UpdateCountry -> _state.value = _state.value.copy(country = event.value)
            is AccommodationEditIntent.UpdateCity -> _state.value = _state.value.copy(city = event.value)
            is AccommodationEditIntent.UpdateCoordinate -> _state.value = _state.value.copy(coordinate = event.value)

            is AccommodationEditIntent.AddImage -> addImage(event.url)
            is AccommodationEditIntent.RemoveImage -> removeImage(event.index)
            is AccommodationEditIntent.SetLocalImages -> {
                _state.value = _state.value.copy(localImageUris = event.uris)
            }
            is AccommodationEditIntent.RemoveLocalImage -> {
                val current = _state.value.localImageUris.toMutableList()
                if (event.index in current.indices) {
                    current.removeAt(event.index)
                    _state.value = _state.value.copy(localImageUris = current)
                }
            }

            AccommodationEditIntent.AddPolicy -> addPolicy()
            is AccommodationEditIntent.UpdatePolicyTitle -> updatePolicyTitle(event.index, event.value)
            is AccommodationEditIntent.UpdatePolicyContent -> updatePolicyContent(event.index, event.value)
            is AccommodationEditIntent.RemovePolicy -> removePolicy(event.index)

            is AccommodationEditIntent.ToggleLanguage -> toggleLanguage(event.value)
            is AccommodationEditIntent.ToggleFacility -> toggleFacility(event.value)
            is AccommodationEditIntent.ToggleFeature -> toggleFeature(event.value)

            AccommodationEditIntent.Save -> saveHotel()
            AccommodationEditIntent.Create -> createHotel()
            AccommodationEditIntent.OpenRooms -> openRooms()
            AccommodationEditIntent.NavigateBack -> navigateBack()
            AccommodationEditIntent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun resetForCreate() {
        _state.value = AccommodationEditUiState()
    }

    private fun loadHotelById(hotelId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, mode = Mode.Edit, hotelId = hotelId)
            val result = getHotelByIdUseCase(hotelId).first()
            when (result) {
                is Result.Success -> {
                    val hotel = result.data
                    _state.value = _state.value.copy(
                        isSaving = false,
                        mode = Mode.Edit,
                        hotelId = hotel.id,
                        name = hotel.name,
                        propertyType = hotel.propertyType,
                        description = hotel.description,
                        address = hotel.formattedAddress,
                        country = hotel.country,
                        city = hotel.city,
                        coordinate = "${hotel.coordinate.latitude},${hotel.coordinate.longitude}",
                        images = hotel.imageUrl,
                        policies = hotel.policy.map { PolicyUi(title = it.title, content = it.content) },
                        selectedLanguages = hotel.language.toSet(),
                        selectedFeatures = hotel.feature.toSet()
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isSaving = false)
                    sendEffect {
                        AccommodationEditEffect.ShowError(
                            result.throwable.message ?: "Failed to load hotel"
                        )
                    }
                }
            }
        }
    }

    private fun addImage(url: String) {
        if (url.isBlank()) return
        _state.value = _state.value.copy(images = _state.value.images + url.trim())
    }

    private fun removeImage(index: Int) {
        val images = _state.value.images.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(images = images)
        }
    }

    private fun addPolicy() {
        _state.value = _state.value.copy(policies = _state.value.policies + PolicyUi())
    }

    private fun updatePolicyTitle(index: Int, value: String) {
        val list = _state.value.policies.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(title = value)
            _state.value = _state.value.copy(policies = list)
        }
    }

    private fun updatePolicyContent(index: Int, value: String) {
        val list = _state.value.policies.toMutableList()
        if (index in list.indices) {
            list[index] = list[index].copy(content = value)
            _state.value = _state.value.copy(policies = list)
        }
    }

    private fun removePolicy(index: Int) {
        val list = _state.value.policies.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _state.value = _state.value.copy(policies = list)
        }
    }

    private fun toggleLanguage(value: String) {
        _state.value = _state.value.copy(
            selectedLanguages = _state.value.selectedLanguages.toggle(value)
        )
    }

    private fun toggleFacility(value: String) {
        _state.value = _state.value.copy(
            selectedFacilities = _state.value.selectedFacilities.toggle(value)
        )
    }

    private fun toggleFeature(value: String) {
        _state.value = _state.value.copy(
            selectedFeatures = _state.value.selectedFeatures.toggle(value)
        )
    }

    private fun saveHotel() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            prepareImagesForSave()
            val hotel = buildHotelFromState()
            sendEffect { AccommodationEditEffect.ShowSaveSuccess(hotel) }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun createHotel() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            prepareImagesForSave()
            val hotel = buildHotelFromState().copy(id = _state.value.hotelId.orEmpty())
            sendEffect { AccommodationEditEffect.ShowCreateSuccess(hotel) }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun openRooms() {
        viewModelScope.launch {
            sendEffect { AccommodationEditEffect.NavigateToRooms(_state.value.hotelId) }
        }
    }

    private suspend fun prepareImagesForSave(): List<String> {
        val existing = _state.value.images
        val locals = _state.value.localImageUris
        val hotelId = _state.value.hotelId.orEmpty()
        val name = _state.value.name
        Log.d(LOG_TAG, "prepareImagesForSave() start, existing=${existing.size}, locals=${locals.size}, hotelId=$hotelId, name='$name'")

        if (locals.isEmpty()) {
            Log.d(LOG_TAG, "No local images to upload, skip upload")
            return existing
        }

        // Nếu chưa có hotelId (trường hợp create mới hoàn toàn), tạm thời không upload
        if (hotelId.isBlank()) {
            Log.d(LOG_TAG, "Skip upload because hotelId is blank (create mode, chưa có ID)")
            return existing
        }

        return try {
            Log.d(LOG_TAG, "Calling UploadAccommodationImagesUseCase with ${locals.size} images")
            val uploadedUrls = uploadAccommodationImagesUseCase(
                hotelId = hotelId,
                accommodationName = name.ifBlank { hotelId },
                imageUris = locals
            )
            Log.d(LOG_TAG, "Upload success, received ${uploadedUrls.size} URLs: $uploadedUrls")

            val merged = existing + uploadedUrls

            _state.value = _state.value.copy(
                images = merged,
                localImageUris = emptyList()
            )
            merged
        } catch (e: CancellationException) {
            // Nếu upload bị cancel (ViewModel bị clear), chỉ log warning và return existing images
            // Không throw lại để tránh crash app
            Log.w(LOG_TAG, "Upload cancelled (ViewModel may have been cleared): ${e.message}")
            existing
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error while uploading images: ${e.message}", e)
            // Gửi error effect để UI có thể hiển thị message
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Cannot connect to image service. Please ensure the Spring Boot backend is running on port 8080."
                e.message?.contains("Connection refused", ignoreCase = true) == true -> 
                    "Connection refused. Is the Spring Boot backend running?"
                e.message?.contains("Cannot connect", ignoreCase = true) == true -> 
                    "Cannot connect to image service. Please start the backend server."
                else -> "Failed to upload images: ${e.message}"
            }
            sendEffect { AccommodationEditEffect.ShowError(errorMessage) }
            existing
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { AccommodationEditEffect.NavigateBack }
        }
    }

    private fun buildHotelFromState(): Hotel {
        val (lat, lng) = parseCoordinate(_state.value.coordinate)
        return Hotel(
            id = _state.value.hotelId.orEmpty(),
            name = _state.value.name,
            description = _state.value.description,
            propertyType = _state.value.propertyType,
            formattedAddress = _state.value.address,
            country = _state.value.country,
            city = _state.value.city,
            coordinate = Coordinate(lat, lng),
            imageUrl = _state.value.images,
            policy = _state.value.policies.map { Policy(title = it.title, content = it.content) },
            language = _state.value.selectedLanguages.toList(),
            feature = _state.value.selectedFeatures.toList(),
            minPrice = null,
            rating = BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP).toDouble(),
            numberOfReviews = 0
        )
    }

    private fun parseCoordinate(raw: String): Pair<Double, Double> {
        val parts = raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val lat = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val lng = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        return lat to lng
    }

    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) {
            this - value
        } else {
            this + value
        }
    }
}

