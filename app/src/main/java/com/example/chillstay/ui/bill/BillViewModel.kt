package com.example.chillstay.ui.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.room.GetRoomByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BillViewModel(
    private val getBookingById: GetBookingByIdUseCase,
    private val getHotelById: GetHotelByIdUseCase,
    private val getRoomById: GetRoomByIdUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BillUiState())
    val state: StateFlow<BillUiState> = _state.asStateFlow()

    fun handleIntent(intent: BillIntent) = when (intent) {
        is BillIntent.LoadBillDetails -> handleLoadBillDetails(intent.bookingId)
        is BillIntent.RetryLoad -> handleRetryLoad()
        is BillIntent.DownloadBill -> handleDownloadBill()
        is BillIntent.ShareBill -> handleShareBill()
    }

    private fun handleLoadBillDetails(bookingId: String) {
        Log.d("BillViewModel", "Loading bill details for: $bookingId")
        _state.update { 
            it.updateBookingId(bookingId)
                .updateIsLoading(true)
                .clearError()
        }
        
        viewModelScope.launch {
            try {
                when (val bookingResult = getBookingById(bookingId).first()) {
                    is Result.Success -> {
                        val booking = bookingResult.data
                        if (booking != null) {
                            // Load hotel
                            val hotelResult = getHotelById(booking.hotelId).first()
                            val hotel = if (hotelResult is Result.Success) hotelResult.data else null
                            
                            // Load room
                            val roomResult = getRoomById(booking.roomId).first()
                            val room = if (roomResult is Result.Success) roomResult.data else null
                            
                            _state.update { 
                                it.updateBooking(booking)
                                  .updateHotel(hotel)
                                  .updateRoom(room)
                                  .updateIsLoading(false)
                            }
                        } else {
                            _state.update { 
                                it.updateIsLoading(false)
                                  .updateError("Booking not found")
                            }
                        }
                    }
                    is Result.Error -> {
                         _state.update { 
                            it.updateIsLoading(false)
                              .updateError(bookingResult.throwable.message ?: "Failed to load booking")
                        }
                    }
                }
            } catch (exception: Exception) {
                Log.e("BillViewModel", "Exception loading bill details: ${exception.message}")
                _state.update { 
                    it.updateIsLoading(false)
                        .updateError(exception.message ?: "Unknown error")
                }
            }
        }
    }

    private fun handleRetryLoad() {
        val currentBookingId = _state.value.bookingId
        if (currentBookingId.isNotEmpty()) {
            handleLoadBillDetails(currentBookingId)
        }
    }

    private fun handleDownloadBill() {
        Log.d("BillViewModel", "Downloading bill")
        _state.update { it.updateIsDownloading(true) }
        
        viewModelScope.launch {
            try {
                // Simulate download
                kotlinx.coroutines.delay(2000)
                
                _state.update { it.updateIsDownloading(false) }
                Log.d("BillViewModel", "Bill downloaded successfully")
            } catch (exception: Exception) {
                Log.e("BillViewModel", "Exception downloading bill: ${exception.message}")
                _state.update { 
                    it.updateIsDownloading(false)
                        .updateError(exception.message ?: "Failed to download bill")
                }
            }
        }
    }

    private fun handleShareBill() {
        Log.d("BillViewModel", "Sharing bill")
        _state.update { it.updateIsSharing(true) }
        
        viewModelScope.launch {
            try {
                // Simulate share
                kotlinx.coroutines.delay(1000)
                
                _state.update { it.updateIsSharing(false) }
                Log.d("BillViewModel", "Bill shared successfully")
            } catch (exception: Exception) {
                Log.e("BillViewModel", "Exception sharing bill: ${exception.message}")
                _state.update { 
                    it.updateIsSharing(false)
                        .updateError(exception.message ?: "Failed to share bill")
                }
            }
        }
    }
}
