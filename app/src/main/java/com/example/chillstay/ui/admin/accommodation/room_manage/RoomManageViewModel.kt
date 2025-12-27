package com.example.chillstay.ui.admin.accommodation.room_manage

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.usecase.room.GetRoomsByHotelIdUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RoomManageViewModel(
    private val getHotelRoomsUseCase: GetRoomsByHotelIdUseCase
) : BaseViewModel<RoomManageUiState, RoomManageIntent, RoomManageEffect>(
    RoomManageUiState()
) {

    val uiState = state

    override fun onEvent(event: RoomManageIntent) {
        when (event) {
            is RoomManageIntent.LoadRooms -> loadRooms(event.hotelId)
            RoomManageIntent.NavigateBack -> navigateBack()
            RoomManageIntent.CreateNewRoom -> createNewRoom()
            is RoomManageIntent.EditRoom -> editRoom(event.room)
            is RoomManageIntent.DisableRoom -> disableRoom(event.room)
            is RoomManageIntent.DeleteRoom -> deleteRoom(event.room)
            RoomManageIntent.ClearError -> _state.value = _state.value.clearError()
        }
    }

    private fun loadRooms(hotelId: String) {
        viewModelScope.launch {
            _state.value = _state.value
                .updateHotelId(hotelId)
                .updateIsLoading(true)
                .clearError()

            getHotelRoomsUseCase(hotelId = hotelId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _state.value = _state.value
                            .updateRooms(result.data)
                            .updateIsLoading(false)
                            .clearError()
                    }
                    is Result.Error -> {
                        _state.value = _state.value
                            .updateIsLoading(false)
                            .updateError(result.throwable.message ?: "Failed to load rooms")
                        sendEffect {
                            RoomManageEffect.ShowError(
                                result.throwable.message ?: "Failed to load rooms"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { RoomManageEffect.NavigateBack }
        }
    }

    private fun createNewRoom() {
        viewModelScope.launch {
            sendEffect { RoomManageEffect.NavigateToCreateRoom }
        }
    }

    private fun editRoom(room: Room) {
        viewModelScope.launch {
            sendEffect { RoomManageEffect.NavigateToEditRoom(room) }
        }
    }

    private fun disableRoom(room: Room) {
        // TODO: Implement disable room logic (set isAvailable = false)
        viewModelScope.launch {
            sendEffect { RoomManageEffect.ShowDisableSuccess(room) }
            // Reload rooms after disable
            _state.value.hotelId?.let { loadRooms(it) }
        }
    }

    private fun deleteRoom(room: Room) {
        // TODO: Implement delete room logic
        viewModelScope.launch {
            sendEffect { RoomManageEffect.ShowDeleteSuccess(room) }
            // Reload rooms after delete
            _state.value.hotelId?.let { loadRooms(it) }
        }
    }
}

