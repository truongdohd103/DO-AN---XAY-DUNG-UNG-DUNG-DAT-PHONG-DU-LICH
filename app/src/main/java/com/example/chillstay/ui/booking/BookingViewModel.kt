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
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import java.time.LocalDate

class BookingViewModel(
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getRoomByIdUseCase: GetRoomByIdUseCase,
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val getBookingByIdUseCase: GetBookingByIdUseCase
) : BaseViewModel<BookingUiState, BookingIntent, BookingEffect>(BookingUiState()) {

    override fun onEvent(event: BookingIntent) {
        when (event) {
            is BookingIntent.LoadBookingData -> {
                loadBookingData(event.hotelId, event.roomId, event.dateFrom, event.dateTo)
            }
            is BookingIntent.LoadBookingById -> {
                loadBookingById(event.bookingId)
            }
            is BookingIntent.UpdateGuests -> {
                updateGuests(event.adults, event.children, event.rooms)
            }
            is BookingIntent.UpdatePreferences -> {
                updatePreferences(event.preferences)
            }
            is BookingIntent.UpdateSpecialRequests -> {
                updateSpecialRequests(event.specialRequests)
            }
            is BookingIntent.UpdatePaymentMethod -> {
                updatePaymentMethod(event.paymentMethod)
            }
            is BookingIntent.ApplyVoucher -> {
                applyVoucher(event.voucherCode)
            }
            is BookingIntent.RemoveVoucher -> {
                removeVoucher(event.voucherId)
            }
            is BookingIntent.CreateBooking -> {
                createBooking()
            }
            is BookingIntent.ClearBooking -> {
                clearBooking()
            }
            is BookingIntent.UpdateDates -> {
                updateDates(event.dateFrom, event.dateTo)
            }
        }
    }

    private fun loadBookingData(hotelId: String, roomId: String, dateFrom: LocalDate, dateTo: LocalDate) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Load hotel data
                val hotelResult = getHotelByIdUseCase(hotelId).first()
                val hotel = when (hotelResult) {
                    is com.example.chillstay.core.common.Result.Success -> hotelResult.data
                    is com.example.chillstay.core.common.Result.Error -> null
                }
                
                // Load room data
                val roomResult = getRoomByIdUseCase(roomId).first()
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
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    hotel = hotel,
                    room = room,
                    dateFrom = dateFrom,
                    dateTo = dateTo,
                    availableVouchers = vouchers,
                    priceBreakdown = priceBreakdown,
                    hasInitialDates = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun loadBookingById(bookingId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                // Get booking from database
                val bookingResult = getBookingByIdUseCase(bookingId)
                when (bookingResult) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val booking = bookingResult.data
                        if (booking != null) {
                            // Load hotel and room data
                            val hotelResult = getHotelByIdUseCase(booking.hotelId).first()
                            val roomResult = getRoomByIdUseCase(booking.roomId).first()
                            
                            val hotel = when (hotelResult) {
                                is com.example.chillstay.core.common.Result.Success -> hotelResult.data
                                is com.example.chillstay.core.common.Result.Error -> null
                            }
                            
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
                            val dateFrom = java.time.LocalDate.parse(booking.dateFrom)
                            val dateTo = java.time.LocalDate.parse(booking.dateTo)
                            val priceBreakdown = calculatePriceBreakdown(room, dateFrom, dateTo)
                            
                            _state.value = _state.value.copy(
                                isLoading = false,
                                hotel = hotel,
                                room = room,
                                dateFrom = dateFrom,
                                dateTo = dateTo,
                                availableVouchers = vouchers,
                                priceBreakdown = priceBreakdown,
                                hasInitialDates = true
                            )
                        } else {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "Booking not found"
                            )
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = bookingResult.throwable.message ?: "Failed to load booking"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun updateGuests(adults: Int, children: Int, rooms: Int) {
        val currentState = _state.value
        
        _state.value = currentState.copy(
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
        _state.value = _state.value.copy(preferences = preferences)
    }

    private fun updateSpecialRequests(specialRequests: String) {
        _state.value = _state.value.copy(specialRequests = specialRequests)
    }

    private fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        _state.value = _state.value.copy(paymentMethod = paymentMethod)
    }

    private fun updateDates(dateFrom: LocalDate, dateTo: LocalDate) {
        val safeTo = if (dateTo.isAfter(dateFrom)) dateTo else dateFrom.plusDays(1)
        val currentState = _state.value
        _state.value = currentState.copy(
            dateFrom = dateFrom,
            dateTo = safeTo,
            priceBreakdown = calculatePriceBreakdown(
                currentState.room,
                dateFrom,
                safeTo,
                currentState.rooms
            ),
            datesUserSelected = true
        )
    }

    private fun applyVoucher(voucherCode: String) {
        val currentState = _state.value
        val voucher = currentState.availableVouchers.find { it.code == voucherCode }
        
        if (voucher != null && !currentState.appliedVouchers.any { it.id == voucher.id }) {
            val updatedAppliedVouchers = currentState.appliedVouchers + voucher
            val voucherDiscount = calculateVoucherDiscount(voucher, currentState.priceBreakdown.roomPrice)
            
            _state.value = currentState.copy(
                appliedVouchers = updatedAppliedVouchers,
                priceBreakdown = currentState.priceBreakdown.copy(
                    voucherDiscount = voucherDiscount
                )
            )
        }
    }

    private fun removeVoucher(voucherId: String) {
        val currentState = _state.value
        val updatedAppliedVouchers = currentState.appliedVouchers.filter { it.id != voucherId }
        val voucherDiscount = updatedAppliedVouchers.sumOf { 
            calculateVoucherDiscount(it, currentState.priceBreakdown.roomPrice) 
        }
        
        _state.value = currentState.copy(
            appliedVouchers = updatedAppliedVouchers,
            priceBreakdown = currentState.priceBreakdown.copy(
                voucherDiscount = voucherDiscount
            )
        )
    }

    private fun createBooking() {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.hotel == null || currentState.room == null) {
                _state.value = currentState.copy(error = "Missing hotel or room information")
                viewModelScope.launch {
                    sendEffect { BookingEffect.ShowError("Missing hotel or room information") }
                }
                return@launch
            }
            
            _state.value = currentState.copy(isCreatingBooking = true, error = null)
            
            try {
                val booking = Booking(
                    id = "", // Will be generated by Firestore
                    userId = (try { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }) ?: "",
                    hotelId = currentState.hotel.id,
                    roomId = currentState.room.id,
                    dateFrom = currentState.dateFrom.toString(),
                    dateTo = currentState.dateTo.toString(),
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
                    status = BookingStatus.COMPLETED,
                    paymentMethod = currentState.paymentMethod,
                    specialRequests = currentState.specialRequests,
                    preferences = currentState.preferences,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now(),
                    appliedVouchers = currentState.appliedVouchers.map { it.id },
                    hotel = currentState.hotel,
                    room = currentState.room
                )
                
                val result = createBookingUseCase(booking)
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        _state.value = currentState.copy(
                            isCreatingBooking = false,
                            bookingCreated = true
                        )
                        viewModelScope.launch {
                            sendEffect { BookingEffect.ShowBookingCreated }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        _state.value = currentState.copy(
                            isCreatingBooking = false,
                            error = result.throwable.message ?: "Failed to create booking"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isCreatingBooking = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun clearBooking() {
        _state.value = BookingUiState()
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
