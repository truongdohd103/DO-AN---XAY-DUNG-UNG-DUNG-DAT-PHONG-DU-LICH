package com.example.chillstay.ui.admin.accommodation.accommodation_manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccommodationManageViewModel(
    private val getHotelsUseCase: GetHotelsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccommodationManageUiState())
    val uiState = _uiState.asStateFlow()

    private val _effect = Channel<AccommodationManageEffect>()
    val effect = _effect.receiveAsFlow()

    private var allHotels: List<Hotel> = emptyList()

    init {
        loadHotels()
    }

    fun onEvent(event: AccommodationManageIntent) {
        when (event) {
            AccommodationManageIntent.LoadHotels -> loadHotels()
            is AccommodationManageIntent.SearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                filterHotels()
            }
            AccommodationManageIntent.PerformSearch -> filterHotels()
            is AccommodationManageIntent.CountryChanged -> {
                _uiState.update { it.copy(selectedCountry = event.country) }
                filterHotels()
            }
            is AccommodationManageIntent.CityChanged -> {
                _uiState.update { it.copy(selectedCity = event.city) }
                filterHotels()
            }
            AccommodationManageIntent.ToggleCountryDropdown -> {
                _uiState.update { it.copy(isCountryExpanded = !it.isCountryExpanded) }
            }
            AccommodationManageIntent.ToggleCityDropdown -> {
                _uiState.update { it.copy(isCityExpanded = !it.isCityExpanded) }
            }
            is AccommodationManageIntent.EditHotel -> sendEffect(AccommodationManageEffect.NavigateToEdit(event.hotel))
            is AccommodationManageIntent.InvalidateHotel -> {
                // TODO: Implement invalidate logic
                sendEffect(AccommodationManageEffect.ShowInvalidateSuccess(event.hotel))
            }
            is AccommodationManageIntent.DeleteHotel -> {
                // TODO: Implement delete logic
                sendEffect(AccommodationManageEffect.ShowDeleteSuccess(event.hotel))
            }
            is AccommodationManageIntent.GoToPage -> {
                _uiState.update { it.copy(currentPage = event.page) }
                updatePagination()
            }
            AccommodationManageIntent.PreviousPage -> {
                val current = _uiState.value.currentPage
                if (current > 1) {
                    _uiState.update { it.copy(currentPage = current - 1) }
                    updatePagination()
                }
            }
            AccommodationManageIntent.NextPage -> {
                val current = _uiState.value.currentPage
                val total = _uiState.value.totalPages
                if (current < total) {
                    _uiState.update { it.copy(currentPage = current + 1) }
                    updatePagination()
                }
            }
        }
    }

    private fun loadHotels() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Assuming GetHotelsUseCase returns Flow<List<Hotel>> or List<Hotel>
                // Adjust based on actual implementation. Using a dummy fetch for now if use case signature is different
                getHotelsUseCase().collect { result ->
                     if (result is Result.Success) {
                         val hotels = result.data
                         allHotels = hotels
                         val countries = hotels.map { it.country }.distinct().sorted()
                         val cities = hotels.map { it.city }.distinct().sorted()
                         
                         _uiState.update { 
                             it.copy(
                                 isLoading = false,
                                 hotels = hotels,
                                 availableCountries = countries,
                                 availableCities = cities,
                                 totalProperties = hotels.size,
                                 activeProperties = hotels.size // Placeholder
                             ) 
                         }
                         filterHotels()
                     } else if (result is Result.Error) {
                         _uiState.update { it.copy(isLoading = false, error = result.throwable.message) }
                     }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun filterHotels() {
        val state = _uiState.value
        val filtered = allHotels.filter { hotel ->
            (state.searchQuery.isEmpty() || hotel.name.contains(state.searchQuery, ignoreCase = true)) &&
            (state.selectedCountry.isEmpty() || hotel.country.equals(state.selectedCountry, ignoreCase = true)) &&
            (state.selectedCity.isEmpty() || hotel.city.equals(state.selectedCity, ignoreCase = true))
        }
        
        _uiState.update { 
            it.copy(
                hotels = filtered,
                totalProperties = filtered.size,
                totalPages = (filtered.size + 9) / 10,
                currentPage = 1
            ) 
        }
        updatePagination()
    }

    private fun updatePagination() {
        val state = _uiState.value
        val start = (state.currentPage - 1) * 10
        val end = minOf(start + 10, state.hotels.size)
        if (start <= state.hotels.size) {
            _uiState.update { it.copy(paginatedHotels = state.hotels.subList(start, end)) }
        }
    }

    private fun sendEffect(effect: AccommodationManageEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}
