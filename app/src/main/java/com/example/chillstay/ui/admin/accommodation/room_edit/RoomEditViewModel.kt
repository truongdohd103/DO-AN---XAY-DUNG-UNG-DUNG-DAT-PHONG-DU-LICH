package com.example.chillstay.ui.admin.accommodation.room_edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomGallery
import com.example.chillstay.domain.usecase.room.GetRoomByIdUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.chillstay.domain.usecase.image.UploadRoomImagesUseCase
import com.example.chillstay.domain.usecase.room.CreateRoomUseCase
import com.example.chillstay.domain.usecase.room.UpdateRoomUseCase

class RoomEditViewModel(
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val uploadRoomImagesUseCase: UploadRoomImagesUseCase,
    private val updateRoomUseCase: UpdateRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase
) : BaseViewModel<RoomEditUiState, RoomEditIntent, RoomEditEffect>(
    RoomEditUiState()
) {
    companion object {
        private const val LOG_TAG = "ChillStayImageUpload"
    }

    val uiState = state

    override fun onEvent(event: RoomEditIntent) {
        when (event) {
            is RoomEditIntent.LoadForCreate -> resetForCreate(event.hotelId)
            is RoomEditIntent.LoadForEdit -> loadRoomById(event.roomId)

            is RoomEditIntent.UpdateRoomName -> _state.value = _state.value.copy(name = event.value)
            is RoomEditIntent.UpdateArea -> _state.value = _state.value.copy(area = event.value)
            is RoomEditIntent.UpdateDoubleBeds -> _state.value = _state.value.copy(doubleBed = event.value)
            is RoomEditIntent.UpdateSingleBeds -> _state.value = _state.value.copy(singleBed = event.value)
            is RoomEditIntent.UpdateMaxOccupancy -> _state.value = _state.value.copy(maxOccupancy = event.value)
            is RoomEditIntent.UpdatePricePerNight -> _state.value = _state.value.copy(pricePerNight = event.value)
            is RoomEditIntent.UpdateDiscount -> _state.value = _state.value.copy(discount = event.value)
            is RoomEditIntent.UpdateAvailableQuantity -> _state.value = _state.value.copy(availableQuantity = event.value)
            is RoomEditIntent.UpdateBreakfastPrice -> _state.value = _state.value.copy(breakfastPrice = event.value)

            is RoomEditIntent.RemoveImage -> removeImage(event.tag, event.index)
            is RoomEditIntent.SetLocalImages -> setLocalImage(event.tag, event.uris)
            is RoomEditIntent.RemoveLocalImage -> removeLocalImage(event.tag, event.index)

            is RoomEditIntent.ToggleFeature -> toggleFeature(event.feature)

            RoomEditIntent.Save -> saveRoom()
            RoomEditIntent.Create -> createRoom()
            RoomEditIntent.NavigateBack -> navigateBack()
            RoomEditIntent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun resetForCreate(hotelId: String) {
        _state.value = RoomEditUiState(hotelId = hotelId)
    }

    private fun loadRoomById(roomId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, mode = Mode.Edit, roomId = roomId)
            val result = getRoomByIdUseCase(roomId).first()
            when (result) {
                is Result.Success -> {
                    val room = result.data
                    _state.value = _state.value.copy(
                        isSaving = false,
                        mode = Mode.Edit,
                        roomId = room.id,
                        hotelId = room.hotelId,
                        name = room.name,
                        area = room.area.toString(),
                        doubleBed = room.doubleBed.toString(),
                        singleBed = room.singleBed.toString(),
                        discount = room.discount.toString(),
                        breakfastPrice = room.breakfastPrice.toString(),
                        maxOccupancy = room.capacity.toString(),
                        pricePerNight = room.price.toString(),
                        availableQuantity = room.quantity.toString(),
                        selectedFeatures = room.feature.toSet(),
                        exteriorView = room.gallery?.exteriorView ?: emptyList(),
                        dining = room.gallery?.dining ?: emptyList(),
                        thisRoom = room.gallery?.thisRoom ?: emptyList()
                    )

                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isSaving = false)
                    sendEffect {
                        RoomEditEffect.ShowError(
                            result.throwable.message ?: "Failed to load room"
                        )
                    }
                }
            }
        }
    }
    private fun removeImage(tag: String, index: Int) {
        when (tag) {
            RoomEditConstant.EXTERIOR_VIEW -> {
                val images = _state.value.exteriorView.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(exteriorView = images)
                }
            }
            RoomEditConstant.DINING -> {
                val images = _state.value.dining.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(dining = images)
                }
            }
            else -> {
                val images = _state.value.thisRoom.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(thisRoom = images)
                }
            }
        }
    }


    private fun setLocalImage(tag: String, uris: List<Uri>) {
        when (tag) {
            RoomEditConstant.EXTERIOR_VIEW -> _state.value = _state.value.copy(localExteriorUris = uris)
            RoomEditConstant.DINING -> _state.value = _state.value.copy(localDiningUris = uris)
            else -> _state.value = _state.value.copy(localRoomUris = uris)
        }
    }

    private fun removeLocalImage(tag: String, index: Int) {
        when (tag) {
            RoomEditConstant.EXTERIOR_VIEW -> {
                val uris = _state.value.localExteriorUris.toMutableList()
                if (index in uris.indices) {
                    uris.removeAt(index)
                    _state.value = _state.value.copy(localExteriorUris = uris)
                }
            }
            RoomEditConstant.DINING -> {
                val uris = _state.value.localDiningUris.toMutableList()
                if (index in uris.indices) {
                    uris.removeAt(index)
                    _state.value = _state.value.copy(localDiningUris = uris)
                }
            }
            else -> {
                val uris = _state.value.localRoomUris.toMutableList()
                if (index in uris.indices) {
                    uris.removeAt(index)
                    _state.value = _state.value.copy(localRoomUris = uris)
                }
            }
        }
    }

    private fun toggleFeature(feature: String) {
        _state.value = _state.value.copy(
            selectedFeatures = _state.value.selectedFeatures.toggle(feature)
        )
    }

    private fun saveRoom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val uploadedExteriorImages = prepareImagesForSave(
                    tag = RoomEditConstant.EXTERIOR_VIEW,
                    existing = _state.value.exteriorView,
                    locals = _state.value.localExteriorUris,
                    roomId = _state.value.roomId.orEmpty(),
                    hotelId = _state.value.hotelId.orEmpty(),
                    name = _state.value.name
                )
                val uploadedDiningImages = prepareImagesForSave(
                    tag = RoomEditConstant.DINING,
                    existing = _state.value.dining,
                    locals = _state.value.localDiningUris,
                    roomId = _state.value.roomId.orEmpty(),
                    hotelId = _state.value.hotelId.orEmpty(),
                    name = _state.value.name
                )
                val uploadedRoomImages = prepareImagesForSave(
                    tag = RoomEditConstant.THIS_ROOM,
                    existing = _state.value.thisRoom,
                    locals = _state.value.localRoomUris,
                    roomId = _state.value.roomId.orEmpty(),
                    hotelId = _state.value.hotelId.orEmpty(),
                    name = _state.value.name
                )
                val roomToUpdate = buildRoomFromState().copy(gallery = RoomGallery(
                    exteriorView = uploadedExteriorImages,
                    dining = uploadedDiningImages,
                    thisRoom = uploadedRoomImages
                ))
                updateRoomUseCase(roomToUpdate).first().fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            isSaving = false,
                            exteriorView = uploadedExteriorImages,
                            dining = uploadedDiningImages,
                            thisRoom = uploadedRoomImages)
                        sendEffect { RoomEditEffect.ShowSaveSuccess(roomToUpdate) }
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect { RoomEditEffect.ShowError(throwable.message ?: "Failed to save room") }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { RoomEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private suspend fun prepareImagesForSave(tag: String, existing: List<String>, locals: List<Uri>, roomId: String, hotelId: String, name: String): List<String> {
        Log.d(LOG_TAG, "prepareImagesForSave() start, existing=${existing.size}, locals=${locals.size}, name='$name'")

        if (locals.isEmpty()) {
            Log.d(LOG_TAG, "No local images to upload, skip upload")
            return existing
        }
        // Nếu chưa có hotelId (trường hợp create mới hoàn toàn), tạm thời không upload
        if (hotelId.isBlank()) {
            Log.d(LOG_TAG, "Skip upload because hotelId is blank")
            return existing
        }
        if (roomId.isBlank()) {
            Log.d(LOG_TAG, "Skip upload because roomId is blank")
            return existing
        }

        return try {
            Log.d(LOG_TAG, "Calling UploadAccommodationImagesUseCase with ${locals.size} images")
            val uploadedUrls = uploadRoomImagesUseCase(
                hotelId = hotelId,
                roomId = roomId,
                tag = tag,
                imageUris = locals
            )
            Log.d(LOG_TAG, "Upload success, received ${uploadedUrls.size} URLs: $uploadedUrls")

            val merged = existing + uploadedUrls
            when(tag) {
                RoomEditConstant.EXTERIOR_VIEW -> _state.value = _state.value.copy(
                    exteriorView = merged,
                    localExteriorUris = emptyList()
                )
                RoomEditConstant.DINING -> _state.value = _state.value.copy(
                    dining = merged,
                    localDiningUris = emptyList()
                )
                RoomEditConstant.THIS_ROOM -> _state.value = _state.value.copy(
                    thisRoom = merged,
                    localDiningUris = emptyList()
                )
            }

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
                    "Connection timed out."
                e.message?.contains("Connection refused", ignoreCase = true) == true ->
                    "Connection refused."
                e.message?.contains("Cannot connect", ignoreCase = true) == true ->
                    "Cannot connect to image service."
                else -> "Failed to upload images: ${e.message}"
            }
            sendEffect { RoomEditEffect.ShowError(errorMessage) }
            existing
        }
    }

    private fun createRoom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val minimalRoom = buildRoomFromState().copy(
                    id = "",
                    gallery = RoomGallery(
                        emptyList(),
                        emptyList(),
                        emptyList()
                    ))

                createRoomUseCase(minimalRoom).first().fold(
                    onSuccess = { newId ->
                        _state.value = _state.value.copy(hotelId = newId)
                        val uploadedExteriorImages = prepareImagesForSave(
                            tag = RoomEditConstant.EXTERIOR_VIEW,
                            existing = _state.value.exteriorView,
                            locals = _state.value.localExteriorUris,
                            roomId = _state.value.roomId.orEmpty(),
                            hotelId = _state.value.hotelId.orEmpty(),
                            name = _state.value.name
                        )
                        val uploadedDiningImages = prepareImagesForSave(
                            tag = RoomEditConstant.DINING,
                            existing = _state.value.dining,
                            locals = _state.value.localDiningUris,
                            roomId = _state.value.roomId.orEmpty(),
                            hotelId = _state.value.hotelId.orEmpty(),
                            name = _state.value.name
                        )
                        val uploadedRoomImages = prepareImagesForSave(
                            tag = RoomEditConstant.THIS_ROOM,
                            existing = _state.value.thisRoom,
                            locals = _state.value.localRoomUris,
                            roomId = _state.value.roomId.orEmpty(),
                            hotelId = _state.value.hotelId.orEmpty(),
                            name = _state.value.name
                        )
                        val finalRoom = buildRoomFromState().copy(gallery = RoomGallery(
                            exteriorView = uploadedExteriorImages,
                            dining = uploadedDiningImages,
                            thisRoom = uploadedRoomImages
                        ))

                        updateRoomUseCase(finalRoom).first().fold(
                            onSuccess = {
                                _state.value = _state.value.copy(
                                    isSaving = false,
                                    exteriorView = uploadedExteriorImages,
                                    dining = uploadedDiningImages,
                                    thisRoom = uploadedRoomImages)
                                sendEffect { RoomEditEffect.ShowCreateSuccess(finalRoom) }
                            },
                            onFailure = { throwable ->
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect { RoomEditEffect.ShowError(throwable.message ?: "Failed to finalize room creation") }
                            }
                        )
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect { RoomEditEffect.ShowError(throwable.message ?: "Failed to create room") }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { RoomEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { RoomEditEffect.NavigateBack }
        }
    }

    private fun buildRoomFromState(): Room {
        val area = _state.value.area.toDoubleOrNull() ?: 0.0
        val doubleBed = _state.value.doubleBed.toIntOrNull() ?: 0
        val singleBed = _state.value.doubleBed.toIntOrNull() ?: 0
        val price = _state.value.pricePerNight.toDoubleOrNull() ?: 0.0
        val discount = _state.value.discount.toDoubleOrNull() ?: 0.0
        val occupancy = _state.value.maxOccupancy.toIntOrNull() ?: 0
        val quantity = _state.value.availableQuantity.toIntOrNull() ?: 0
        val breakfastPrice = _state.value.breakfastPrice.toDoubleOrNull() ?: 0.0
        return Room(
            id = _state.value.roomId.orEmpty(),
            hotelId = _state.value.hotelId.orEmpty(),
            name = _state.value.name,
            area = area,
            doubleBed = doubleBed,
            singleBed = singleBed,
            quantity = quantity,
            feature = _state.value.selectedFeatures.toList(),
            breakfastPrice = breakfastPrice,
            price = price,
            discount = discount,
            capacity = occupancy,
            gallery =
                RoomGallery(
                    exteriorView = _state.value.exteriorView.toList(),
                    dining = _state.value.dining.toList(),
                    thisRoom = _state.value.thisRoom.toList()
                )
        )
    }


    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) this - value else this + value
    }
}

