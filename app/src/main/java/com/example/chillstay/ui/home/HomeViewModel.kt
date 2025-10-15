package com.example.chillstay.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.data.api.ChillStayApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: ChillStayApi
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadCategory(0)
    }

    fun handleIntent(intent: HomeIntent) = when (intent) {
        is HomeIntent.ChangeHotelCategory -> handleChangeHotelCategory(intent.categoryIndex)
        is HomeIntent.RefreshHotels -> handleRefreshHotels(intent.categoryIndex)
        is HomeIntent.RetryLoadHotels -> handleRetryLoadHotels(intent.categoryIndex)
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
}
