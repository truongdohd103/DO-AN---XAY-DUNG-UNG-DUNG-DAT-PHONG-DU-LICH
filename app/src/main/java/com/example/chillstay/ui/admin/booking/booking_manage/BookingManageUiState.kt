package com.example.chillstay.ui.admin.booking.booking_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.BookingSummary

@Immutable
data class BookingManageUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Data
    val allBookings: List<BookingSummary> = emptyList(),
    val bookings: List<BookingSummary> = emptyList(),
    // Search & Filter
    val searchQuery: String = "",
    val dateFrom: Long? = null,
    val dateTo: Long? = null,

    // Date picker states
    val isDateFromPickerOpen: Boolean = false,
    val isDateToPickerOpen: Boolean = false,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 10
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (bookings.isEmpty()) 1 else (bookings.size + itemsPerPage - 1) / itemsPerPage

    val paginatedBookings: List<BookingSummary>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, bookings.size)
            return if (startIndex < bookings.size) {
                bookings.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Update functions - Search & Filter
    fun updateSearchQuery(query: String) = copy(
        searchQuery = query,
        currentPage = 1
    )

    fun updateDateFrom(date: Long?) = copy(
        dateFrom = date,
        currentPage = 1
    )

    fun updateDateTo(date: Long?) = copy(
        dateTo = date,
        currentPage = 1
    )

    fun clearDateFilters() = copy(
        dateFrom = null,
        dateTo = null,
        currentPage = 1
    )

    // Update functions - Data
    fun updateAllBookings(bookings: List<BookingSummary>) = copy(
        allBookings = bookings
    )

    fun updateFilteredBookings(bookings: List<BookingSummary>) = copy(
        bookings = bookings,
        currentPage = 1
    )

    // Update functions - UI State
    fun updateCurrentPage(page: Int) = copy(currentPage = page)

    fun updateIsLoading(isLoading: Boolean) = copy(isLoading = isLoading)

    fun clearError() = copy(error = null)

    fun updateError(error: String?) = copy(error = error)

    // Date picker functions
    fun toggleDateFromPicker() = copy(
        isDateFromPickerOpen = !isDateFromPickerOpen,
        isDateToPickerOpen = false
    )

    fun toggleDateToPicker() = copy(
        isDateToPickerOpen = !isDateToPickerOpen,
        isDateFromPickerOpen = false
    )

    fun closeDatePickers() = copy(
        isDateFromPickerOpen = false,
        isDateToPickerOpen = false
    )
}