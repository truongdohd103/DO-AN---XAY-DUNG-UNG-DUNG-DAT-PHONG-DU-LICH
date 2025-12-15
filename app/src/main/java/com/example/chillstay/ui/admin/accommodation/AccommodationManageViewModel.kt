package com.example.chillstay.ui.admin.accommodation

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelListFilter
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class AccommodationManageViewModel(
    private val getHotelsUseCase: GetHotelsUseCase
) : BaseViewModel<AccommodationManageUiState, AccommodationManageIntent, AccommodationManageEffect>(
    AccommodationManageUiState()
) {

    val uiState = state
    private var allHotelsCache: List<Hotel> = emptyList()
    private var filterJob: Job? = null
    private var lastLoadedOffset = 0
    private val loadBatchSize = 20 // Giảm batch size để load nhanh hơn

    override fun onEvent(event: AccommodationManageIntent) {
        when (event) {
            is AccommodationManageIntent.LoadHotels -> {
                loadHotelsOptimized()
            }
            is AccommodationManageIntent.LoadMoreHotels -> {
                if (_state.value.hasMoreHotels && !_state.value.isLoadingMore) {
                    loadMoreHotels()
                }
            }
            is AccommodationManageIntent.SearchQueryChanged -> {
                _state.value = _state.value.updateSearchQuery(event.query)
                applyFiltersDebounced()
            }
            is AccommodationManageIntent.PerformSearch -> {
                applyFiltersAsync()
            }
            is AccommodationManageIntent.CountryChanged -> {
                updateCountryAndCities(event.country)
            }
            is AccommodationManageIntent.CityChanged -> {
                _state.value = _state.value.updateSelectedCity(event.city)
                applyFiltersAsync()
            }
            is AccommodationManageIntent.ToggleCountryDropdown -> {
                _state.value = _state.value.toggleCountryExpanded()
            }
            is AccommodationManageIntent.ToggleCityDropdown -> {
                _state.value = _state.value.toggleCityExpanded()
            }
            is AccommodationManageIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.updateCurrentPage(event.page)
                }
            }
            is AccommodationManageIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.updateCurrentPage(currentPage + 1)
                }
            }
            is AccommodationManageIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.updateCurrentPage(currentPage - 1)
                }
            }
            is AccommodationManageIntent.CreateNew -> {
                viewModelScope.launch {
                    sendEffect { AccommodationManageEffect.NavigateToCreateNew }
                }
            }
            is AccommodationManageIntent.EditHotel -> {
                viewModelScope.launch {
                    sendEffect { AccommodationManageEffect.NavigateToEdit(event.hotel) }
                }
            }
            is AccommodationManageIntent.InvalidateHotel -> {
                invalidateHotel(event.hotel)
            }
            is AccommodationManageIntent.DeleteHotel -> {
                deleteHotel(event.hotel)
            }
            is AccommodationManageIntent.ClearError -> {
                _state.value = _state.value.clearError()
            }
        }
    }

    /**
     * TỐI ƯU 1: Load hotels nhanh hơn với batch size nhỏ hơn
     * Thay vì load 50 hotels cùng lúc, load 20 hotels và cho phép load more
     */
    private fun loadHotelsOptimized() {
        viewModelScope.launch {
            _state.value = _state.value.updateIsLoading(true).clearError()
            lastLoadedOffset = 0
            allHotelsCache = emptyList()

            try {
                getHotelsUseCase(
                    HotelListFilter(limit = loadBatchSize)
                ).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val newHotels = result.data
                            allHotelsCache = newHotels
                            lastLoadedOffset = newHotels.size

                            // Process data in background thread
                            withContext(Dispatchers.Default) {
                                val countries = newHotels
                                    .map { it.country }
                                    .distinct()
                                    .sorted()

                                val cities = newHotels
                                    .map { it.city }
                                    .distinct()
                                    .sorted()

                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value
                                        .updateAllHotels(newHotels)
                                        .copy(
                                            isLoading = false,
                                            hasMoreHotels = newHotels.size >= loadBatchSize,
                                            availableCountries = countries,
                                            availableCities = cities
                                        )
                                        .clearError()

                                    // Apply initial filters
                                    applyFiltersAsync()
                                }
                            }
                        }
                        is Result.Error -> {
                            _state.value = _state.value
                                .copy(isLoading = false)
                                .updateError(result.throwable.message ?: "Failed to load hotels")
                            sendEffect {
                                AccommodationManageEffect.ShowError(
                                    result.throwable.message ?: "Failed to load hotels"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value
                    .copy(isLoading = false)
                    .updateError(e.message ?: "Failed to load hotels")
            }
        }
    }

    /**
     * Load more hotels when user scrolls or clicks "Load More"
     */
    private fun loadMoreHotels() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMore = true)

            try {
                getHotelsUseCase(
                    HotelListFilter(limit = loadBatchSize)
                ).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val newHotels = result.data

                            // Merge with existing cache, avoid duplicates
                            allHotelsCache = allHotelsCache + newHotels.filter { newHotel ->
                                !allHotelsCache.any { it.id == newHotel.id }
                            }

                            lastLoadedOffset = allHotelsCache.size

                            withContext(Dispatchers.Default) {
                                val countries = allHotelsCache
                                    .map { it.country }
                                    .distinct()
                                    .sorted()

                                val cities = allHotelsCache
                                    .map { it.city }
                                    .distinct()
                                    .sorted()

                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value
                                        .updateAllHotels(allHotelsCache)
                                        .copy(
                                            isLoadingMore = false,
                                            hasMoreHotels = newHotels.size >= loadBatchSize,
                                            availableCountries = countries,
                                            availableCities = cities
                                        )

                                    applyFiltersAsync()
                                }
                            }
                        }
                        is Result.Error -> {
                            _state.value = _state.value.copy(isLoadingMore = false)
                            sendEffect {
                                AccommodationManageEffect.ShowError(
                                    result.throwable.message ?: "Failed to load more hotels"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingMore = false)
            }
        }
    }

    /**
     * TỐI ƯU 2: Debounce search để tránh filter liên tục khi user đang gõ
     */
    private fun applyFiltersDebounced() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300) // Đợi 300ms sau khi user ngừng gõ
            applyFiltersAsync()
        }
    }

    private fun updateCountryAndCities(country: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val updatedCities = if (country.isNotBlank()) {
                allHotelsCache
                    .filter { it.country.equals(country, ignoreCase = true) }
                    .map { it.city }
                    .distinct()
                    .sorted()
            } else {
                allHotelsCache.map { it.city }.distinct().sorted()
            }

            withContext(Dispatchers.Main) {
                _state.value = _state.value.updateSelectedCountry(country).copy(
                    selectedCity = "",
                    availableCities = updatedCities
                )
                applyFiltersAsync()
            }
        }
    }

    /**
     * TỐI ƯU 3: Cập nhật statistics dựa trên filtered hotels
     */
    private fun applyFiltersAsync() {
        viewModelScope.launch(Dispatchers.Default) {
            val searchQuery = _state.value.searchQuery.lowercase()
            val country = _state.value.selectedCountry
            val city = _state.value.selectedCity

            val filtered = allHotelsCache.filter { hotel ->
                val matchesSearch = searchQuery.isBlank() ||
                        hotel.name.lowercase().contains(searchQuery) ||
                        hotel.city.lowercase().contains(searchQuery) ||
                        hotel.country.lowercase().contains(searchQuery) ||
                        hotel.description.lowercase().contains(searchQuery)

                val matchesCountry = country.isBlank() ||
                        hotel.country.equals(country, ignoreCase = true)

                val matchesCity = city.isBlank() ||
                        hotel.city.equals(city, ignoreCase = true)

                matchesSearch && matchesCountry && matchesCity
            }

            // Tính toán statistics dựa trên filtered results
            val totalProperties = filtered.size
            val activeProperties = filtered.count { it.rooms.isNotEmpty() || it.minPrice != null }

            withContext(Dispatchers.Main) {
                _state.value = _state.value.updateFilteredHotels(filtered).copy(
                    totalProperties = totalProperties,
                    activeProperties = activeProperties
                )
            }
        }
    }

    private fun invalidateHotel(hotel: Hotel) {
        // TODO: Implement invalidate hotel logic
        viewModelScope.launch {
            sendEffect { AccommodationManageEffect.ShowInvalidateSuccess(hotel) }
        }
    }

    private fun deleteHotel(hotel: Hotel) {
        // TODO: Implement delete hotel logic
        viewModelScope.launch {
            sendEffect { AccommodationManageEffect.ShowDeleteSuccess(hotel) }
            // Reload hotels after delete
            loadHotelsOptimized()
        }
    }
}