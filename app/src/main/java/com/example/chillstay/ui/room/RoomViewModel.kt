package com.example.chillstay.ui.room

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase
) : BaseViewModel<RoomUiState, RoomIntent, RoomEffect>(RoomUiState()) {

    override fun onEvent(event: RoomIntent) = when (event) {
        is RoomIntent.LoadRooms -> load(event.hotelId)
        is RoomIntent.RefreshRooms -> load(event.hotelId)
        is RoomIntent.RetryLoad -> {
            _state.update { it.clearError() }
            load(event.hotelId)
        }
    }

    private fun load(hotelId: String) {
        _state.update { it.updateIsLoading(true).clearError() }

        viewModelScope.launch {
            try {
                // Load hotel name (optional, for top bar)
                when (val hotelResult = getHotelById(hotelId).first()) {
                    is com.example.chillstay.core.common.Result.Success ->
                        _state.update { it.updateHotelName(hotelResult.data.name) }
                    is com.example.chillstay.core.common.Result.Error -> Unit
                }

                // Load rooms
                when (val roomsResult = getHotelRooms(hotelId).first()) {
                    is com.example.chillstay.core.common.Result.Success ->
                        _state.update { it.updateRooms(roomsResult.data).updateIsLoading(false) }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.update { it.updateIsLoading(false).updateError(roomsResult.throwable.message) }
                        viewModelScope.launch {
                            sendEffect { RoomEffect.ShowError(roomsResult.throwable.message ?: "Failed to load rooms") }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.updateIsLoading(false).updateError(e.message ?: "Unknown error") }
                viewModelScope.launch {
                    sendEffect { RoomEffect.ShowError(e.message ?: "Unknown error") }
                }
            }
        }
    }
}


