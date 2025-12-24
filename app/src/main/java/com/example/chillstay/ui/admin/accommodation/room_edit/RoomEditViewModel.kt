package com.example.chillstay.ui.admin.accommodation.room_edit

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomGallery
import com.example.chillstay.domain.repository.ImageUploadRepository
import com.example.chillstay.domain.usecase.room.GetRoomByIdUseCase
import com.example.chillstay.domain.usecase.image.UploadRoomImagesUseCase
import com.example.chillstay.domain.usecase.room.CreateRoomUseCase
import com.example.chillstay.domain.usecase.room.UpdateRoomUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RoomEditViewModel(
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val uploadRoomImagesUseCase: UploadRoomImagesUseCase,
    private val updateRoomUseCase: UpdateRoomUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val imageUploadRepository: ImageUploadRepository
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

            is RoomEditIntent.AddImages -> addImages(event.tag, event.uris)
            is RoomEditIntent.RemoveImage -> removeImage(event.tag, event.index)

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
            _state.value = _state.value.copy(
                isSaving = true,
                isLoadingImages = true,
                mode = Mode.Edit,
                roomId = roomId
            )

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
                        selectedFeatures = room.feature.toSet()
                    )

                    // Download tất cả ảnh cũ về local
                    downloadExistingImages(
                        exteriorUrls = room.gallery?.exteriorView ?: emptyList(),
                        diningUrls = room.gallery?.dining ?: emptyList(),
                        roomUrls = room.gallery?.thisRoom ?: emptyList()
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        isLoadingImages = false
                    )
                    sendEffect {
                        RoomEditEffect.ShowError(
                            result.throwable.message ?: "Failed to load room"
                        )
                    }
                }
            }
        }
    }

    /**
     * Download tất cả ảnh cũ từ URL về local storage
     */
    private fun downloadExistingImages(
        exteriorUrls: List<String>,
        diningUrls: List<String>,
        roomUrls: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d(LOG_TAG, "Downloading images: exterior=${exteriorUrls.size}, dining=${diningUrls.size}, room=${roomUrls.size}")

                // Download parallel
                val exteriorUris = exteriorUrls.mapIndexed { index, url ->
                    async {
                        Log.d(LOG_TAG, "Downloading exterior image $index: $url")
                        imageUploadRepository.downloadImageToLocal(url)
                    }
                }.awaitAll().filterNotNull()

                val diningUris = diningUrls.mapIndexed { index, url ->
                    async {
                        Log.d(LOG_TAG, "Downloading dining image $index: $url")
                        imageUploadRepository.downloadImageToLocal(url)
                    }
                }.awaitAll().filterNotNull()

                val roomUris = roomUrls.mapIndexed { index, url ->
                    async {
                        Log.d(LOG_TAG, "Downloading room image $index: $url")
                        imageUploadRepository.downloadImageToLocal(url)
                    }
                }.awaitAll().filterNotNull()

                Log.d(LOG_TAG, "Download completed: exterior=${exteriorUris.size}, dining=${diningUris.size}, room=${roomUris.size}")

                _state.value = _state.value.copy(
                    allExteriorUris = exteriorUris,
                    allDiningUris = diningUris,
                    allRoomUris = roomUris,
                    isLoadingImages = false
                )

            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error downloading images: ${e.message}", e)
                _state.value = _state.value.copy(isLoadingImages = false)
                sendEffect {
                    RoomEditEffect.ShowError("Failed to load some images: ${e.message}")
                }
            }
        }
    }

    private fun addImages(tag: String, newUris: List<Uri>) {
        when (tag) {
            RoomEditConstant.EXTERIOR_VIEW -> {
                val current = _state.value.allExteriorUris
                _state.value = _state.value.copy(allExteriorUris = current + newUris)
            }
            RoomEditConstant.DINING -> {
                val current = _state.value.allDiningUris
                _state.value = _state.value.copy(allDiningUris = current + newUris)
            }
            RoomEditConstant.THIS_ROOM -> {
                val current = _state.value.allRoomUris
                _state.value = _state.value.copy(allRoomUris = current + newUris)
            }
        }
    }

    private fun removeImage(tag: String, index: Int) {
        when (tag) {
            RoomEditConstant.EXTERIOR_VIEW -> {
                val images = _state.value.allExteriorUris.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(allExteriorUris = images)
                }
            }
            RoomEditConstant.DINING -> {
                val images = _state.value.allDiningUris.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(allDiningUris = images)
                }
            }
            RoomEditConstant.THIS_ROOM -> {
                val images = _state.value.allRoomUris.toMutableList()
                if (index in images.indices) {
                    images.removeAt(index)
                    _state.value = _state.value.copy(allRoomUris = images)
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
                val roomId = _state.value.roomId
                val hotelId = _state.value.hotelId

                if (roomId.isNullOrBlank() || hotelId.isNullOrBlank()) {
                    throw IllegalStateException("Cannot update room without ID")
                }

                // Upload TẤT CẢ ảnh (folder cũ sẽ được xóa trong repository)
                val (exteriorUrls, diningUrls, roomUrls) = uploadAllRoomImages(hotelId, roomId)

                val roomToUpdate = buildRoomFromState().copy(
                    gallery = RoomGallery(
                        exteriorView = exteriorUrls,
                        dining = diningUrls,
                        thisRoom = roomUrls
                    )
                )

                updateRoomUseCase(roomToUpdate).first().fold(
                    onSuccess = {
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect { RoomEditEffect.ShowSaveSuccess(roomToUpdate) }
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            RoomEditEffect.ShowError(
                                throwable.message ?: "Failed to save room"
                            )
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                Log.e(LOG_TAG, "Validation error: ${e.message}", e)
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { RoomEditEffect.ShowError(e.message ?: "Invalid state") }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Unexpected error in saveRoom: ${e.message}", e)
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { RoomEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private fun createRoom() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)

            try {
                val minimalRoom = buildRoomFromState().copy(
                    id = "",
                    gallery = RoomGallery(emptyList(), emptyList(), emptyList())
                )

                createRoomUseCase(minimalRoom).first().fold(
                    onSuccess = { newId ->
                        _state.value = _state.value.copy(roomId = newId)

                        val hotelId = _state.value.hotelId.orEmpty()

                        // Upload tất cả ảnh
                        val (exteriorUrls, diningUrls, roomUrls) = uploadAllRoomImages(hotelId, newId)

                        val finalRoom = buildRoomFromState().copy(
                            id = newId,
                            gallery = RoomGallery(
                                exteriorView = exteriorUrls,
                                dining = diningUrls,
                                thisRoom = roomUrls
                            )
                        )

                        updateRoomUseCase(finalRoom).first().fold(
                            onSuccess = {
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect { RoomEditEffect.ShowCreateSuccess(finalRoom) }
                            },
                            onFailure = { throwable ->
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect {
                                    RoomEditEffect.ShowError(
                                        throwable.message ?: "Failed to finalize room creation"
                                    )
                                }
                            }
                        )
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            RoomEditEffect.ShowError(
                                throwable.message ?: "Failed to create room"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { RoomEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    /**
     * Upload tất cả ảnh của room (repository sẽ tự động xóa folder cũ)
     * @return Triple(exteriorUrls, diningUrls, roomUrls)
     */
    private suspend fun uploadAllRoomImages(
        hotelId: String,
        roomId: String
    ): Triple<List<String>, List<String>, List<String>> = kotlinx.coroutines.coroutineScope {
        Log.d(LOG_TAG, "Uploading all room images for: $hotelId/$roomId")

        try {
            val exteriorDeferred = async {
                if (_state.value.allExteriorUris.isNotEmpty()) {
                    uploadRoomImagesUseCase(
                        hotelId = hotelId,
                        roomId = roomId,
                        tag = RoomEditConstant.EXTERIOR_VIEW,
                        imageUris = _state.value.allExteriorUris
                    )
                } else emptyList()
            }

            val diningDeferred = async {
                if (_state.value.allDiningUris.isNotEmpty()) {
                    uploadRoomImagesUseCase(
                        hotelId = hotelId,
                        roomId = roomId,
                        tag = RoomEditConstant.DINING,
                        imageUris = _state.value.allDiningUris
                    )
                } else emptyList()
            }

            val roomDeferred = async {
                if (_state.value.allRoomUris.isNotEmpty()) {
                    uploadRoomImagesUseCase(
                        hotelId = hotelId,
                        roomId = roomId,
                        tag = RoomEditConstant.THIS_ROOM,
                        imageUris = _state.value.allRoomUris
                    )
                } else emptyList()
            }

            val exteriorUrls = exteriorDeferred.await()
            val diningUrls = diningDeferred.await()
            val roomUrls = roomDeferred.await()

            Log.d(
                LOG_TAG,
                "Upload completed: exterior=${exteriorUrls.size}, dining=${diningUrls.size}, room=${roomUrls.size}"
            )

            Triple(exteriorUrls, diningUrls, roomUrls)
        } catch (e: CancellationException) {
            Log.w(LOG_TAG, "Upload cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error uploading room images: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> "Connection timed out."
                e.message?.contains("Connection refused", ignoreCase = true) == true -> "Connection refused."
                else -> "Failed to upload images: ${e.message}"
            }
            sendEffect { RoomEditEffect.ShowError(errorMessage) }
            Triple(emptyList(), emptyList(), emptyList())
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
        val singleBed = _state.value.singleBed.toIntOrNull() ?: 0
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
            gallery = null // Sẽ được set sau khi upload
        )
    }

    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) this - value else this + value
    }
}