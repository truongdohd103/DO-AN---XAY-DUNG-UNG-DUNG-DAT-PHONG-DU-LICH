package com.example.chillstay.ui.trip

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.CancelBookingUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import android.util.Log

class MyTripViewModel(
    private val getUserBookings: GetUserBookingsUseCase,
    private val cancelBooking: CancelBookingUseCase,
    private val getHotelById: GetHotelByIdUseCase,
    private val getRoomById: GetRoomByIdUseCase
) : BaseViewModel<MyTripUiState, MyTripIntent, MyTripEffect>(MyTripUiState()) {

    override fun onEvent(event: MyTripIntent) {
        when (event) {
        is MyTripIntent.LoadBookings -> handleLoadBookings(event.userId, event.status)
        is MyTripIntent.ChangeTab -> handleChangeTab(event.tabIndex)
        is MyTripIntent.CancelBooking -> handleCancelBooking(event.bookingId)
        is MyTripIntent.RefreshBookings -> handleRefreshBookings(event.userId, event.status)
        is MyTripIntent.RetryLoad -> handleRetryLoad(event.userId, event.status)
        is MyTripIntent.ShowCancelDialog -> handleShowCancelDialog(event.booking)
        is MyTripIntent.HideCancelDialog -> handleHideCancelDialog()
        is MyTripIntent.LoadHotelDetails -> {
            viewModelScope.launch { handleLoadHotelDetails(event.hotelIds) }
        }
        is MyTripIntent.LoadRoomDetails -> {
            viewModelScope.launch { handleLoadRoomDetails(event.roomIds) }
        }
        }
    }

    private fun handleLoadBookings(userId: String, status: String?) {
        Log.d("MyTripViewModel", "Loading bookings for user: $userId, status: $status")
_state.update { it.updateIsLoading(true).clearError() }
        
        viewModelScope.launch {
            try {
                val result = getUserBookings(userId, status)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("MyTripViewModel", "Found ${result.data.size} bookings")
                        _state.update { it.updateBookings(result.data) }
                        
                        // Load hotel and room details in parallel
                        val hotelIds = result.data.map { it.hotelId }.distinct()
                        val roomIds = result.data.map { it.roomId }.distinct()
                        
                        // Load hotel and room details in parallel
                        val hotelJob = launch { 
                            handleLoadHotelDetails(hotelIds)
                        }
                        val roomJob = launch { 
                            handleLoadRoomDetails(roomIds)
                        }
                        
                        // Wait for both to complete before setting loading to false
                        hotelJob.join()
                        roomJob.join()
                        
                        _state.update { it.updateIsLoading(false) }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("MyTripViewModel", "Error loading bookings: ${result.throwable.message}")
                        _state.update { 
                            it.updateIsLoading(false)
                                .updateError(result.throwable.message ?: "Failed to load bookings")
                        }
                        viewModelScope.launch {
                            sendEffect { MyTripEffect.ShowError(result.throwable.message ?: "Failed to load bookings") }
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e("MyTripViewModel", "Exception loading bookings: ${exception.message}")
                _state.update { 
                    it.updateIsLoading(false)
                        .updateError(exception.message ?: "Unknown error")
                }
                viewModelScope.launch {
                    sendEffect { MyTripEffect.ShowError(exception.message ?: "Failed to load bookings") }
                }
            }
        }
    }

    private fun handleChangeTab(tabIndex: Int) {
        _state.update { it.updateSelectedTab(tabIndex) }
    }

    private fun handleCancelBooking(bookingId: String) {
        Log.d("MyTripViewModel", "Cancelling booking: $bookingId")
        
        viewModelScope.launch {
            try {
                val result = cancelBooking(bookingId)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("MyTripViewModel", "Successfully cancelled booking: $bookingId")
                        
                        // Remove cancelled booking from current list
                        _state.update { currentState ->
                            val updatedBookings = currentState.bookings.filter { it.id != bookingId }
                            currentState.updateBookings(updatedBookings)
                        }
                        
                        viewModelScope.launch {
                            sendEffect { MyTripEffect.ShowBookingCancelled }
                        }
                        
                        // Refresh bookings for current tab
                        val currentState = _state.value
                        val status = when (currentState.selectedTab) {
                            0 -> "PENDING"
                            1 -> "COMPLETED"
                            2 -> "CANCELED"
                            else -> null
                        }
                        
                        if (status != null) {
                            // Get current user ID from Firebase Auth
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            if (currentUserId != null) {
                                handleLoadBookings(currentUserId, status)
                            }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("MyTripViewModel", "Error cancelling booking: ${result.throwable.message}")
                        _state.update { 
                            it.updateError("Failed to cancel booking: ${result.throwable.message}")
                        }
                        viewModelScope.launch {
                            sendEffect { MyTripEffect.ShowError("Failed to cancel booking: ${result.throwable.message}") }
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e("MyTripViewModel", "Exception cancelling booking: ${exception.message}")
                _state.update { 
                    it.updateError("Failed to cancel booking: ${exception.message}")
                }
                viewModelScope.launch {
                    sendEffect { MyTripEffect.ShowError("Failed to cancel booking: ${exception.message}") }
                }
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

    private fun handleShowCancelDialog(booking: Booking) {
        _state.update { 
            it.updateShowCancelDialog(true)
                .updateBookingToCancel(booking)
        }
    }

    private fun handleHideCancelDialog() {
        _state.update { 
            it.updateShowCancelDialog(false)
                .updateBookingToCancel(null)
        }
    }

    private suspend fun handleLoadHotelDetails(hotelIds: List<String>) {
        if (hotelIds.isEmpty()) return
        
        try {
            val hotels = hotelIds.map { hotelId ->
                val result = getHotelById(hotelId).first()
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> hotelId to result.data
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("MyTripViewModel", "Error loading hotel $hotelId: ${result.throwable.message}")
                        null
                    }
                }
            }.filterNotNull()
            
            val hotelMap = hotels.toMap()
            _state.update { currentState ->
                currentState.updateHotelMap(currentState.hotelMap + hotelMap)
            }
            
            Log.d("MyTripViewModel", "Loaded ${hotelMap.size} hotels")
        } catch (exception: Exception) {
            Log.e("MyTripViewModel", "Exception loading hotel details: ${exception.message}")
        }
    }

    private suspend fun handleLoadRoomDetails(roomIds: List<String>) {
        if (roomIds.isEmpty()) return
        
        try {
            val rooms = roomIds.map { roomId ->
                val result = getRoomById(roomId).first()
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> roomId to result.data
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("MyTripViewModel", "Error loading room $roomId: ${result.throwable.message}")
                        null
                    }
                }
            }.filterNotNull()
            
            val roomMap = rooms.toMap()
            _state.update { currentState ->
                currentState.updateRoomMap(currentState.roomMap + roomMap)
            }
            
            Log.d("MyTripViewModel", "Loaded ${roomMap.size} rooms")
        } catch (exception: Exception) {
            Log.e("MyTripViewModel", "Exception loading room details: ${exception.message}")
        }
    }

}