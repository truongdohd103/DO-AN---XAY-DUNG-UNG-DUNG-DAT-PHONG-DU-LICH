package com.example.chillstay.ui.admin.accommodation.room_manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import com.example.chillstay.domain.usecase.hotel.DeleteRoomUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateRoomUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomManageViewModel(
    private val getHotelRoomsUseCase: GetHotelRoomsUseCase,
    private val updateRoomUseCase: UpdateRoomUseCase,
    private val deleteRoomUseCase: DeleteRoomUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomManageUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<RoomManageEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: RoomManageIntent) {
        when (event) {
            is RoomManageIntent.LoadRooms -> loadRooms(event.hotelId)
            RoomManageIntent.CreateNewRoom -> sendEffect(RoomManageEffect.NavigateToCreateRoom)
            is RoomManageIntent.EditRoom -> sendEffect(RoomManageEffect.NavigateToEditRoom(event.room))
            is RoomManageIntent.DeleteRoom -> deleteRoom(event.room)
            is RoomManageIntent.DisableRoom -> disableRoom(event.room)
            RoomManageIntent.NavigateBack -> sendEffect(RoomManageEffect.NavigateBack)
        }
    }

    private fun deleteRoom(room: Room) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = deleteRoomUseCase(room.id)
            if (result is Result.Success<*>) {
                val currentRooms = _uiState.value.rooms.toMutableList()
                currentRooms.removeIf { it.id == room.id }
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        rooms = currentRooms,
                        totalRooms = currentRooms.size,
                        activeRooms = currentRooms.count { r -> r.isAvailable }
                    ) 
                }
                sendEffect(RoomManageEffect.ShowDeleteSuccess(room))
            } else if (result is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.throwable.message) }
            }
        }
    }

    private fun disableRoom(room: Room) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updatedRoom = room.copy(isAvailable = !room.isAvailable)
            val result = updateRoomUseCase(updatedRoom)
            if (result is Result.Success<*>) {
                val currentRooms = _uiState.value.rooms.map { 
                    if (it.id == room.id) updatedRoom else it 
                }
                _uiState.update { 
                     it.copy(
                        isLoading = false,
                        rooms = currentRooms,
                        totalRooms = currentRooms.size,
                        activeRooms = currentRooms.count { r -> r.isAvailable }
                     )
                }
                sendEffect(RoomManageEffect.ShowDisableSuccess(updatedRoom))
            } else if (result is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.throwable.message) }
            }
        }
    }

    private fun loadRooms(hotelId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getHotelRoomsUseCase(hotelId).collect { result ->
                    if (result is Result.Success<*>) {
                        val rooms = (result as Result.Success<List<Room>>).data
                        _uiState.update { 
                             it.copy(
                                 isLoading = false,
                                 rooms = rooms,
                                 totalRooms = rooms.size,
                                 activeRooms = rooms.count { room -> room.isAvailable }
                             ) 
                         }
                    } else if (result is Result.Error) {
                        _uiState.update { it.copy(isLoading = false, error = result.throwable.message) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun sendEffect(effect: RoomManageEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
