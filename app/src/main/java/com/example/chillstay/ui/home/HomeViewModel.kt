package com.example.chillstay.ui.home

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import com.example.chillstay.domain.model.HotelCategory
import com.example.chillstay.domain.model.HotelListFilter
import com.example.chillstay.ui.home.HomeIntent.ChangeHotelCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHotelsUseCase: GetHotelsUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val getUserBookmarksUseCase: GetUserBookmarksUseCase,
    private val getUserBookingsUseCase: GetUserBookingsUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : BaseViewModel<HomeUiState, HomeIntent, HomeEffect>(HomeUiState()) {

    private var currentUserId: String? = null
    private val categoryJobs: MutableMap<HotelCategory, Job?> = mutableMapOf()

    init {
        observeCurrentUser()
        loadCategory(HotelCategory.POPULAR, force = true)
        prefetchCategories()
    }

    override fun onEvent(event: HomeIntent) {
        when (event) {
            is ChangeHotelCategory -> {
                val category = event.categoryIndex.toHotelCategory()
                _state.update { it.updateSelectedCategory(category) }
                loadCategory(category)
            }
            HomeIntent.RefreshHotels -> loadCategory(_state.value.selectedCategory, force = true)
            is HomeIntent.ToggleBookmark -> toggleBookmark(event.hotelId)
            HomeIntent.RefreshBookmarks -> currentUserId?.let { loadBookmarks(it) }
            HomeIntent.RefreshUserSections -> currentUserId?.let { loadUserSections(it, force = true) }
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            getCurrentUserIdUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        currentUserId = result.data
                        val userId = result.data
                        if (userId.isNullOrBlank()) {
                            _state.update {
                                it.copy(
                                    bookmarkedHotels = emptySet(),
                                    pendingBookings = emptyList(),
                                    recentHotels = emptyList()
                                )
                            }
                        } else {
                            loadBookmarks(userId)
                            loadUserSections(userId, force = true)
                        }
                    }
                    is Result.Error -> {
                        _state.update { it.updateError(result.throwable.message ?: "Không thể xác định người dùng") }
                    }
                }
            }
        }
    }

    private fun loadCategory(category: HotelCategory, force: Boolean = false) {
        if (!force && _state.value.hotelsByCategory.containsKey(category)) {
            _state.update { it.updateSelectedCategory(category).updateIsLoading(false).clearError() }
            return
        }

        categoryJobs[category]?.cancel()
        categoryJobs[category] = viewModelScope.launch {
            _state.update { it.updateIsLoading(true).clearError().updateSelectedCategory(category) }
            getHotelsUseCase(
                HotelListFilter(
                    category = category,
                    limit = when (category) {
                        HotelCategory.POPULAR -> 5
                        HotelCategory.RECOMMENDED -> 3
                        HotelCategory.TRENDING -> 6
                        HotelCategory.ALL -> null
                    }
                )
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update {
                            it.storeHotels(category, result.data)
                                .updateIsLoading(false)
                                .clearError()
                        }
                    }
                    is Result.Error -> {
                        _state.update {
                            it.updateIsLoading(false)
                                .updateError(result.throwable.message ?: "Không thể tải danh sách khách sạn")
                        }
                        sendEffect { HomeEffect.ShowError(result.throwable.message ?: "Không thể tải danh sách khách sạn") }
                    }
                }
            }
        }
    }

    private fun prefetchCategories() {
        viewModelScope.launch {
            // Prefetch Recommended
            getHotelsUseCase(
                HotelListFilter(category = HotelCategory.RECOMMENDED, limit = 3)
            ).collectLatest { result ->
                if (result is Result.Success) {
                    _state.update { it.storeHotels(HotelCategory.RECOMMENDED, result.data) }
                }
            }
        }
        viewModelScope.launch {
            // Prefetch Trending
            getHotelsUseCase(
                HotelListFilter(category = HotelCategory.TRENDING, limit = 6)
            ).collectLatest { result ->
                if (result is Result.Success) {
                    _state.update { it.storeHotels(HotelCategory.TRENDING, result.data) }
                }
            }
        }
    }

    private fun toggleBookmark(hotelId: String) {
        val userId = currentUserId
        if (userId.isNullOrBlank()) {
            viewModelScope.launch {
                sendEffect { HomeEffect.RequireAuthentication }
            }
            return
        }

        val isCurrentlyBookmarked = _state.value.bookmarkedHotels.contains(hotelId)
        _state.update { it.toggleBookmark(hotelId) }

        viewModelScope.launch {
            val result = if (isCurrentlyBookmarked) {
                removeBookmarkUseCase(userId, hotelId)
            } else {
                addBookmarkUseCase(userId, hotelId)
            }

            when (result) {
                is Result.Success -> {
                    val effect = if (isCurrentlyBookmarked) HomeEffect.ShowBookmarkRemoved else HomeEffect.ShowBookmarkAdded
                    sendEffect { effect }
                }
                is Result.Error -> {
                    _state.update { it.toggleBookmark(hotelId) }
                    sendEffect { HomeEffect.ShowError(result.throwable.message ?: "Thao tác bookmark thất bại") }
                }
            }
        }
    }

    private fun loadBookmarks(userId: String) {
        viewModelScope.launch {
            when (val result = getUserBookmarksUseCase(userId)) {
                is Result.Success -> {
                    _state.update { it.updateBookmarkedHotels(result.data.map { bookmark -> bookmark.hotelId }.toSet()) }
                }
                is Result.Error -> {
                    sendEffect { HomeEffect.ShowError(result.throwable.message ?: "Không thể tải bookmark") }
                }
            }
        }
    }

    private fun loadUserSections(userId: String, force: Boolean) {
        loadPendingBookings(userId, force)
        loadRecentHotels(userId, force)
    }

    private fun loadPendingBookings(userId: String, force: Boolean) {
        if (!force && _state.value.pendingBookings.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.updatePendingBookings(it.pendingBookings, isLoading = true) }
            when (val result = getUserBookingsUseCase(userId, BookingStatus.PENDING.name)) {
                is Result.Success -> {
                    val pendingItems = mutableListOf<PendingDisplayItem>()
                    for (booking in result.data.sortedByDescending { it.createdAt.toDate() }) {
                        val hotel = booking.hotel ?: resolveHotel(booking.hotelId)
                        val room = booking.room ?: resolveRoom(booking.roomId)
                        pendingItems += PendingDisplayItem(
                            hotelName = hotel?.name,
                            dateFrom = booking.dateFrom,
                            dateTo = booking.dateTo,
                            guests = booking.guests,
                            createdAt = booking.createdAt.toDate(),
                            hotelId = booking.hotelId,
                            roomId = booking.roomId
                        )
                    }
                    _state.update { it.updatePendingBookings(pendingItems, isLoading = false) }
                }
                is Result.Error -> {
                    _state.update { it.updatePendingBookings(emptyList(), isLoading = false) }
                }
            }
        }
    }

    private fun loadRecentHotels(userId: String, force: Boolean) {
        if (!force && _state.value.recentHotels.isNotEmpty()) return
        viewModelScope.launch {
            _state.update { it.updateRecentHotels(it.recentHotels, isLoading = true) }
            when (val result = getUserBookingsUseCase(userId, BookingStatus.COMPLETED.name)) {
                is Result.Success -> {
                    val hotels = result.data
                        .sortedByDescending { it.updatedAt.toDate() }
                        .mapNotNull { booking ->
                            booking.hotel ?: booking.hotelId.takeIf { it.isNotBlank() }?.let { hotelId ->
                                resolveHotel(hotelId)
                            }
                        }
                        .distinctBy { it.id }
                        .take(4)
                    _state.update { it.updateRecentHotels(hotels, isLoading = false) }
                }
                is Result.Error -> {
                    _state.update { it.updateRecentHotels(emptyList(), isLoading = false) }
                }
            }
        }
    }

    private suspend fun resolveHotel(hotelId: String): Hotel? {
        return try {
            getHotelByIdUseCase(hotelId).first().let { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resolveRoom(roomId: String): com.example.chillstay.domain.model.Room? {
        return try {
            getRoomByIdUseCase(roomId).first().let { result ->
                when (result) {
                    is Result.Success -> result.data
                    is Result.Error -> null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun Int.toHotelCategory(): HotelCategory = when (this) {
        0 -> HotelCategory.POPULAR
        1 -> HotelCategory.RECOMMENDED
        2 -> HotelCategory.TRENDING
        else -> HotelCategory.POPULAR
    }
}
