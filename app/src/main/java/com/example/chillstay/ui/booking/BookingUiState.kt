package com.example.chillstay.ui.booking

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingPreferences
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.PaymentMethod
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.Voucher
import java.time.LocalDate

@Immutable
data class BookingUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val hotel: Hotel? = null,
    val room: Room? = null,
    val dateFrom: LocalDate = LocalDate.now(),
    val dateTo: LocalDate = LocalDate.now().plusDays(1),
    val adults: Int = 2,
    val children: Int = 0,
    val rooms: Int = 1,
    val preferences: BookingPreferences = BookingPreferences(),
    val specialRequests: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val availableVouchers: List<Voucher> = emptyList(),
    val appliedVouchers: List<Voucher> = emptyList(),
    val priceBreakdown: PriceBreakdown = PriceBreakdown(),
    val isCreatingBooking: Boolean = false,
    val bookingCreated: Boolean = false,
    val hasInitialDates: Boolean = false,
    val datesUserSelected: Boolean = false
) : UiState

data class PriceBreakdown(
    val roomPrice: Double = 0.0,
    val serviceFee: Double = 0.0,
    val taxes: Double = 0.0,
    val discount: Double = 0.0,
    val voucherDiscount: Double = 0.0,
    val totalPrice: Double = 0.0
) {
    val subtotal: Double
        get() = roomPrice - discount - voucherDiscount
    
    val finalTotal: Double
        get() = subtotal + serviceFee + taxes
}
