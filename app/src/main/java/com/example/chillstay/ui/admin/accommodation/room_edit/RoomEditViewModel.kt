package com.example.chillstay.ui.admin.accommodation.room_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomDetail
import com.example.chillstay.domain.usecase.hotel.CreateRoomUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateRoomUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomEditViewModel(
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val updateRoomUseCase: UpdateRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomEditUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<RoomEditEffect>()
    val effect = _effect.receiveAsFlow()

    private var currentRoomId: String? = null
    private var currentHotelId: String? = null

    fun onEvent(event: RoomEditIntent) {
        when (event) {
            is RoomEditIntent.LoadForCreate -> {
                currentHotelId = event.hotelId
                currentRoomId = null
                _uiState.update { RoomEditUiState(mode = Mode.Create) }
            }
            is RoomEditIntent.LoadForEdit -> loadRoom(event.roomId)
            
            is RoomEditIntent.UpdateRoomName -> _uiState.update { it.copy(roomName = event.name) }
            is RoomEditIntent.UpdateArea -> _uiState.update { it.copy(area = event.area) }
            is RoomEditIntent.UpdateDoubleBeds -> _uiState.update { it.copy(doubleBeds = event.count) }
            is RoomEditIntent.UpdateSingleBeds -> _uiState.update { it.copy(singleBeds = event.count) }
            is RoomEditIntent.UpdateMaxOccupancy -> _uiState.update { it.copy(maxOccupancy = event.count) }
            
            is RoomEditIntent.UpdatePricePerNight -> _uiState.update { it.copy(pricePerNight = event.price) }
            is RoomEditIntent.UpdateDiscount -> _uiState.update { it.copy(discount = event.discount) }
            is RoomEditIntent.UpdateAvailableQuantity -> _uiState.update { it.copy(availableQuantity = event.quantity) }
            
            is RoomEditIntent.RemoveImage -> removeImage(event.index)
            is RoomEditIntent.AddImage -> addImage(event.url)
            
            is RoomEditIntent.ToggleFeature -> toggleFeature(event.feature)
            is RoomEditIntent.UpdateBreakfastPrice -> _uiState.update { it.copy(breakfastPrice = event.price) }
            
            RoomEditIntent.Save -> saveRoom()
            RoomEditIntent.Create -> createRoom()
            RoomEditIntent.NavigateBack -> sendEffect(RoomEditEffect.NavigateBack)
        }
    }

    private fun loadRoom(roomId: String) {
        currentRoomId = roomId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mode = Mode.Edit) }
            getRoomByIdUseCase(roomId).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val room = result.data
                        currentHotelId = room.hotelId
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                roomName = room.detail?.name ?: "",
                                area = room.detail?.size?.toString() ?: "",
                                doubleBeds = "1", // TODO: Parse from detail if needed or add field to Room model
                                singleBeds = "0", // Placeholder
                                maxOccupancy = room.capacity.toString(),
                                pricePerNight = room.price.toString(),
                                discount = "0", // Placeholder
                                availableQuantity = room.availableCount.toString(),
                                imageUrls = if (room.imageUrl.isNotEmpty()) listOf(room.imageUrl) else emptyList(), // Convert single to list or fix model
                                selectedFeatures = room.facilities,
                                // breakfastPrice not in model yet
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.throwable.message ?: "Room not found") }
                        sendEffect(RoomEditEffect.ShowError("Room not found"))
                    }
                }
            }
        }
    }

    private fun removeImage(index: Int) {
        _uiState.update {
            val list = it.imageUrls.toMutableList()
            if (index in list.indices) list.removeAt(index)
            it.copy(imageUrls = list)
        }
    }

    private fun addImage(url: String) {
        _uiState.update { it.copy(imageUrls = it.imageUrls + url) }
    }

    private fun toggleFeature(feature: String) {
        _uiState.update {
            val list = it.selectedFeatures.toMutableList()
            if (list.contains(feature)) list.remove(feature) else list.add(feature)
            it.copy(selectedFeatures = list)
        }
    }

    private fun saveRoom() {
        val state = uiState.value
        val id = currentRoomId ?: return
        val hotelId = currentHotelId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val room = buildRoomObject(id, hotelId, state)
            
            updateRoomUseCase(room).collect { result ->
                _uiState.update { it.copy(isSaving = false) }
                when (result) {
                    is com.example.chillstay.core.common.Result.Success<*> -> {
                        sendEffect(RoomEditEffect.ShowSaveSuccess(room))
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        sendEffect(RoomEditEffect.ShowError(result.throwable.message ?: "Unknown error"))
                    }
                }
            }
        }
    }

    private fun createRoom() {
        val state = uiState.value
        val hotelId = currentHotelId ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            val room = buildRoomObject("", hotelId, state) // ID generated by repo
            
            createRoomUseCase(room).collect { result ->
                _uiState.update { it.copy(isSaving = false) }
                if (result is Result.Success<String>) {
                    val newId = result.data
                    val createdRoom = room.copy(id = newId)
                    currentRoomId = newId
                    sendEffect(RoomEditEffect.ShowCreateSuccess(createdRoom))
                } else if (result is Result.Error) {
                    sendEffect(RoomEditEffect.ShowError(result.throwable.message ?: "Failed to create"))
                }
            }
        }
    }

    private fun buildRoomObject(id: String, hotelId: String, state: RoomEditUiState): Room {
        val area = state.area.toDoubleOrNull() ?: 0.0
        val price = state.pricePerNight.toDoubleOrNull() ?: 0.0
        val capacity = state.maxOccupancy.toIntOrNull() ?: 2
        val availableCount = state.availableQuantity.toIntOrNull() ?: 1
        
        // Construct RoomDetail
        val detail = RoomDetail(
            name = state.roomName,
            size = area,
            view = "City View" // Default or add to UI
        )
        
        return Room(
            id = id,
            hotelId = hotelId,
            type = state.roomName,
            price = price,
            isAvailable = availableCount > 0,
            imageUrl = state.imageUrls.firstOrNull() ?: "", // TODO: Handle multiple images if model supports
            facilities = state.selectedFeatures,
            capacity = capacity,
            availableCount = availableCount,
            detail = detail
        )
    }

    private fun sendEffect(effect: RoomEditEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
