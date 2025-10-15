package com.example.chillstay.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.CancelBookingUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MyTripViewModel(
    private val getUserBookings: GetUserBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val getHotelById: GetHotelByIdUseCase,
    private val getRoomById: GetRoomByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyTripUiState())
    val state: StateFlow<MyTripUiState> = _state.asStateFlow()

    fun handleIntent(intent: MyTripIntent) = when (intent) {
        is MyTripIntent.LoadBookings -> handleLoadBookings(intent.userId, intent.status)
        is MyTripIntent.ChangeTab -> handleChangeTab(intent.tabIndex)
        is MyTripIntent.CancelBooking -> handleCancelBooking(intent.bookingId)
        is MyTripIntent.RefreshBookings -> handleRefreshBookings(intent.userId, intent.status)
        is MyTripIntent.RetryLoad -> handleRetryLoad(intent.userId, intent.status)
    }

    private fun handleChangeTab(tabIndex: Int) {
        _state.update { it.updateSelectedTab(tabIndex) }
        // Load bookings for the new tab will be handled by the UI
    }

    private fun handleLoadBookings(userId: String, status: String?) {
        _state.update { it.updateIsLoading(true).clearError() }
        
        viewModelScope.launch {
            try {
                val result = getUserBookings(userId)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val filteredBookings = if (status != null) {
                            result.data.filter { it.status.name == status }
                        } else {
                            result.data
                        }
                        
                        loadRelatedData(filteredBookings)
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.update { 
                            it.updateIsLoading(false).updateError(result.throwable.message ?: "Failed to load bookings")
                        }
                    }
                }
            } catch (exception: Exception) {
                _state.update { 
                    it.updateIsLoading(false).updateError(exception.message ?: "Unknown error")
                }
            }
        }
    }

    private fun handleCancelBooking(bookingId: String) {
        viewModelScope.launch {
            try {
                val result = cancelBooking(bookingId)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        // Remove booking from current list
                        _state.update { currentState ->
                            val updatedBookings = currentState.bookings.filter { it.id != bookingId }
                            currentState.updateBookings(updatedBookings).updateIsEmpty(updatedBookings.isEmpty())
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        // Handle error if needed
                    }
                }
            } catch (exception: Exception) {
                // Handle exception if needed
            }
        }
    }

    private fun handleRefreshBookings(userId: String, status: String?) {
        handleLoadBookings(userId, status)
    }

    private fun handleRetryLoad(userId: String, status: String?) {
        _state.update { it.clearError() }
        handleLoadBookings(userId, status)
    }

    private suspend fun loadRelatedData(bookings: List<com.example.chillstay.domain.model.Booking>) {
        val roomMap = mutableMapOf<String, com.example.chillstay.domain.model.Room>()
        val hotelMap = mutableMapOf<String, com.example.chillstay.domain.model.Hotel>()
        
        // Load rooms and hotels for each booking
        for (booking in bookings) {
            // Load room
            try {
                val roomResult = getRoomById(booking.roomId)
                when (roomResult) {
                    is com.example.chillstay.core.common.Result.Success -> roomMap[booking.roomId] = roomResult.data
                    is com.example.chillstay.core.common.Result.Error -> {
                        // Log error but continue
                    }
                }
            } catch (exception: Exception) {
                // Log error but continue
            }
            
            // Load hotel - Note: We need to get hotelId from roomId
            // This is a simplified approach, in real app you'd need to get hotel from room
            try {
                // For now, we'll skip loading hotel details as we don't have direct hotelId
                // In a real implementation, you'd need to get hotelId from roomId first
            } catch (exception: Exception) {
                // Log error but continue
            }
        }
        
        _state.update { 
            it.updateBookings(bookings)
                .updateRoomMap(roomMap)
                .updateHotelMap(hotelMap)
                .updateIsLoading(false)
                .updateIsEmpty(bookings.isEmpty())
        }
    }
}
