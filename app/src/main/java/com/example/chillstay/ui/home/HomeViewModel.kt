package com.example.chillstay.ui.home

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.data.api.ChillStayApi
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class HomeViewModel(
    private val api: ChillStayApi,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val getUserBookmarksUseCase: GetUserBookmarksUseCase
) : BaseViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {

    init {
        loadCategory(0)
        loadUserBookmarks()
    }

    override fun onEvent(event: HomeIntent) = when (event) {
        is HomeIntent.ChangeHotelCategory -> handleChangeHotelCategory(event.categoryIndex)
        is HomeIntent.RefreshHotels -> handleRefreshHotels(event.categoryIndex)
        is HomeIntent.RetryLoadHotels -> handleRetryLoadHotels(event.categoryIndex)
        is HomeIntent.ToggleBookmark -> handleToggleBookmark(event.hotelId)
        is HomeIntent.RefreshBookmarks -> handleRefreshBookmarks()
    }

    private fun handleChangeHotelCategory(categoryIndex: Int) {
        _state.update { it.updateSelectedCategory(categoryIndex) }
        loadCategory(categoryIndex)
    }

    private fun handleRefreshHotels(categoryIndex: Int) {
        loadCategory(categoryIndex)
    }

    private fun handleRetryLoadHotels(categoryIndex: Int) {
        _state.update { it.clearError() }
        loadCategory(categoryIndex)
    }

    /**
     * Load hotel category data on background thread to prevent main thread blocking
     * Uses Dispatchers.IO for network operations
     */
    private fun loadCategory(index: Int) {
        val categoryName = when (index) {
            0 -> "Popular"
            1 -> "Recommended"
            else -> "Trending"
        }
        Log.d("HomeViewModel", "==========================================")
        Log.d("HomeViewModel", "Loading category: $categoryName (index: $index)")
        _state.update { it.updateIsLoading(true).updateError(null) }

        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Calling API on background thread (IO dispatcher)")
                
                val hotels = withContext(Dispatchers.IO) {
                    val loader = when (index) {
                        0 -> suspend { api.getPopularHotels(limit = 5) }
                        1 -> suspend { api.getRecommendedHotels(limit = 3) }
                        else -> suspend { api.getTrendingHotels(limit = 2) }
                    }
                    loader()
                }
                
                Log.d("HomeViewModel", "API returned ${hotels.size} hotels for category $categoryName")
                if (hotels.isEmpty()) {
                    Log.w("HomeViewModel", "⚠️ WARNING: No hotels returned from API for category $categoryName")
                } else {
                    Log.d("HomeViewModel", "Hotels received:")
                    hotels.forEachIndexed { idx, hotel ->
                        Log.d("HomeViewModel", "  [$idx] ${hotel.name} (ID: ${hotel.id}) - ${hotel.city}, ${hotel.country} - Rating: ${hotel.rating}")
                        Log.d("HomeViewModel", "      Image URLs: ${hotel.imageUrl.size} images")
                        Log.d("HomeViewModel", "      Rooms: ${hotel.rooms.size} rooms")
                    }
                }
                
                Log.d("HomeViewModel", "Updating UI state with ${hotels.size} hotels")
                _state.update { 
                    it.updateIsLoading(false).updateHotels(hotels) 
                }
                
                val updatedState = _state.value
                Log.d("HomeViewModel", "UI State updated - hotels count: ${updatedState.hotels.size}, isLoading: ${updatedState.isLoading}")
                Log.d("HomeViewModel", "==========================================")
                
            } catch (exception: Exception) {
                Log.e("HomeViewModel", "❌ ERROR loading category $categoryName: ${exception.message}", exception)
                Log.e("HomeViewModel", "Exception stack trace:", exception)
                _state.update {
                    it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                }
                viewModelScope.launch {
                    sendEffect { HomeEffect.ShowError(exception.message ?: "Failed to load hotels") }
                }
            }
        }
    }

    /**
     * Load user bookmarks on background thread to prevent main thread blocking
     * Uses Dispatchers.IO for Firestore operations
     */
    private fun loadUserBookmarks() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("HomeViewModel", "Loading bookmarks for user: $currentUserId")
        
        if (currentUserId != null) {
            viewModelScope.launch {
                try {
                    Log.d("HomeViewModel", "Loading bookmarks on background thread")
                    
                    val result = withContext(Dispatchers.IO) {
                        getUserBookmarksUseCase(currentUserId)
                    }
                    
                    when (result) {
                        is com.example.chillstay.core.common.Result.Success -> {
                            val bookmarkedHotelIds = result.data.map { it.hotelId }.toSet()
                            Log.d("HomeViewModel", "Loaded ${result.data.size} bookmarks: $bookmarkedHotelIds")
                            _state.update { it.updateBookmarkedHotels(bookmarkedHotelIds) }
                        }
                        is com.example.chillstay.core.common.Result.Error -> {
                            Log.e("HomeViewModel", "Error loading bookmarks: ${result.throwable.message}", result.throwable)
                        }
                    }
                } catch (exception: Exception) {
                    Log.e("HomeViewModel", "Exception loading bookmarks: ${exception.message}", exception)
                }
            }
        }
    }

    /**
     * Handle bookmark toggle with background thread operations
     * Uses optimistic UI updates for better UX
     */
    private fun handleToggleBookmark(hotelId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("HomeViewModel", "Toggle bookmark for hotel: $hotelId, user: $currentUserId")
        
        if (currentUserId != null) {
            val isCurrentlyBookmarked = _state.value.bookmarkedHotels.contains(hotelId)
            Log.d("HomeViewModel", "Currently bookmarked: $isCurrentlyBookmarked")
            
            // Update UI immediately for better UX (optimistic update)
            _state.update { it.toggleBookmark(hotelId) }
            
            viewModelScope.launch {
                try {
                    if (isCurrentlyBookmarked) {
                        // Remove bookmark on background thread
                        Log.d("HomeViewModel", "Removing bookmark for hotel: $hotelId")
                        val result = withContext(Dispatchers.IO) {
                            removeBookmarkUseCase(currentUserId, hotelId)
                        }
                        
                        if (result is com.example.chillstay.core.common.Result.Success) {
                            Log.d("HomeViewModel", "Successfully removed bookmark")
                            viewModelScope.launch {
                                sendEffect { HomeEffect.ShowBookmarkRemoved }
                            }
                        } else {
                            Log.e("HomeViewModel", "Failed to remove bookmark: ${(result as com.example.chillstay.core.common.Result.Error).throwable.message}")
                            // Revert UI change if backend call failed
                            _state.update { it.toggleBookmark(hotelId) }
                            viewModelScope.launch {
                                sendEffect { HomeEffect.ShowError("Failed to remove bookmark") }
                            }
                        }
                    } else {
                        // Add bookmark on background thread
                        Log.d("HomeViewModel", "Adding bookmark for hotel: $hotelId")
                        val result = withContext(Dispatchers.IO) {
                            addBookmarkUseCase(currentUserId, hotelId)
                        }
                        
                        if (result is com.example.chillstay.core.common.Result.Success) {
                            Log.d("HomeViewModel", "Successfully added bookmark")
                            viewModelScope.launch {
                                sendEffect { HomeEffect.ShowBookmarkAdded }
                            }
                        } else {
                            Log.e("HomeViewModel", "Failed to add bookmark: ${(result as com.example.chillstay.core.common.Result.Error).throwable.message}")
                            // Revert UI change if backend call failed
                            _state.update { it.toggleBookmark(hotelId) }
                            viewModelScope.launch {
                                sendEffect { HomeEffect.ShowError("Failed to add bookmark") }
                            }
                        }
                    }
                } catch (exception: Exception) {
                    Log.e("HomeViewModel", "Exception in toggle bookmark: ${exception.message}", exception)
                    // Revert UI change if backend call failed
                    _state.update { it.toggleBookmark(hotelId) }
                    viewModelScope.launch {
                        sendEffect { HomeEffect.ShowError("Something went wrong with bookmark operation") }
                    }
                }
            }
        }
    }

    private fun handleRefreshBookmarks() {
        loadUserBookmarks()
    }
}