package com.example.chillstay.ui.admin.accommodation.accommodation_edit

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Policy
import com.example.chillstay.domain.usecase.hotel.CreateHotelUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateHotelUseCase
import com.example.chillstay.domain.usecase.storage.UploadImageUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccommodationEditViewModel(
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val createHotelUseCase: CreateHotelUseCase,
    private val updateHotelUseCase: UpdateHotelUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccommodationEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AccommodationEditEffect>()
    val effect = _effect.receiveAsFlow()

    private var currentHotelId: String? = null

    fun onEvent(event: AccommodationEditIntent) {
        when (event) {
            AccommodationEditIntent.LoadForCreate -> {
                _uiState.update { AccommodationEditUiState(mode = Mode.Create) }
                currentHotelId = null
            }
            is AccommodationEditIntent.LoadForEdit -> loadHotel(event.hotelId)
            
            is AccommodationEditIntent.UpdateName -> _uiState.update { it.copy(name = event.name) }
            is AccommodationEditIntent.UpdateDescription -> _uiState.update { it.copy(description = event.description) }
            is AccommodationEditIntent.UpdateType -> _uiState.update { it.copy(propertyType = event.type) }
            
            is AccommodationEditIntent.UpdateFullAddress -> _uiState.update { it.copy(address = event.address) }
            is AccommodationEditIntent.UpdateCountry -> _uiState.update { it.copy(country = event.country) }
            is AccommodationEditIntent.UpdateCity -> _uiState.update { it.copy(city = event.city) }
            is AccommodationEditIntent.UpdateCoordinate -> _uiState.update { it.copy(coordinate = event.coordinate) }
            
            is AccommodationEditIntent.SetLocalImages -> _uiState.update { it.copy(localImageUris = it.localImageUris + event.uris) }
            is AccommodationEditIntent.RemoveLocalImage -> removeLocalImage(event.index)
            is AccommodationEditIntent.RemoveImage -> removeRemoteImage(event.index)
            
            AccommodationEditIntent.AddPolicy -> addPolicy()
            is AccommodationEditIntent.RemovePolicy -> removePolicy(event.index)
            is AccommodationEditIntent.UpdatePolicyTitle -> updatePolicyTitle(event.index, event.title)
            is AccommodationEditIntent.UpdatePolicyContent -> updatePolicyContent(event.index, event.content)
            
            is AccommodationEditIntent.ToggleLanguage -> toggleLanguage(event.language)
            is AccommodationEditIntent.ToggleFacility -> toggleFacility(event.facility)
            is AccommodationEditIntent.ToggleFeature -> toggleFeature(event.feature)
            
            AccommodationEditIntent.Save -> saveHotel()
            AccommodationEditIntent.Create -> createHotel()
            
            AccommodationEditIntent.OpenRooms -> {
                currentHotelId?.let { id ->
                    sendEffect(AccommodationEditEffect.NavigateToRooms(id))
                }
            }
            AccommodationEditIntent.NavigateBack -> sendEffect(AccommodationEditEffect.NavigateBack)
        }
    }

    private fun loadHotel(hotelId: String) {
        currentHotelId = hotelId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mode = Mode.Edit) }
            getHotelByIdUseCase(hotelId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val hotel = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = hotel.name,
                                description = hotel.description,
                                propertyType = hotel.propertyType,
                                address = hotel.formattedAddress,
                                country = hotel.country,
                                city = hotel.city,
                                coordinate = "${hotel.coordinate.latitude},${hotel.coordinate.longitude}",
                                images = hotel.imageUrl,
                                policies = hotel.policy.map { p -> PolicyUi(p.title, p.content) },
                                selectedLanguages = hotel.language.toSet(),
                                selectedFacilities = hotel.feature.toSet(),
                                selectedFeatures = emptySet()
                            )
                        }
                    }
                    is Result.Error -> {
                        val error = result.throwable
                        _uiState.update { it.copy(isLoading = false, error = error.message) }
                        sendEffect(AccommodationEditEffect.ShowError(error.message ?: "Failed to load hotel"))
                    }
                }
            }
        }
    }

    private fun removeLocalImage(index: Int) {
        _uiState.update {
            val list = it.localImageUris.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(localImageUris = list)
        }
    }

    private fun removeRemoteImage(index: Int) {
        _uiState.update {
            val list = it.images.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(images = list)
        }
    }

    private fun addPolicy() {
        _uiState.update {
            it.copy(policies = it.policies + PolicyUi("New Policy", ""))
        }
    }

    private fun removePolicy(index: Int) {
        _uiState.update {
            val list = it.policies.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(policies = list)
        }
    }

    private fun updatePolicyTitle(index: Int, title: String) {
        _uiState.update {
            val list = it.policies.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(title = title)
            }
            it.copy(policies = list)
        }
    }

    private fun updatePolicyContent(index: Int, content: String) {
        _uiState.update {
            val list = it.policies.toMutableList()
            if (index in list.indices) {
                list[index] = list[index].copy(content = content)
            }
            it.copy(policies = list)
        }
    }

    private fun toggleLanguage(language: String) {
        _uiState.update {
            val list = it.selectedLanguages.toMutableList()
            if (list.contains(language)) list.remove(language) else list.add(language)
            it.copy(selectedLanguages = list.toSet())
        }
    }

    private fun toggleFacility(facility: String) {
        _uiState.update {
            val list = it.selectedFacilities.toMutableList()
            if (list.contains(facility)) list.remove(facility) else list.add(facility)
            it.copy(selectedFacilities = list.toSet())
        }
    }

    private fun toggleFeature(feature: String) {
        _uiState.update {
            val list = it.selectedFeatures.toMutableList()
            if (list.contains(feature)) list.remove(feature) else list.add(feature)
            it.copy(selectedFeatures = list.toSet())
        }
    }

    private fun saveHotel() {
        val state = uiState.value
        val id = currentHotelId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            // Upload images first
            val uploadedUrls = uploadImages(state.localImageUris)
            val allImages = state.images + uploadedUrls
            
            val hotel = buildHotelObject(id, state, allImages)
            
            updateHotelUseCase(hotel).collect { result ->
                _uiState.update { it.copy(isSaving = false) }
                when (result) {
                    is Result.Success<*> -> {
                        sendEffect(AccommodationEditEffect.ShowSaveSuccess(hotel))
                    }
                    is Result.Error -> {
                        val e = result.throwable
                        sendEffect(AccommodationEditEffect.ShowError(e.message ?: "Failed to save"))
                    }
                }
            }
        }
    }

    private fun createHotel() {
        val state = uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            // Upload images first
            val uploadedUrls = uploadImages(state.localImageUris)
            val allImages = state.images + uploadedUrls // Should be empty + uploaded
            
            val hotel = buildHotelObject("", state, allImages) // ID generated by repo/firestore
            
            createHotelUseCase(hotel).collect { result ->
                _uiState.update { it.copy(isSaving = false) }
                when (result) {
                    is Result.Success<String> -> {
                        val newId = result.data
                        val createdHotel = hotel.copy(id = newId)
                        currentHotelId = newId
                        sendEffect(AccommodationEditEffect.ShowCreateSuccess(createdHotel))
                    }
                    is Result.Error -> {
                        val e = result.throwable
                        sendEffect(AccommodationEditEffect.ShowError(e.message ?: "Failed to create"))
                    }
                }
            }
        }
    }

    private suspend fun uploadImages(uris: List<Uri>): List<String> = coroutineScope {
        uris.map { uri ->
            async {
                var resultUrl: String? = null
                uploadImageUseCase(uri, "hotel_images").collect { result ->
                    if (result is Result.Success<*>) {
                        resultUrl = (result as Result.Success<String>).data
                    }
                }
                resultUrl
            }
        }.awaitAll().filterNotNull()
    }

    private fun buildHotelObject(id: String, state: AccommodationEditUiState, imageUrls: List<String>): Hotel {
        val (lat, lng) = try {
            val parts = state.coordinate.split(",")
            parts[0].trim().toDouble() to parts[1].trim().toDouble()
        } catch (e: Exception) {
            0.0 to 0.0
        }

        return Hotel(
            id = id,
            name = state.name,
            description = state.description,
            propertyType = state.propertyType,
            formattedAddress = state.address,
            country = state.country,
            city = state.city,
            coordinate = com.example.chillstay.domain.model.Coordinate(lat, lng),
            imageUrl = imageUrls,
            policy = state.policies.map { Policy(it.title, it.content) },
            language = state.selectedLanguages.toList(),
            feature = state.selectedFacilities.toList(),
            // Add other fields as necessary, defaulting if missing in UI
            rating = 0.0,
            numberOfReviews = 0,
            minPrice = 0.0,
            rooms = emptyList()
        )
    }

    private fun sendEffect(effect: AccommodationEditEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
