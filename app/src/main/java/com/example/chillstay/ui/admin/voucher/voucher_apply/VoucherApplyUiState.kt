package com.example.chillstay.ui.admin.voucher.voucher_apply

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherType

@Immutable
data class VoucherApplyUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Voucher data
    val voucher: Voucher? = null,

    // Hotel data
    val availableHotels: List<Hotel> = emptyList(),
    val filteredHotels: List<Hotel> = emptyList(),
    val selectedHotels: List<Hotel> = emptyList(),

    // Search & Filter
    val searchQuery: String = "",
    val selectedCountry: String = "",
    val selectedCity: String = "",
    val availableCountries: List<String> = emptyList(),
    val availableCities: List<String> = emptyList(),

    // Dropdown states
    val isCountryExpanded: Boolean = false,
    val isCityExpanded: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 6, // 6 hotels per page for horizontal scroll (2 rows of 3)

    // Discount calculation
    val totalDiscount: Double = 0.0,
    val discountPerHotel: Double = 0.0
) : UiState {

    // Computed properties
    val selectedHotelsCount: Int
        get() = selectedHotels.size

    val availableHotelsCount: Int
        get() = availableFilteredHotels.size

    val canConfirm: Boolean
        get() = selectedHotels.isNotEmpty() && voucher != null

    // Pagination computed properties
    val totalPages: Int
        get() = if (availableFilteredHotels.isEmpty()) 1
        else (availableFilteredHotels.size + itemsPerPage - 1) / itemsPerPage

    val paginatedHotels: List<Hotel>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, availableFilteredHotels.size)
            return if (startIndex < availableFilteredHotels.size) {
                availableFilteredHotels.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    val availableFilteredHotels: List<Hotel>
        get() {
            val selectedIds = selectedHotels.map { it.id }.toSet()
            return filteredHotels.filter { it.id !in selectedIds }
        }
    val appliedHotelIds: List<String>
        get() = voucher?.applyForHotel ?: emptyList()

    val appliedHotelsCount: Int
        get() = appliedHotelIds.size

    // Helper function
    fun isHotelAlreadyApplied(hotel: Hotel): Boolean {
        return appliedHotelIds.contains(hotel.id)
    }

    // Helper functions
    fun isHotelSelected(hotel: Hotel): Boolean {
        return selectedHotels.any { it.id == hotel.id }
    }

    // Update functions - Search & Filter
    fun updateSearchQuery(query: String) = copy(
        searchQuery = query,
        currentPage = 1 // Reset to first page when searching
    )

    fun updateSelectedCountry(country: String) = copy(
        selectedCountry = country,
        currentPage = 1 // Reset to first page when filtering
    )

    fun updateSelectedCity(city: String) = copy(
        selectedCity = city,
        currentPage = 1 // Reset to first page when filtering
    )

    // Update functions - Data
    fun updateAvailableHotels(hotels: List<Hotel>) = copy(availableHotels = hotels)

    fun updateFilteredHotels(hotels: List<Hotel>) = copy(
        filteredHotels = hotels,
        currentPage = 1 // Reset to first page when filter changes
    )

    fun updateVoucher(voucher: Voucher) = copy(
        voucher = voucher,
        discountPerHotel = when (voucher.type) {
            VoucherType.FIXED_AMOUNT -> voucher.value
            VoucherType.PERCENTAGE -> 0.0 // Will be calculated per hotel based on room price
        }
    )

    fun addSelectedHotel(hotel: Hotel): VoucherApplyUiState {
        if (isHotelSelected(hotel)) return this
        val newSelected = selectedHotels + hotel
        return copy(
            selectedHotels = newSelected,
            totalDiscount = calculateTotalDiscount(newSelected, voucher)
        )
    }

    fun removeSelectedHotel(hotel: Hotel): VoucherApplyUiState {
        val newSelected = selectedHotels.filter { it.id != hotel.id }
        return copy(
            selectedHotels = newSelected,
            totalDiscount = calculateTotalDiscount(newSelected, voucher)
        )
    }

    // Update functions - UI State
    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun clearError() = copy(error = null)

    fun updateError(error: String?) = copy(error = error)

    // Pagination functions
    fun updateCurrentPage(page: Int) = copy(currentPage = page)

    // Dropdown functions
    fun toggleCountryExpanded() = copy(
        isCountryExpanded = !isCountryExpanded,
        isCityExpanded = false
    )

    fun toggleCityExpanded() = copy(
        isCityExpanded = !isCityExpanded,
        isCountryExpanded = false
    )

    fun setCountryExpanded(expanded: Boolean) = copy(isCountryExpanded = expanded)

    fun setCityExpanded(expanded: Boolean) = copy(isCityExpanded = expanded)

    // Price calculation helpers
    fun calculateDiscountForHotel(hotel: Hotel, voucher: Voucher?): Double {
        if (voucher == null) return 0.0

        val hotelPrice = hotel.minPrice ?: 0.0

        return when (voucher.type) {
            VoucherType.FIXED_AMOUNT -> {
                val discount = voucher.value
                // Apply max discount limit if specified
                if (voucher.maxDiscountAmount > 0) {
                    minOf(discount, voucher.maxDiscountAmount)
                } else {
                    discount
                }
            }
            VoucherType.PERCENTAGE -> {
                val discount = hotelPrice * (voucher.value / 100.0)
                // Apply max discount limit if specified
                if (voucher.maxDiscountAmount > 0) {
                    minOf(discount, voucher.maxDiscountAmount)
                } else {
                    discount
                }
            }
        }
    }

    fun calculatePriceAfterDiscount(hotel: Hotel, voucher: Voucher?): Double {
        val originalPrice = hotel.minPrice ?: 0.0
        val discount = calculateDiscountForHotel(hotel, voucher)
        return maxOf(0.0, originalPrice - discount)
    }

    // Private helper
    private fun calculateTotalDiscount(hotels: List<Hotel>, voucher: Voucher?): Double {
        if (voucher == null) return 0.0

        return hotels.sumOf { hotel ->
            calculateDiscountForHotel(hotel, voucher)
        }
    }

}