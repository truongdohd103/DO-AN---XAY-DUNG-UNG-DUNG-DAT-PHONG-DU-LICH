package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomGallery
import com.example.chillstay.domain.model.RoomStatus
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

            is RoomEditIntent.UpdateRoomName -> _state.value = _state.value.copy(name = event.value)
            is RoomEditIntent.UpdateArea -> _state.value = _state.value.copy(area = event.value)
            is RoomEditIntent.UpdateDoubleBeds -> _state.value = _state.value.copy(doubleBed = event.value)
            is RoomEditIntent.UpdateSingleBeds -> _state.value = _state.value.copy(singleBed = event.value)
            is RoomEditIntent.UpdateMaxOccupancy -> _state.value = _state.value.copy(maxOccupancy = event.value)
            is RoomEditIntent.UpdatePricePerNight -> _state.value = _state.value.copy(pricePerNight = event.value)
            is RoomEditIntent.UpdateDiscount -> _state.value = _state.value.copy(discount = event.value)
            is RoomEditIntent.UpdateAvailableQuantity -> _state.value = _state.value.copy(availableQuantity = event.value)
            is RoomEditIntent.UpdateBreakfastPrice -> _state.value = _state.value.copy(breakfastPrice = event.value)

            is RoomEditIntent.AddImageExteriorView -> addImageExteriorView(event.url)
            is RoomEditIntent.RemoveImageExteriorView -> removeImageExteriorView(event.index)
            is RoomEditIntent.AddImageDining -> addImageDining(event.url)
            is RoomEditIntent.RemoveImageDining -> removeImageDining(event.index)
            is RoomEditIntent.AddImageThisRoom -> addImageThisRoom(event.url)
            is RoomEditIntent.RemoveImageThisRoom -> removeImageThisRoom(event.index)

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
                        area = room.toString(),
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

    private fun addImageExteriorView(url: String) {
        if (url.isBlank()) return
        _state.value = _state.value.copy(exteriorView = _state.value.exteriorView + url.trim())
    }

    private fun removeImageExteriorView(index: Int) {
        val images = _state.value.exteriorView.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(exteriorView = images)
        }
    }
    private fun addImageDining(url: String) {
        if (url.isBlank()) return
        _state.value = _state.value.copy(dining = _state.value.dining + url.trim())
    }

    private fun removeImageDining(index: Int) {
        val images = _state.value.dining.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(dining = images)
        }
    }
    private fun addImageThisRoom(url: String) {
        if (url.isBlank()) return
        _state.value = _state.value.copy(thisRoom = _state.value.thisRoom + url.trim())
    }

    private fun removeImageThisRoom(index: Int) {
        val images = _state.value.thisRoom.toMutableList()
        if (index in images.indices) {
            images.removeAt(index)
            _state.value = _state.value.copy(thisRoom = images)
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

