package com.example.chillstay.ui.hoteldetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HotelDetailViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase,
    private val addBookmark: AddBookmarkUseCase,
    private val removeBookmark: RemoveBookmarkUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HotelDetailUiState())
    val state: StateFlow<HotelDetailUiState> = _state.asStateFlow()

    fun handleIntent(intent: HotelDetailIntent) = when (intent) {
        is HotelDetailIntent.LoadHotelDetails -> handleLoadHotelDetails(intent.hotelId)
        is HotelDetailIntent.ToggleBookmark -> handleToggleBookmark(intent.hotelId, intent.isBookmarked)
        is HotelDetailIntent.RetryLoad -> handleRetryLoad(intent.hotelId)
        is HotelDetailIntent.NavigateToRooms -> handleNavigateToRooms(intent.hotelId)
    }

    private fun handleLoadHotelDetails(hotelId: String) {
        _state.update { it.updateIsLoading(true).clearError() }

        viewModelScope.launch {
            try {
                val result = getHotelById(hotelId)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        _state.update { it.updateHotel(result.data) }
                        loadHotelRooms(hotelId)
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.update {
                            it.updateIsLoading(false).updateError(result.throwable.message ?: "Failed to load hotel")
                        }
                    }
                }
            } catch (exception: Exception) {
                _state.update {
                    it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                }
            }
        }
    }

    private fun handleToggleBookmark(hotelId: String, isBookmarked: Boolean) {
        viewModelScope.launch {
            try {
                // Note: This needs userId, but we don't have it in this context
                // This should be handled differently in a real implementation
                val result = if (isBookmarked) {
                    // removeBookmark(userId, hotelId) // Need userId
                    com.example.chillstay.core.common.Result.failure(Exception("Need userId"))
                } else {
                    // addBookmark(userId, hotelId) // Need userId
                    com.example.chillstay.core.common.Result.failure(Exception("Need userId"))
                }
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        _state.update { it.updateIsBookmarked(!isBookmarked) }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        // Handle error if needed
                    }
                }
            } catch (exception: Exception) {
                // Handle exception if needed
            }
        }
    }

    private fun handleRetryLoad(hotelId: String) {
        _state.update { it.clearError() }
        handleLoadHotelDetails(hotelId)
    }

    private fun handleNavigateToRooms(hotelId: String) {
        // This will be handled by navigation logic
    }

    private suspend fun loadHotelRooms(hotelId: String) {
        try {
            val result = getHotelRooms(hotelId)
            when (result) {
                is com.example.chillstay.core.common.Result.Success -> {
                    val rooms = result.data
                    val minPrice = rooms.minByOrNull { it.price }?.price?.toInt()
                    _state.update { 
                        it.updateRooms(rooms)
                            .updateMinPrice(minPrice)
                            .updateIsLoading(false)
                    }
                }
                is com.example.chillstay.core.common.Result.Error -> {
                    _state.update { 
                        it.updateIsLoading(false).updateError(result.throwable.message ?: "Failed to load rooms")
                    }
                }
            }
        } catch (exception: Exception) {
            _state.update { 
                it.updateIsLoading(false).updateError(exception.message ?: "Failed to load rooms")
            }
        }
    }
}
