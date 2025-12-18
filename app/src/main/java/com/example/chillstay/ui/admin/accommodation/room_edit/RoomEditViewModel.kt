package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomDetail
import com.example.chillstay.domain.model.RoomGallery
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RoomEditViewModel(
    private val getRoomByIdUseCase: GetRoomByIdUseCase
) : BaseViewModel<RoomEditUiState, RoomEditIntent, RoomEditEffect>(
    RoomEditUiState()
) {

    val uiState = state

    override fun onEvent(event: RoomEditIntent) {
        when (event) {
            is RoomEditIntent.LoadForCreate -> resetForCreate(event.hotelId)
            is RoomEditIntent.LoadForEdit -> loadRoomById(event.roomId)

            is RoomEditIntent.UpdateRoomName -> _state.value = _state.value.copy(roomName = event.value)
            is RoomEditIntent.UpdateArea -> _state.value = _state.value.copy(area = event.value)
            is RoomEditIntent.UpdateDoubleBeds -> _state.value = _state.value.copy(doubleBeds = event.value)
            is RoomEditIntent.UpdateSingleBeds -> _state.value = _state.value.copy(singleBeds = event.value)
            is RoomEditIntent.UpdateMaxOccupancy -> _state.value = _state.value.copy(maxOccupancy = event.value)
            is RoomEditIntent.UpdatePricePerNight -> _state.value = _state.value.copy(pricePerNight = event.value)
            is RoomEditIntent.UpdateDiscount -> _state.value = _state.value.copy(discount = event.value)
            is RoomEditIntent.UpdateAvailableQuantity -> _state.value = _state.value.copy(availableQuantity = event.value)
            is RoomEditIntent.UpdateBreakfastPrice -> _state.value = _state.value.copy(breakfastPrice = event.value)

            is RoomEditIntent.AddImage -> addImage(event.url)
            is RoomEditIntent.RemoveImage -> removeImage(event.index)

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
                        roomName = room.detail?.name ?: room.type,
                        area = room.detail?.size?.toString() ?: "",
                        maxOccupancy = room.capacity.toString(),
                        pricePerNight = room.price.toString(),
                        availableQuantity = room.availableCount.toString(),
                        imageUrls = if (room.imageUrl.isNotEmpty()) listOf(room.imageUrl) else emptyList(),
                        selectedFeatures = room.facilities.toSet()
                    )
                    // TODO: Parse beds from room data if available
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

    private fun addImage(url: String) {
        if (url.isBlank()) return
        _state.value = _state.value.copy(imageUrls = _state.value.imageUrls + url.trim())
    }

    private fun removeImage(index: Int) {
        val images = _state.value.imageUrls.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(imageUrls = images)
        }
    }

    private fun toggleFeature(feature: String) {
        _state.value = _state.value.copy(
            selectedFeatures = _state.value.selectedFeatures.toggle(feature)
        )
    }

    private fun saveRoom() {
        viewModelScope.launch {
            val room = buildRoomFromState()
            _state.value = _state.value.copy(isSaving = true)
            sendEffect { RoomEditEffect.ShowSaveSuccess(room) }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun createRoom() {
        viewModelScope.launch {
            val room = buildRoomFromState().copy(id = _state.value.roomId.orEmpty())
            _state.value = _state.value.copy(isSaving = true)
            sendEffect { RoomEditEffect.ShowCreateSuccess(room) }
            _state.value = _state.value.copy(isSaving = false)
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { RoomEditEffect.NavigateBack }
        }
    }

    private fun buildRoomFromState(): Room {
        val area = _state.value.area.toDoubleOrNull() ?: 0.0
        val maxOccupancy = _state.value.maxOccupancy.toIntOrNull() ?: 0
        val price = _state.value.pricePerNight.toDoubleOrNull() ?: 0.0
        val availableCount = _state.value.availableQuantity.toIntOrNull() ?: 0
        val imageUrl = _state.value.imageUrls.firstOrNull() ?: ""

        return Room(
            id = _state.value.roomId.orEmpty(),
            hotelId = _state.value.hotelId.orEmpty(),
            type = _state.value.roomName,
            price = price,
            imageUrl = imageUrl,
            detail = RoomDetail(
                name = _state.value.roomName,
                size = area,
                view = ""
            ),
            isAvailable = true,
            capacity = maxOccupancy,
            availableCount = availableCount,
            facilities = _state.value.selectedFeatures.toList(),
            gallery = if (_state.value.imageUrls.isNotEmpty()) {
                RoomGallery(
                    exteriorView = emptyList(),
                    facilities = emptyList(),
                    dining = emptyList(),
                    thisRoom = _state.value.imageUrls
                )
            } else null
        )
    }

    private fun Set<String>.toggle(value: String): Set<String> {
        return if (contains(value)) {
            this - value
        } else {
            this + value
        }
    }
}

