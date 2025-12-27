package com.example.chillstay.ui.admin.voucher.voucher_apply

import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.Hotel

sealed interface VoucherApplyIntent : UiEvent {
    // Load operations
    data class LoadData(val voucherId: String) : VoucherApplyIntent
    data object LoadAvailableHotels : VoucherApplyIntent

    data object LoadAppliedHotels : VoucherApplyIntent

    // Search operations
    data class SearchQueryChanged(val query: String) : VoucherApplyIntent
    data object PerformSearch : VoucherApplyIntent

    // Filter operations
    data class CountryChanged(val country: String) : VoucherApplyIntent
    data class CityChanged(val city: String) : VoucherApplyIntent
    data object ToggleCountryDropdown : VoucherApplyIntent
    data object ToggleCityDropdown : VoucherApplyIntent

    // Pagination operations
    data class GoToPage(val page: Int) : VoucherApplyIntent
    data object NextPage : VoucherApplyIntent
    data object PreviousPage : VoucherApplyIntent

    // Hotel selection operations
    data class AddHotel(val hotel: Hotel) : VoucherApplyIntent
    data class RemoveHotel(val hotel: Hotel) : VoucherApplyIntent

    // Navigation operations
    data object NavigateBack : VoucherApplyIntent
    data object ConfirmAndApplyVoucher : VoucherApplyIntent

    // Error handling
    data object ClearError : VoucherApplyIntent
}