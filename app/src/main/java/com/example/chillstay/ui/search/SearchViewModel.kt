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
            is SearchUiEvent.SortChanged -> {
                _state.update { it.copy(sortBy = event.value) }
                val current = _state.value.results
                val sorted = when (event.value) {
                    SortOption.RatingDesc -> current.sortedByDescending { it.rating }
                    SortOption.PriceAsc -> current.sortedBy { it.minPrice }
                    SortOption.Relevance -> current
                }
                _state.update { it.copy(results = sorted) }
            }
            is SearchUiEvent.ApplyQuickFilter -> {
                _state.update {
                    it.copy(
                        minRating = event.minRating?.toString() ?: it.minRating,
                        maxPrice = event.maxPrice?.toString() ?: it.maxPrice
                    )
                }
            }
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
        val hasAnyFilter = _state.value.country.isNotBlank() || _state.value.city.isNotBlank() ||
                _state.value.minRating.isNotBlank() || _state.value.maxPrice.isNotBlank()
        if (currentQuery.isBlank() && !hasAnyFilter) {
            viewModelScope.launch {
                sendEffect { SearchUiEffect.ShowMessage("Please enter a keyword or select filters") }
            }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            searchHotelsUseCase(
                query = currentQuery,
                country = _state.value.country.trim().takeIf { it.isNotBlank() },
                city = _state.value.city.trim().takeIf { it.isNotBlank() },
                minRating = _state.value.minRating.toDoubleOrNull(),
                maxPrice = _state.value.maxPrice.toDoubleOrNull()
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val hotels = when (_state.value.sortBy) {
                            SortOption.RatingDesc -> result.data.sortedByDescending { it.rating }
                            SortOption.PriceAsc -> result.data.sortedBy { it.minPrice }
                            SortOption.Relevance -> result.data
                        }
                        val topCities = result.data
                            .groupBy { it.city }
                            .mapValues { (_, list) -> list.sumOf { it.numberOfReviews } }
                            .toList()
                            .sortedByDescending { it.second }
                            .take(5)
                            .map { it.first }

                        _state.update { it.copy(isLoading = false, results = hotels, errorMessage = null, suggestions = topCities) }
                        if (result.data.isEmpty()) {
                            sendEffect { SearchUiEffect.ShowMessage("No matching hotels found") }
                        }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, errorMessage = result.throwable.message) }
                        sendEffect { SearchUiEffect.ShowMessage(result.throwable.message ?: "Search failed") }
                    }
                }
            }
        }
    }
}
