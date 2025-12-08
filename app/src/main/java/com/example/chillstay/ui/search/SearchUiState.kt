package com.example.chillstay.ui.search

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel

@Immutable
data class SearchUiState(
    val query: String = "",
    val country: String = "",
    val city: String = "",
    val minRating: String = "",
    val maxPrice: String = "",
    val isLoading: Boolean = false,
    val results: List<Hotel> = emptyList(),
    val errorMessage: String? = null
) : UiState


