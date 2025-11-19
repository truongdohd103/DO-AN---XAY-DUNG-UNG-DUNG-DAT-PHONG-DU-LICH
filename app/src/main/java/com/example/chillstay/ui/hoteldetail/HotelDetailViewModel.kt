package com.example.chillstay.ui.hoteldetail

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.review.GetHotelReviewsUseCase
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import android.util.Log

class HotelDetailViewModel(
    private val getHotelById: GetHotelByIdUseCase,
    private val getHotelRooms: GetHotelRoomsUseCase,
    private val getHotelReviews: GetHotelReviewsUseCase,
    private val userRepository: UserRepository,
    private val addBookmark: AddBookmarkUseCase,
    private val removeBookmark: RemoveBookmarkUseCase
) : BaseViewModel<HotelDetailUiState, HotelDetailIntent, HotelDetailEffect>(HotelDetailUiState()) {

    override fun onEvent(event: HotelDetailIntent) = when (event) {
        is HotelDetailIntent.LoadHotelDetails -> handleLoadHotelDetails(event.hotelId)
        is HotelDetailIntent.ToggleBookmark -> handleToggleBookmark(event.hotelId, event.isBookmarked)
        is HotelDetailIntent.RetryLoad -> handleRetryLoad(event.hotelId)
        is HotelDetailIntent.NavigateToRooms -> handleNavigateToRooms(event.hotelId)
    }

    private fun handleLoadHotelDetails(hotelId: String) {
        _state.update { it.updateIsLoading(true).clearError() }

        viewModelScope.launch {
            try {
                when (val hotelResult = getHotelById(hotelId)) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        _state.update {
                            it.updateHotel(hotelResult.data)
                                .updateIsLoading(false)
                        }

                        supervisorScope {
                            launch { loadHotelRooms(hotelId) }
                            launch { loadHotelReviews(hotelId) }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.update {
                            it.updateIsLoading(false)
                                .updateError(hotelResult.throwable.message ?: "Failed to load hotel")
                        }
                    }
                }
            } catch (exception: Exception) {
                _state.update {
                    it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                }
                viewModelScope.launch {
                    sendEffect { HotelDetailEffect.ShowError(exception.message ?: "Failed to load hotel details") }
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
                        viewModelScope.launch {
                            if (isBookmarked) {
                                sendEffect { HotelDetailEffect.ShowBookmarkRemoved }
                            } else {
                                sendEffect { HotelDetailEffect.ShowBookmarkAdded }
                            }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        viewModelScope.launch {
                            sendEffect { HotelDetailEffect.RequireAuthentication }
                        }
                    }
                }
            } catch (_: Exception) {
                viewModelScope.launch {
                    sendEffect { HotelDetailEffect.ShowError("Something went wrong with bookmark operation") }
                }
            }
        }
    }

    private fun handleRetryLoad(hotelId: String) {
        _state.update { it.clearError() }
        handleLoadHotelDetails(hotelId)
    }

    private fun handleNavigateToRooms(hotelId: String) {
        viewModelScope.launch {
            sendEffect { HotelDetailEffect.NavigateToRoomSelection(hotelId) }
        }
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
                    }
                }
                is com.example.chillstay.core.common.Result.Error -> {
                    _state.update { 
                        it.updateError(result.throwable.message ?: "Failed to load rooms")
                    }
                }
            }
        } catch (exception: Exception) {
            _state.update { 
                it.updateError(exception.message ?: "Failed to load rooms")
            }
            viewModelScope.launch {
                sendEffect { HotelDetailEffect.ShowError(exception.message ?: "Failed to load rooms") }
            }
        }
    }

    private suspend fun loadHotelReviews(hotelId: String) {
        try {
            val result = getHotelReviews(hotelId, limit = 10, offset = 0)
            when (result) {
                is com.example.chillstay.core.common.Result.Success -> {
                    val reviews = result.data
                    Log.d("HotelDetailViewModel", "Successfully loaded ${reviews.size} reviews")
                    
                    _state.update { it.updateReviews(reviews) }
                    
                    // Load users cho reviews (song song để tăng tốc)
                    loadUsersForReviews(reviews)
                }
                is com.example.chillstay.core.common.Result.Error -> {
                    Log.e("HotelDetailViewModel", "Error loading reviews: ${result.throwable.message}", result.throwable)
                    _state.update { 
                        it.updateReviews(emptyList())
                            .updateReviewsWithUser(emptyList())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("HotelDetailViewModel", "Exception loading reviews: ${e.message}", e)
            _state.update { 
                it.updateReviews(emptyList())
                    .updateReviewsWithUser(emptyList())
            }
        }
    }

    /**
     * Load user info cho tất cả reviews (load song song để tăng tốc)
     */
    private suspend fun loadUsersForReviews(reviews: List<com.example.chillstay.domain.model.Review>) {
        if (reviews.isEmpty()) {
            _state.update { it.updateReviewsWithUser(emptyList()) }
            return
        }
        
        try {
            Log.d("HotelDetailViewModel", "Loading users for ${reviews.size} reviews")
            
            coroutineScope {
                // Load tất cả users song song (parallel) để tăng tốc
                val reviewsWithUser = reviews.map { review ->
                    async {
                        val user = try {
                            val user = userRepository.getUser(review.userId)
                            if (user != null) {
                                Log.d("HotelDetailViewModel", "Loaded user: id=${user.id}, fullName=${user.fullName}, email=${user.email}")
                            } else {
                                Log.w("HotelDetailViewModel", "User not found: ${review.userId}")
                            }
                            user
                        } catch (e: Exception) {
                            Log.e("HotelDetailViewModel", "Error loading user ${review.userId}: ${e.message}")
                            null
                        }
                        ReviewWithUser(review, user)
                    }
                }.map { it.await() }
                
                Log.d("HotelDetailViewModel", "Loaded ${reviewsWithUser.size} reviews with user info")
                _state.update { it.updateReviewsWithUser(reviewsWithUser) }
            }
        } catch (e: Exception) {
            Log.e("HotelDetailViewModel", "Error loading users for reviews: ${e.message}", e)
            // Fallback: tạo reviewsWithUser mà không có user info
            val reviewsWithUser = reviews.map { review ->
                ReviewWithUser(review, null)
            }
            _state.update { it.updateReviewsWithUser(reviewsWithUser) }
        }
    }
}
