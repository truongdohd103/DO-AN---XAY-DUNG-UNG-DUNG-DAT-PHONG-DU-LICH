package com.example.chillstay.ui.home

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelCategory
import java.util.Date

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val selectedCategory: HotelCategory = HotelCategory.POPULAR,
    val hotelsByCategory: Map<HotelCategory, List<Hotel>> = emptyMap(),
    val errorMessage: String? = null,
    val bookmarkedHotels: Set<String> = emptySet(),
    val pendingBookings: List<PendingDisplayItem> = emptyList(),
    val recentHotels: List<Hotel> = emptyList(),
    val isPendingBookingsLoading: Boolean = false,
    val isRecentHotelsLoading: Boolean = false
) : UiState {
    val hotels: List<Hotel>
        get() = hotelsByCategory[selectedCategory] ?: emptyList()

    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateSelectedCategory(value: HotelCategory) = copy(selectedCategory = value)
    fun updateError(value: String?) = copy(errorMessage = value)
    fun clearError() = copy(errorMessage = null)
    fun updateBookmarkedHotels(value: Set<String>) = copy(bookmarkedHotels = value)
    fun toggleBookmark(hotelId: String) = copy(
        bookmarkedHotels = if (bookmarkedHotels.contains(hotelId)) {
            bookmarkedHotels - hotelId
        } else {
            bookmarkedHotels + hotelId
        }
    )
    fun storeHotels(category: HotelCategory, hotels: List<Hotel>) =
        copy(hotelsByCategory = hotelsByCategory + (category to hotels))
    fun updatePendingBookings(items: List<PendingDisplayItem>, isLoading: Boolean) =
        copy(pendingBookings = items, isPendingBookingsLoading = isLoading)
    fun updateRecentHotels(hotels: List<Hotel>, isLoading: Boolean) =
        copy(recentHotels = hotels, isRecentHotelsLoading = isLoading)
}

data class PendingDisplayItem(
    val hotelName: String?,
    val dateFrom: String,
    val dateTo: String,
    val guests: Int,
    val createdAt: Date?,
    val hotelId: String,
    val roomId: String
)
