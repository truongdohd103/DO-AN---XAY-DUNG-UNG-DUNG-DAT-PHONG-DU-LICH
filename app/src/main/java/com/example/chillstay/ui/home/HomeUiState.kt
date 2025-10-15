package com.example.chillstay.ui.home

import androidx.compose.runtime.Immutable
import com.example.chillstay.domain.model.Hotel

@Immutable
data class HomeUiState(
    val isLoading: Boolean = true,
    val hotels: List<Hotel> = emptyList(),
    val selectedCategory: Int = 0,
    val error: String? = null
) {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateHotels(value: List<Hotel>) = copy(hotels = value)
    fun updateSelectedCategory(value: Int) = copy(selectedCategory = value)
    fun updateError(value: String?) = copy(error = value)
    fun clearError() = copy(error = null)
}
