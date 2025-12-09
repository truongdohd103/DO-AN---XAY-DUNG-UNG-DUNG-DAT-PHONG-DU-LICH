package com.example.chillstay.ui.bookmark

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyBookmarkViewModel(
    private val getUserBookmarks: GetUserBookmarksUseCase,
    private val removeBookmark: RemoveBookmarkUseCase,
    private val getHotelById: GetHotelByIdUseCase
) : BaseViewModel<MyBookmarkUiState, MyBookmarkIntent, MyBookmarkEffect>(MyBookmarkUiState()) {

    override fun onEvent(event: MyBookmarkIntent) = when (event) {
        is MyBookmarkIntent.LoadBookmarks -> handleLoadBookmarks(event.userId)
        is MyBookmarkIntent.RemoveBookmark -> handleRemoveBookmark(event.bookmarkId, event.hotelId)
        is MyBookmarkIntent.RefreshBookmarks -> handleRefreshBookmarks(event.userId)
        is MyBookmarkIntent.RetryLoad -> handleRetryLoad(event.userId)
    }

    private fun handleLoadBookmarks(userId: String) {
        android.util.Log.d("MyBookmarkViewModel", "Loading bookmarks for user: $userId")
        _state.update { it.updateIsLoading(true).clearError() }
        
        viewModelScope.launch {
            try {
                val result = getUserBookmarks(userId)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        android.util.Log.d("MyBookmarkViewModel", "Found ${result.data.size} bookmarks")
                        loadHotelDetails(result.data.map { it.hotelId })
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        android.util.Log.e("MyBookmarkViewModel", "Error loading bookmarks: ${result.throwable.message}")
                        _state.update { 
                            it.updateIsLoading(false).updateError(result.throwable.message ?: "Failed to load bookmarks")
                        }
                    }
                }
            } catch (exception: Exception) {
                android.util.Log.e("MyBookmarkViewModel", "Exception loading bookmarks: ${exception.message}")
                _state.update { 
                    it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                }
            }
        }
    }

    private fun handleRemoveBookmark(bookmarkId: String, hotelId: String) {
        viewModelScope.launch {
            try {
                // Get current user ID from Firebase Auth
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                
                if (currentUserId != null) {
                    val result = removeBookmark(currentUserId, hotelId)
                    when (result) {
                        is com.example.chillstay.core.common.Result.Success -> {
                            // Remove hotel from current list
                            _state.update { currentState ->
                                val updatedHotels = currentState.hotels.filter { it.id != hotelId }
                                currentState.updateHotels(updatedHotels).updateIsEmpty(updatedHotels.isEmpty())
                            }
                        }
                        is com.example.chillstay.core.common.Result.Error -> {
                            // Handle error if needed
                        }
                    }
                } else {
                    // For now, just remove from UI without backend call
                    _state.update { currentState ->
                        val updatedHotels = currentState.hotels.filter { it.id != hotelId }
                        currentState.updateHotels(updatedHotels).updateIsEmpty(updatedHotels.isEmpty())
                    }
                }
            } catch (exception: Exception) {
                // Handle exception if needed
            }
        }
    }

    private fun handleRefreshBookmarks(userId: String) {
        handleLoadBookmarks(userId)
    }

    private fun handleRetryLoad(userId: String) {
        _state.update { it.clearError() }
        handleLoadBookmarks(userId)
    }

    private suspend fun loadHotelDetails(hotelIds: List<String>) {
        val hotels = mutableListOf<com.example.chillstay.domain.model.Hotel>()
        
        for (hotelId in hotelIds) {
            try {
                val result = getHotelById(hotelId).first()
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> hotels.add(result.data)
                    is com.example.chillstay.core.common.Result.Error -> {
                        // Log error but continue with other hotels
                    }
                }
            } catch (exception: Exception) {
                // Log error but continue with other hotels
            }
        }
        
        _state.update { 
            it.updateHotels(hotels)
                .updateIsLoading(false)
                .updateIsEmpty(hotels.isEmpty())
        }
    }
}
