package com.example.chillstay.ui.roomgallery

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RoomGalleryViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getRoomById: GetRoomByIdUseCase
) : BaseViewModel<RoomGalleryUiState, RoomGalleryIntent, RoomGalleryEffect>(RoomGalleryUiState()) {

    override fun onEvent(event: RoomGalleryIntent) = when (event) {
        is RoomGalleryIntent.LoadGallery -> load(event.hotelId, event.roomId)
        is RoomGalleryIntent.SelectCategory -> _state.update { it.updateSelectedCategory(event.category) }
    }

    private fun load(hotelId: String, roomId: String) {
        _state.update { it.updateIsLoading(true).clearError() }
        viewModelScope.launch {
            try {
                val hotelResult = getHotelById(hotelId).first()
                val roomResult = getRoomById(roomId).first()

                when (roomResult) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val room = roomResult.data
                        val gallery = room.gallery
                        _state.update {
                            it.updateExteriorView(gallery?.exteriorView ?: emptyList())
                                .updateFacilities(gallery?.facilities ?: emptyList())
                                .updateDining(gallery?.dining ?: emptyList())
                                .updateThisRoom(gallery?.thisRoom ?: listOfNotNull(room.imageUrl.takeIf { it.isNotBlank() }))
                                .updateRoomName(room.detail?.name ?: room.type)
                                .updateIsLoading(false)
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.update { it.updateIsLoading(false).updateError(roomResult.throwable.message) }
                        viewModelScope.launch { sendEffect { RoomGalleryEffect.ShowError(roomResult.throwable.message ?: "Failed to load room") } }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.updateIsLoading(false).updateError(e.message) }
                viewModelScope.launch { sendEffect { RoomGalleryEffect.ShowError(e.message ?: "Unknown error") } }
            }
        }
    }
}
