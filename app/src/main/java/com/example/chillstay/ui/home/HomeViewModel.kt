package com.example.chillstay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.SampleItem
import com.example.chillstay.domain.usecase.GetSampleItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val items: List<SampleItem> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val getSampleItems: GetSampleItems
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        load()
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { getSampleItems() }
                .onSuccess { _uiState.value = HomeUiState(isLoading = false, items = it) }
                .onFailure { _uiState.value = HomeUiState(isLoading = false, error = it.message) }
        }
    }
}
