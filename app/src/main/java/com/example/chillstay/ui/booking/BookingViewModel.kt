package com.example.chillstay.ui.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingPreferences
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.PaymentMethod
import com.example.chillstay.domain.usecase.booking.CreateBookingUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.time.LocalDate

class BookingViewModel(
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase,
    private val createBookingUseCase: CreateBookingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: BookingIntent) {
        when (intent) {
            is BookingIntent.LoadBookingData -> {
                loadBookingData(intent.hotelId, intent.roomId, intent.dateFrom, intent.dateTo)
            }
            is BookingIntent.UpdateGuests -> {
                updateGuests(intent.adults, intent.children, intent.rooms)
            }
            is BookingIntent.UpdatePreferences -> {
                updatePreferences(intent.preferences)
            }
            is BookingIntent.UpdateSpecialRequests -> {
                updateSpecialRequests(intent.specialRequests)
            }
            is BookingIntent.UpdatePaymentMethod -> {
                updatePaymentMethod(intent.paymentMethod)
            }
            is BookingIntent.ApplyVoucher -> {
                applyVoucher(intent.voucherCode)
            }
            is BookingIntent.RemoveVoucher -> {
                removeVoucher(intent.voucherId)
            }
            is BookingIntent.CreateBooking -> {
                createBooking()
            }
            is BookingIntent.ClearBooking -> {
                clearBooking()
            }
        }
    }

    private fun loadBookingData(hotelId: String, roomId: String, dateFrom: LocalDate, dateTo: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Load hotel data
                val hotelResult = getHotelByIdUseCase(hotelId)
                val hotel = when (hotelResult) {
                    is com.example.chillstay.core.common.Result.Success -> hotelResult.data
                    is com.example.chillstay.core.common.Result.Error -> null
                }
                
                // Load room data
                val roomResult = getRoomByIdUseCase(roomId)
                val room = when (roomResult) {
                    is com.example.chillstay.core.common.Result.Success -> roomResult.data
                    is com.example.chillstay.core.common.Result.Error -> null
                }
                
                // Load available vouchers
                val vouchersResult = getAvailableVouchersUseCase()
                val vouchers = when (vouchersResult) {
                    is com.example.chillstay.core.common.Result.Success -> vouchersResult.data
                    is com.example.chillstay.core.common.Result.Error -> emptyList()
                }
                
                // Calculate price breakdown
                val priceBreakdown = calculatePriceBreakdown(room, dateFrom, dateTo)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hotel = hotel,
                    room = room,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    availableVouchers = vouchers,
                    priceBreakdown = priceBreakdown
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun updateGuests(adults: Int, children: Int, rooms: Int) {
        val currentState = _uiState.value
        
        _uiState.value = currentState.copy(
            adults = adults,
            children = children,
            rooms = rooms,
            priceBreakdown = calculatePriceBreakdown(
                currentState.room,
                currentState.dateFrom,
                currentState.dateTo,
                rooms
            )
        )
    }

    private fun updatePreferences(preferences: BookingPreferences) {
        _uiState.value = _uiState.value.copy(preferences = preferences)
    }

    private fun updateSpecialRequests(specialRequests: String) {
        _uiState.value = _uiState.value.copy(specialRequests = specialRequests)
    }

    private fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        _uiState.value = _uiState.value.copy(paymentMethod = paymentMethod)
    }

    private fun applyVoucher(voucherCode: String) {
        val currentState = _uiState.value
        val voucher = currentState.availableVouchers.find { it.code == voucherCode }
        
        if (voucher != null && !currentState.appliedVouchers.any { it.id == voucher.id }) {
            val updatedAppliedVouchers = currentState.appliedVouchers + voucher
            val voucherDiscount = calculateVoucherDiscount(voucher, currentState.priceBreakdown.roomPrice)
            
            _uiState.value = currentState.copy(
                appliedVouchers = updatedAppliedVouchers,
                priceBreakdown = currentState.priceBreakdown.copy(
                    voucherDiscount = voucherDiscount
                )
            )
        }
    }

    private fun removeVoucher(voucherId: String) {
        val currentState = _uiState.value
        val updatedAppliedVouchers = currentState.appliedVouchers.filter { it.id != voucherId }
        val voucherDiscount = updatedAppliedVouchers.sumOf { 
            calculateVoucherDiscount(it, currentState.priceBreakdown.roomPrice) 
        }
        
        _uiState.value = currentState.copy(
            appliedVouchers = updatedAppliedVouchers,
            priceBreakdown = currentState.priceBreakdown.copy(
                voucherDiscount = voucherDiscount
            )
        )
    }

    private fun createBooking() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.hotel == null || currentState.room == null) {
                _uiState.value = currentState.copy(error = "Missing hotel or room information")
                return@launch
            }
            
            _uiState.value = currentState.copy(isCreatingBooking = true, error = null)
            
            try {
                val booking = Booking(
                    id = "", // Will be generated by Firestore
                    userId = (try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }) ?: "",
                    hotelId = currentState.hotel.id,
                    roomId = currentState.room.id,
                    dateFrom = currentState.dateFrom,
                    dateTo = currentState.dateTo,
                    guests = currentState.adults + currentState.children,
                    adults = currentState.adults,
                    children = currentState.children,
                    rooms = currentState.rooms,
                    price = currentState.priceBreakdown.roomPrice,
                    originalPrice = currentState.priceBreakdown.roomPrice,
                    discount = currentState.priceBreakdown.discount,
                    serviceFee = currentState.priceBreakdown.serviceFee,
                    taxes = currentState.priceBreakdown.taxes,
                    totalPrice = currentState.priceBreakdown.finalTotal,
                    status = BookingStatus.PENDING,
                    paymentMethod = currentState.paymentMethod,
                    specialRequests = currentState.specialRequests,
                    preferences = currentState.preferences,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    appliedVouchers = currentState.appliedVouchers,
                    hotel = currentState.hotel,
                    room = currentState.room
                )
                
                val result = createBookingUseCase(booking)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        _uiState.value = currentState.copy(
                            isCreatingBooking = false,
                            bookingCreated = true
                        )
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _uiState.value = currentState.copy(
                            isCreatingBooking = false,
                            error = result.throwable.message ?: "Failed to create booking"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isCreatingBooking = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun clearBooking() {
        _uiState.value = BookingUiState()
    }

    private fun calculatePriceBreakdown(
        room: com.example.chillstay.domain.model.Room?,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        rooms: Int = 1
    ): PriceBreakdown {
        if (room == null) return PriceBreakdown()
        
        val nights = java.time.temporal.ChronoUnit.DAYS.between(dateFrom, dateTo).toInt()
        val roomPrice = room.price * nights * rooms
        val serviceFee = roomPrice * 0.05 // 5% service fee
        val taxes = roomPrice * 0.1 // 10% taxes
        val discount = 0.0 // TODO: Calculate based on promotions
        
        return PriceBreakdown(
            roomPrice = roomPrice,
            serviceFee = serviceFee,
            taxes = taxes,
            discount = discount,
            voucherDiscount = 0.0,
            totalPrice = roomPrice + serviceFee + taxes - discount
        )
    }

    private fun calculateVoucherDiscount(voucher: com.example.chillstay.domain.model.Voucher, roomPrice: Double): Double {
        return when (voucher.type) {
            com.example.chillstay.domain.model.VoucherType.PERCENTAGE -> roomPrice * (voucher.value / 100)
            com.example.chillstay.domain.model.VoucherType.FIXED_AMOUNT -> voucher.value
        }
    }
}
