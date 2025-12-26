package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Coordinate
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.usecase.hotel.CreateHotelUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateHotelUseCase
import com.example.chillstay.domain.usecase.image.UploadAccommodationImagesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class AccommodationEditViewModel(
    private val createHotelUseCase: CreateHotelUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val updateHotelUseCase: UpdateHotelUseCase,
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

            is AccommodationEditIntent.RemoveImage -> removeImage(event.index)
            is AccommodationEditIntent.AddImages -> addImages(event.uris)

            AccommodationEditIntent.AddPolicy -> addPolicy()
            is AccommodationEditIntent.UpdatePolicyTitle -> updatePolicyTitle(event.index, event.value)
            is AccommodationEditIntent.UpdatePolicyContent -> updatePolicyContent(event.index, event.value)
            is AccommodationEditIntent.RemovePolicy -> removePolicy(event.index)

            is AccommodationEditIntent.ToggleLanguage -> toggleLanguage(event.value)
            is AccommodationEditIntent.ToggleFacility -> toggleFeatureAndFacility(event.value)
            is AccommodationEditIntent.ToggleFeature -> toggleFeatureAndFacility(event.value)

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
            _state.value = _state.value.copy(
                isSaving = true,
                isLoadingImages = true,
                mode = Mode.Edit,
                hotelId = hotelId
            )

            val result = getHotelByIdUseCase(hotelId).first()
            when (result) {
                is Result.Success -> {
                    val hotel = result.data
                    _state.value = _state.value.copy(
                        isSaving = false,
                        mode = Mode.Edit,
                        hotelId = hotel.id,
                        allImageUris = hotel.imageUrl.map { it -> it.toUri() },
                        name = hotel.name,
                        propertyType = hotel.propertyType,
                        description = hotel.description,
                        address = hotel.formattedAddress,
                        country = hotel.country,
                        city = hotel.city,
                        coordinate = "${hotel.coordinate.latitude},${hotel.coordinate.longitude}",
                        policies = hotel.policy.map { PolicyUi(title = it.title, content = it.content) },
                        selectedLanguages = hotel.language.toSet(),
                        selectedFeatures = hotel.feature.toSet()
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        isLoadingImages = false
                    )
                    sendEffect {
                        AccommodationEditEffect.ShowError(
                            result.throwable.message ?: "Failed to load hotel"
                        )
                    }
                }
            }
        }
    }

    private fun removeImage(index: Int) {
        val images = _state.value.allImageUris.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(allImageUris = images)
        }
    }

    private fun addImages(newUris: List<Uri>) {
        val current = _state.value.allImageUris
        _state.value = _state.value.copy(allImageUris = current + newUris)
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

    private fun toggleFeatureAndFacility(value: String) {
        _state.value = _state.value.copy(
            selectedFeatures = _state.value.selectedFeatures.toggle(value)
        )
    }

    private fun saveHotel() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            try {
                val hotelId = _state.value.hotelId
                if (hotelId.isNullOrBlank()) {
                    throw IllegalStateException("Cannot update hotel without ID. Use Create mode instead.")
                }

                // Upload TẤT CẢ ảnh (folder cũ sẽ được xóa trong repository)
                val uploadedImages = uploadAllImages(hotelId)

                val hotelToUpdate = buildHotelFromState().copy(imageUrl = uploadedImages)
                Log.d(LOG_TAG, "Hotel details: city=${hotelToUpdate.city}, country=${hotelToUpdate.country}")

                updateHotelUseCase(hotelToUpdate).first().fold(
                    onSuccess = {
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect { AccommodationEditEffect.ShowSaveSuccess(hotelToUpdate) }
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            AccommodationEditEffect.ShowError(
                                throwable.message ?: "Failed to save hotel"
                            )
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Validation error: ${e.message}", e)
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { AccommodationEditEffect.ShowError(e.message ?: "Invalid state") }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Unexpected error in saveHotel: ${e.message}", e)
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { AccommodationEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private fun createHotel() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val minimalHotel = buildHotelFromState().copy(id = "", imageUrl = emptyList())

                createHotelUseCase(minimalHotel).first().fold(
                    onSuccess = { newId ->
                        _state.value = _state.value.copy(hotelId = newId)

                        // Upload tất cả ảnh
                        val uploadedImages = uploadAllImages(newId)
                        val finalHotel = buildHotelFromState().copy(
                            id = newId,
                            imageUrl = uploadedImages
                        )
                        updateHotelUseCase(finalHotel).first().fold(
                            onSuccess = {
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect { AccommodationEditEffect.ShowCreateSuccess(finalHotel) }
                            },
                            onFailure = { throwable ->
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect {
                                    AccommodationEditEffect.ShowError(throwable.message ?: "Failed to finalize hotel creation")
                                }
                            }
                        )
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            AccommodationEditEffect.ShowError(
                                throwable.message ?: "Failed to create hotel"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { AccommodationEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private fun openRooms() {
        viewModelScope.launch {
            sendEffect { AccommodationEditEffect.NavigateToRooms(_state.value.hotelId) }
        }
    }

    /**
     * Upload tất cả ảnh hiện có (repository sẽ tự động xóa folder cũ)
     */
    private suspend fun uploadAllImages(hotelId: String): List<String> {
        val allUris = _state.value.allImageUris

        if (allUris.isEmpty()) {
            Log.d(LOG_TAG, "No images to upload")
            return emptyList()
        }

        Log.d(LOG_TAG, "Uploading ${allUris.size} images for hotel: $hotelId")

        return try {
            val uploadedUrls = uploadAccommodationImagesUseCase(hotelId = hotelId, imageUris = allUris)

            Log.d(LOG_TAG, "Upload completed: ${uploadedUrls.size} images uploaded successfully")
            uploadedUrls

        } catch (e: CancellationException) {
            Log.w(LOG_TAG, "Upload cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error uploading images: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Cannot connect to image service. Please check your connection."
                e.message?.contains("Connection refused", ignoreCase = true) == true ->
                    "Connection refused. Please check the backend service."
                else -> "Failed to upload images: ${e.message}"
            }
            sendEffect { AccommodationEditEffect.ShowError(errorMessage) }
            emptyList()
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
            imageUrl = emptyList(), // Sẽ được set sau khi upload
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
        return if (contains(value)) this - value else this + value
    }
}