package com.example.chillstay.ui.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RoomUiState())
    val state: StateFlow<RoomUiState> = _state.asStateFlow()

    fun handleIntent(intent: RoomIntent) = when (intent) {
        is RoomIntent.LoadRooms -> load(intent.hotelId)
        is RoomIntent.RefreshRooms -> load(intent.hotelId)
        is RoomIntent.RetryLoad -> {
            _state.update { it.clearError() }
            load(intent.hotelId)
        }
    }

    private fun load(hotelId: String) {
        _state.update { it.updateIsLoading(true).clearError() }

        viewModelScope.launch {
            try {
                // Load hotel name (optional, for top bar)
                when (val hotelResult = getHotelById(hotelId)) {
                    is com.example.chillstay.core.common.Result.Success ->
                        _state.update { it.updateHotelName(hotelResult.data.name) }
                    is com.example.chillstay.core.common.Result.Error -> Unit
                }

                // Load rooms
                when (val roomsResult = getHotelRooms(hotelId)) {
                    is com.example.chillstay.core.common.Result.Success ->
                        _state.update { it.updateRooms(roomsResult.data).updateIsLoading(false) }
                    is com.example.chillstay.core.common.Result.Error ->
                        _state.update { it.updateIsLoading(false).updateError(roomsResult.throwable.message) }
                }
            } catch (e: Exception) {
                _state.update { it.updateIsLoading(false).updateError(e.message ?: "Unknown error") }
            }
        }
    }
}


