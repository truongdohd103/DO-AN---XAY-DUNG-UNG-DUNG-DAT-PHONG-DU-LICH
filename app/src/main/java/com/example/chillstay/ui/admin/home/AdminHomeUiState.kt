package com.example.chillstay.ui.admin.home

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState

@Immutable
data class AdminHomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val greeting: String = "Hello 👋",
    val isStatisticsExpanded: Boolean = false
) : UiState
