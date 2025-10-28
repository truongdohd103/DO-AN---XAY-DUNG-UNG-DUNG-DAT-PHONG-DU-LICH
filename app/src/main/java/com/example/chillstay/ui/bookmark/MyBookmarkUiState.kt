package com.example.chillstay.ui.bookmark

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel

@Immutable
data class MyBookmarkUiState(
    val isLoading: Boolean = true,
    val hotels: List<Hotel> = emptyList(),
    val error: String? = null,
    val isEmpty: Boolean = false
) : UiState {
    fun updateIsLoading(value: Boolean) = copy(isLoading = value)
    fun updateHotels(value: List<Hotel>) = copy(hotels = value)
    fun updateError(value: String?) = copy(error = value)
    fun updateIsEmpty(value: Boolean) = copy(isEmpty = value)
    fun clearError() = copy(error = null)
}
