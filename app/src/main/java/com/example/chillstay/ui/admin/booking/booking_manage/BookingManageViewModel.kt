package com.example.chillstay.ui.admin.booking.booking_manage

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.BookingSummary
import com.example.chillstay.domain.usecase.booking.GetAllBookingSummariesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookingManageViewModel(
    private val getAllBookingSummariesUseCase: GetAllBookingSummariesUseCase
) : BaseViewModel<BookingManageUiState, BookingManageIntent, BookingManageEffect>(
    BookingManageUiState()
) {

    val uiState = state
    private var allBookingsCache: List<BookingSummary> = emptyList()

    // Filter logic now uses userName, hotelName, roomName
    private var filterJob: Job? = null

    init {
        loadBookings()
    }

    override fun onEvent(event: BookingManageIntent) {
        when (event) {
            is BookingManageIntent.LoadBookings -> {
                loadBookings()
            }

            is BookingManageIntent.SearchQueryChanged -> {
                _state.value = _state.value.updateSearchQuery(event.query)
                applyFiltersDebounced()
            }

            is BookingManageIntent.PerformSearch -> {
                applyFiltersAsync()
            }

            is BookingManageIntent.DateFromChanged -> {
                _state.value = _state.value.updateDateFrom(event.date).closeDatePickers()
                applyFiltersAsync()
            }

            is BookingManageIntent.DateToChanged -> {
                _state.value = _state.value.updateDateTo(event.date).closeDatePickers()
                applyFiltersAsync()
            }

            is BookingManageIntent.ToggleDateFromPicker -> {
                _state.value = _state.value.toggleDateFromPicker()
            }

            is BookingManageIntent.ToggleDateToPicker -> {
                _state.value = _state.value.toggleDateToPicker()
            }

            is BookingManageIntent.ClearDateFilters -> {
                _state.value = _state.value.clearDateFilters()
                applyFiltersAsync()
            }

            is BookingManageIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.updateCurrentPage(event.page)
                }
            }

            is BookingManageIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.updateCurrentPage(currentPage + 1)
                }
            }

            is BookingManageIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.updateCurrentPage(currentPage - 1)
                }
            }

            is BookingManageIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { BookingManageEffect.NavigateBack }
                }
            }

            is BookingManageIntent.ViewBooking -> {
                viewModelScope.launch {
                    sendEffect { BookingManageEffect.NavigateToView(event.bookingId) }
                }
            }

            is BookingManageIntent.ClearError -> {
                _state.value = _state.value.clearError()
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            _state.value = _state.value.updateIsLoading(true).clearError()
            allBookingsCache = emptyList()

            try {
                // Dùng .first() để lấy một lần thay vì collectLatest
                when (val result = getAllBookingSummariesUseCase().first()) {
                    is Result.Success -> {
                        val bookings = result.data

                        // Process data in background thread
                        withContext(Dispatchers.Default) {
                            allBookingsCache = bookings

                            withContext(Dispatchers.Main) {
                                _state.value = _state.value
                                    .updateAllBookings(bookings)
                                    .copy(isLoading = false)
                                    .clearError()

                                // Apply initial filters
                                applyFiltersAsync()
                            }
                        }
                    }

                    is Result.Error -> {
                        _state.value = _state.value
                            .copy(isLoading = false)
                            .updateError(result.throwable.message ?: "Failed to load bookings")
                        sendEffect {
                            BookingManageEffect.ShowError(
                                result.throwable.message ?: "Failed to load bookings"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value
                    .copy(isLoading = false)
                    .updateError(e.message ?: "Failed to load bookings")
            }
        }
    }

    /**
     * Debounce search to avoid filtering continuously while user is typing
     */
    private fun applyFiltersDebounced() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300) // Wait 300ms after user stops typing
            applyFiltersAsync()
        }
    }

    /**
     * Apply filters based on search query and date range
     */
    private fun applyFiltersAsync() {
        viewModelScope.launch(Dispatchers.Default) {
            val searchQuery = _state.value.searchQuery.lowercase()
            val dateFrom = _state.value.dateFrom
            val dateTo = _state.value.dateTo

            val filtered = allBookingsCache.filter { booking ->
                // Search filter (by phone, email, booking ID, guest name, hotel, room)
                val matchesSearch = searchQuery.isBlank() ||
                        booking.id.lowercase().contains(searchQuery) ||
                        booking.hotelName?.lowercase()?.contains(searchQuery) == true ||
                        booking.roomName?.lowercase()?.contains(searchQuery) == true

                // Date filter - filter by booking creation date
                val matchesDate = if (dateFrom != null && dateTo != null) {
                    val bookingDate = booking.createdAt.toDate().time
                    bookingDate >= dateFrom && bookingDate <= dateTo
                } else {
                    true
                }

                matchesSearch && matchesDate
            }

            withContext(Dispatchers.Main) {
                _state.value = _state.value.updateFilteredBookings(filtered)
            }
        }
    }
}