package com.example.chillstay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.data.api.ChillStayApi
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: ChillStayApi,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val getUserBookmarksUseCase: GetUserBookmarksUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadCategory(0)
        loadUserBookmarks()
    }

    fun handleIntent(intent: HomeIntent) = when (intent) {
        is HomeIntent.ChangeHotelCategory -> handleChangeHotelCategory(intent.categoryIndex)
        is HomeIntent.RefreshHotels -> handleRefreshHotels(intent.categoryIndex)
        is HomeIntent.RetryLoadHotels -> handleRetryLoadHotels(intent.categoryIndex)
        is HomeIntent.ToggleBookmark -> handleToggleBookmark(intent.hotelId)
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

    private fun loadCategory(index: Int) {
        _state.update { it.updateIsLoading(true).updateError(null) }

        viewModelScope.launch {
            val loader = when (index) {
                0 -> suspend { api.getPopularHotels(limit = 5) }
                1 -> suspend { api.getRecommendedHotels(limit = 3) }
                else -> suspend { api.getTrendingHotels(limit = 2) }
            }

            runCatching { loader() }
                .onSuccess { hotels ->
                    _state.update { it.updateIsLoading(false).updateHotels(hotels) }
                }
                .onFailure { exception ->
                    _state.update {
                        it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                    }
                }
        }
    }

    private fun loadUserBookmarks() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("HomeViewModel", "Loading bookmarks for user: $currentUserId")
        if (currentUserId != null) {
            viewModelScope.launch {
                try {
                    val result = getUserBookmarksUseCase(currentUserId)
                    when (result) {
                        is com.example.chillstay.core.common.Result.Success -> {
                            val bookmarkedHotelIds = result.data.map { it.hotelId }.toSet()
                            android.util.Log.d("HomeViewModel", "Loaded ${result.data.size} bookmarks: $bookmarkedHotelIds")
                            _state.update { it.updateBookmarkedHotels(bookmarkedHotelIds) }
                        }
                        is com.example.chillstay.core.common.Result.Error -> {
                            android.util.Log.e("HomeViewModel", "Error loading bookmarks: ${result.throwable.message}")
                        }
                    }
                } catch (exception: Exception) {
                    android.util.Log.e("HomeViewModel", "Exception loading bookmarks: ${exception.message}")
                }
            }
        }
    }

    private fun handleToggleBookmark(hotelId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("HomeViewModel", "Toggle bookmark for hotel: $hotelId, user: $currentUserId")
        if (currentUserId != null) {
            val isCurrentlyBookmarked = _state.value.bookmarkedHotels.contains(hotelId)
            android.util.Log.d("HomeViewModel", "Currently bookmarked: $isCurrentlyBookmarked")
            
            // Update UI immediately for better UX
            _state.update { it.toggleBookmark(hotelId) }
            
            viewModelScope.launch {
                try {
                    if (isCurrentlyBookmarked) {
                        // Remove bookmark
                        android.util.Log.d("HomeViewModel", "Removing bookmark for hotel: $hotelId")
                        val result = removeBookmarkUseCase(currentUserId, hotelId)
                        if (result is com.example.chillstay.core.common.Result.Success) {
                            android.util.Log.d("HomeViewModel", "Successfully removed bookmark")
                        } else {
                            android.util.Log.e("HomeViewModel", "Failed to remove bookmark: ${(result as com.example.chillstay.core.common.Result.Error).throwable.message}")
                            // Revert UI change if backend call failed
                            _state.update { it.toggleBookmark(hotelId) }
                        }
                    } else {
                        // Add bookmark
                        android.util.Log.d("HomeViewModel", "Adding bookmark for hotel: $hotelId")
                        val result = addBookmarkUseCase(currentUserId, hotelId)
                        if (result is com.example.chillstay.core.common.Result.Success) {
                            android.util.Log.d("HomeViewModel", "Successfully added bookmark")
                        } else {
                            android.util.Log.e("HomeViewModel", "Failed to add bookmark: ${(result as com.example.chillstay.core.common.Result.Error).throwable.message}")
                            // Revert UI change if backend call failed
                            _state.update { it.toggleBookmark(hotelId) }
                        }
                    }
                } catch (exception: Exception) {
                    android.util.Log.e("HomeViewModel", "Exception in toggle bookmark: ${exception.message}")
                    // Revert UI change if backend call failed
                    _state.update { it.toggleBookmark(hotelId) }
                }
            }
        }
    }

    private fun handleRefreshBookmarks() {
        loadUserBookmarks()
    }
}
