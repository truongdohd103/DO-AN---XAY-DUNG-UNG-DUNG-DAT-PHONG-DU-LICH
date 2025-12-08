package com.example.chillstay.ui.search

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.hotel.SearchHotelsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchHotelsUseCase: SearchHotelsUseCase
) : BaseViewModel<SearchUiState, SearchUiEvent, SearchUiEffect>(SearchUiState()) {

    private var searchJob: Job? = null

    override fun onEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.QueryChanged -> _state.update { it.copy(query = event.value) }
            is SearchUiEvent.CountryChanged -> _state.update { it.copy(country = event.value) }
            is SearchUiEvent.CityChanged -> _state.update { it.copy(city = event.value) }
            is SearchUiEvent.MinRatingChanged -> _state.update { it.copy(minRating = event.value) }
            is SearchUiEvent.MaxPriceChanged -> _state.update { it.copy(maxPrice = event.value) }
            SearchUiEvent.Submit -> performSearch()
            SearchUiEvent.ClearFilters -> _state.update {
                it.copy(
                    country = "",
                    city = "",
                    minRating = "",
                    maxPrice = ""
                )
            }
        }
    }

    private fun performSearch() {
        val currentQuery = _state.value.query.trim()
        if (currentQuery.isBlank()) {
            viewModelScope.launch {
                sendEffect { SearchUiEffect.ShowMessage("Vui lòng nhập từ khoá tìm kiếm") }
            }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            searchHotelsUseCase(
                query = currentQuery,
                country = _state.value.country.takeIf { it.isNotBlank() },
                city = _state.value.city.takeIf { it.isNotBlank() },
                minRating = _state.value.minRating.toDoubleOrNull(),
                maxPrice = _state.value.maxPrice.toDoubleOrNull()
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false, results = result.data, errorMessage = null) }
                        if (result.data.isEmpty()) {
                            sendEffect { SearchUiEffect.ShowMessage("Không tìm thấy khách sạn phù hợp") }
                        }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, errorMessage = result.throwable.message) }
                        sendEffect { SearchUiEffect.ShowMessage(result.throwable.message ?: "Tìm kiếm thất bại") }
                    }
                }
            }
        }
    }
}


