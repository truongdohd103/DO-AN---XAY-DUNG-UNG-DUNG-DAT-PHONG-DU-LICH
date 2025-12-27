package com.example.chillstay.ui.admin.voucher.voucher_apply

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import com.example.chillstay.domain.usecase.voucher.ApplyVoucherToHotelsUseCase
import com.example.chillstay.domain.usecase.voucher.GetApplicableHotelsUseCase
import com.example.chillstay.domain.usecase.voucher.GetVoucherByIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VoucherApplyViewModel(
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val getApplicableHotelsUseCase: GetApplicableHotelsUseCase,
    private val getHotelsUseCase: GetHotelsUseCase,
    private val applyVoucherToHotelsUseCase: ApplyVoucherToHotelsUseCase
) : BaseViewModel<VoucherApplyUiState, VoucherApplyIntent, VoucherApplyEffect>(
    VoucherApplyUiState()
) {

    companion object {
        private const val TAG = "AccommodationSelect"
    }

    val uiState = state
    private var allHotelsCache: List<Hotel> = emptyList()
    private var filterJob: Job? = null
    private var currentVoucherId: String = ""

    override fun onEvent(event: VoucherApplyIntent) {
        Log.d(TAG, "onEvent: $event")
        when (event) {
            is VoucherApplyIntent.LoadData -> {
                loadVoucherAndHotels(event.voucherId)
            }
            is VoucherApplyIntent.LoadAvailableHotels -> {
                loadAvailableHotels()
            }
            is VoucherApplyIntent.LoadAppliedHotels -> {
                loadAppliedHotels()
            }
            is VoucherApplyIntent.SearchQueryChanged -> {
                _state.value = _state.value.updateSearchQuery(event.query)
                applyFiltersDebounced()
            }
            is VoucherApplyIntent.PerformSearch -> {
                applyFiltersAsync()
            }
            is VoucherApplyIntent.CountryChanged -> {
                updateCountryAndCities(event.country)
            }
            is VoucherApplyIntent.CityChanged -> {
                _state.value = _state.value.updateSelectedCity(event.city)
                applyFiltersAsync()
            }
            is VoucherApplyIntent.ToggleCountryDropdown -> {
                _state.value = _state.value.toggleCountryExpanded()
            }
            is VoucherApplyIntent.ToggleCityDropdown -> {
                _state.value = _state.value.toggleCityExpanded()
            }
            is VoucherApplyIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.updateCurrentPage(event.page)
                }
            }
            is VoucherApplyIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.updateCurrentPage(currentPage + 1)
                }
            }
            is VoucherApplyIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.updateCurrentPage(currentPage - 1)
                }
            }
            is VoucherApplyIntent.AddHotel -> {
                addHotelToSelection(event.hotel)
            }
            is VoucherApplyIntent.RemoveHotel -> {
                removeHotelFromSelection(event.hotel)
            }
            is VoucherApplyIntent.NavigateBack -> {
                Log.d(TAG, "Navigate back requested")
                viewModelScope.launch {
                    sendEffect { VoucherApplyEffect.NavigateBack }
                    Log.d(TAG, "NavigateBack effect sent")
                }
            }
            is VoucherApplyIntent.ConfirmAndApplyVoucher -> {
                confirmAndApplyVoucher()
            }
            is VoucherApplyIntent.ClearError -> {
                _state.value = _state.value.clearError()
            }
        }
    }

    private fun loadVoucherAndHotels(voucherId: String) {
        Log.d(TAG, "loadVoucherAndHotels: voucherId=$voucherId")
        currentVoucherId = voucherId
        viewModelScope.launch {
            _state.value = _state.value.updateIsLoading(true).clearError()

            try {
                Log.d(TAG, "Fetching voucher...")
                val voucherResult = getVoucherByIdUseCase(voucherId).first()

                when (voucherResult) {
                    is Result.Success -> {
                        val voucher = voucherResult.data
                        Log.d(TAG, "Voucher loaded: ${voucher.title}, applyForHotel=${voucher.applyForHotel?.size ?: 0} hotels")
                        _state.value = _state.value.updateVoucher(voucher)
                        loadAvailableHotels()
                        loadAppliedHotels()
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to load voucher: ${voucherResult.throwable.message}", voucherResult.throwable)
                        _state.value = _state.value
                            .updateIsLoading(false)
                            .updateError(voucherResult.throwable.message ?: "Failed to load voucher")
                        sendEffect {
                            VoucherApplyEffect.ShowError(
                                voucherResult.throwable.message ?: "Failed to load voucher"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading voucher: ${e.message}", e)
                _state.value = _state.value
                    .updateIsLoading(false)
                    .updateError(e.message ?: "Failed to load voucher")
                sendEffect {
                    VoucherApplyEffect.ShowError(
                        e.message ?: "Failed to load voucher"
                    )
                }
            }
        }
    }

    private fun loadAppliedHotels() {
        Log.d(TAG, "loadAppliedHotels")
        viewModelScope.launch {
            if (!_state.value.isLoading) {
                _state.value = _state.value.updateIsLoading(true)
            }
            _state.value = _state.value.clearError()

            try {
                Log.d(TAG, "Fetching applicable hotels for voucher: $currentVoucherId")
                val result = getApplicableHotelsUseCase(currentVoucherId).first()

                when (result) {
                    is Result.Success -> {
                        val appliedHotels = result.data
                        Log.d(TAG, "Applied hotels loaded: ${appliedHotels.size} total")

                            withContext(Dispatchers.Main) {
                                var totalDiscount = 0.0
                                appliedHotels.forEach { appliedHotel ->
                                    totalDiscount += _state.value.calculateDiscountForHotel(appliedHotel, _state.value.voucher)
                                }
                                _state.value = _state.value.copy(selectedHotels = result.data, totalDiscount = totalDiscount)
                                Log.d(TAG, "Applied hotels loaded successfully, isLoading=${_state.value.isLoading}")
                                applyFiltersAsync()
                            }
                        }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to load applied hotels: ${result.throwable.message}", result.throwable)
                        _state.value = _state.value
                            .updateIsLoading(false)
                            .updateError(result.throwable.message ?: "Failed to load applied hotels")
                        sendEffect {
                            VoucherApplyEffect.ShowError(
                                result.throwable.message ?: "Failed to load applied hotels"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading hotels: ${e.message}", e)
                _state.value = _state.value
                    .updateIsLoading(false)
                    .updateError(e.message ?: "Failed to load hotels")
                sendEffect {
                    VoucherApplyEffect.ShowError(
                        e.message ?: "Failed to load hotels"
                    )
                }
            }
        }
    }

    private fun loadAvailableHotels() {
        Log.d(TAG, "loadAvailableHotels")
        viewModelScope.launch {
            if (!_state.value.isLoading) {
                _state.value = _state.value.updateIsLoading(true)
            }
            _state.value = _state.value.clearError()

            try {
                Log.d(TAG, "Fetching applicable hotels for voucher: $currentVoucherId")
                val result = getHotelsUseCase().first()

                when (result) {
                    is Result.Success -> {
                        val hotels = result.data
                        Log.d(TAG, "Hotels loaded: ${hotels.size} total")

                        val appliedHotelIds = _state.value.appliedHotelIds
                        Log.d(TAG, "Applied hotel IDs: ${appliedHotelIds.size} - $appliedHotelIds")

                        val availableHotels = if (appliedHotelIds.isNotEmpty()) {
                            hotels.filter { it.id !in appliedHotelIds }
                        } else {
                            hotels
                        }

                        Log.d(TAG, "Available hotels after filtering: ${availableHotels.size}")
                        allHotelsCache = availableHotels

                        withContext(Dispatchers.Default) {
                            val countries = availableHotels
                                .map { it.country }
                                .distinct()
                                .sorted()

                            val cities = availableHotels
                                .map { it.city }
                                .distinct()
                                .sorted()

                            withContext(Dispatchers.Main) {
                                _state.value = _state.value
                                    .updateAvailableHotels(availableHotels)
                                    .copy(
                                        isLoading = false,
                                        availableCountries = countries,
                                        availableCities = cities
                                    )

                                Log.d(TAG, "Hotels loaded successfully, isLoading=${_state.value.isLoading}")
                                applyFiltersAsync()
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "Failed to load hotels: ${result.throwable.message}", result.throwable)
                        _state.value = _state.value
                            .updateIsLoading(false)
                            .updateError(result.throwable.message ?: "Failed to load hotels")
                        sendEffect {
                            VoucherApplyEffect.ShowError(
                                result.throwable.message ?: "Failed to load hotels"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading hotels: ${e.message}", e)
                _state.value = _state.value
                    .updateIsLoading(false)
                    .updateError(e.message ?: "Failed to load hotels")
                sendEffect {
                    VoucherApplyEffect.ShowError(
                        e.message ?: "Failed to load hotels"
                    )
                }
            }
        }
    }

    private fun applyFiltersDebounced() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
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

            withContext(Dispatchers.Main) {
                _state.value = _state.value.updateFilteredHotels(filtered)
            }
        }
    }

    private fun addHotelToSelection(hotel: Hotel) {
        Log.d(TAG, "addHotelToSelection: ${hotel.name}")
        _state.value = _state.value.addSelectedHotel(hotel)
        Log.d(TAG, "Selected hotels count: ${_state.value.selectedHotelsCount}")
    }

    private fun removeHotelFromSelection(hotel: Hotel) {
        Log.d(TAG, "removeHotelFromSelection: ${hotel.name}")
        _state.value = _state.value.removeSelectedHotel(hotel)
        Log.d(TAG, "Selected hotels count: ${_state.value.selectedHotelsCount}")
    }

    private fun confirmAndApplyVoucher() {
        viewModelScope.launch {
            val currentState = _state.value

            Log.d(TAG, "=== CONFIRM AND APPLY VOUCHER ===")
            Log.d(TAG, "Voucher ID: $currentVoucherId")
            Log.d(TAG, "Selected hotels count: ${currentState.selectedHotelsCount}")
            Log.d(TAG, "Selected hotel IDs: ${currentState.selectedHotels.map { it.id }}")
            Log.d(TAG, "Can confirm: ${currentState.canConfirm}")

            if (!currentState.canConfirm) {
                Log.w(TAG, "Cannot confirm - validation failed")
                sendEffect {
                    VoucherApplyEffect.ShowError("Please select at least one hotel")
                }
                return@launch
            }

            Log.d(TAG, "Setting isLoading = true")
            _state.value = _state.value.updateIsLoading(true)

            try {
                val hotelIds = currentState.selectedHotels.map { it.id }

                Log.d(TAG, "Calling applyVoucherToHotelsUseCase...")

                val result = applyVoucherToHotelsUseCase(
                    voucherId = currentVoucherId,
                    hotelIds = hotelIds).first()

                Log.d(TAG, "UseCase result received: ${result::class.simpleName}")

                when (result) {
                    is Result.Success -> {
                        Log.d(TAG, "✅ SUCCESS! Voucher applied successfully")
                        Log.d(TAG, "Clearing selected hotels and stopping loading...")

                        _state.value = _state.value.copy(
                            selectedHotels = emptyList(),
                            isLoading = false
                        )

                        Log.d(TAG, "isLoading = ${_state.value.isLoading}")
                        Log.d(TAG, "Sending ShowSuccess effect...")

                        sendEffect {
                            VoucherApplyEffect.ShowSuccess(
                                "Voucher applied successfully to ${currentState.selectedHotelsCount} hotels"
                            )
                        }

                        Log.d(TAG, "ShowSuccess effect sent")
                        Log.d(TAG, "Waiting 300ms before navigation...")
                        delay(300)

                        Log.d(TAG, "Sending NavigateToConfirmation effect...")
                        sendEffect {
                            VoucherApplyEffect.NavigateToConfirmation
                        }
                        Log.d(TAG, "NavigateToConfirmation effect sent")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "❌ ERROR! Failed to apply voucher: ${result.throwable.message}", result.throwable)
                        _state.value = _state.value.updateIsLoading(false)

                        sendEffect {
                            VoucherApplyEffect.ShowError(
                                result.throwable.message ?: "Failed to apply voucher"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ EXCEPTION in confirmAndApplyVoucher: ${e.message}", e)
                e.printStackTrace()

                _state.value = _state.value.updateIsLoading(false)

                sendEffect {
                    VoucherApplyEffect.ShowError(
                        e.message ?: "Failed to apply voucher"
                    )
                }
            }

            Log.d(TAG, "=== END CONFIRM AND APPLY ===")
        }
    }
}