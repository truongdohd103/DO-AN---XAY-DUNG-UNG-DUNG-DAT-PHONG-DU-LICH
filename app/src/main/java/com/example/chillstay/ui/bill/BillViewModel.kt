package com.example.chillstay.ui.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

class BillViewModel(
    private val getUserBookings: GetUserBookingsUseCase,
    private val getHotelById: GetHotelByIdUseCase
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
                // For now, we'll simulate loading bill details
                // In a real implementation, you'd have a GetBookingByIdUseCase
                _state.update { 
                    it.updateIsLoading(false)
                        .updateError("Bill details loading not implemented yet")
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
