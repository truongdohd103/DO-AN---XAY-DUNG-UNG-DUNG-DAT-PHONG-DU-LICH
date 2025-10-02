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

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    init {
        load()
    }

    private fun load() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching { getSampleItems() }
                .onSuccess { _state.value = HomeUiState(isLoading = false, items = it) }
                .onFailure { _state.value = HomeUiState(isLoading = false, error = it.message) }
        }
    }
}
