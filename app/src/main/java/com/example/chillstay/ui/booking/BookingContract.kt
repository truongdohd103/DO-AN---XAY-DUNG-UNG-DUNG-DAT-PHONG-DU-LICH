package com.example.chillstay.ui.booking

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.BookingPreferences
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.PaymentMethod
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.Voucher
import java.time.LocalDate

data class BookingUiState(
    val isLoading: Boolean = false,
    val isCreatingBooking: Boolean = false,
    val bookingCreated: Boolean = false,
    val error: String? = null,
    
    val hotel: Hotel? = null,
    val room: Room? = null,
    
    val dateFrom: LocalDate = LocalDate.now(),
    val dateTo: LocalDate = LocalDate.now().plusDays(1),
    val datesUserSelected: Boolean = false,
    val hasInitialDates: Boolean = false,
    
    val adults: Int = 1,
    val children: Int = 0,
    val rooms: Int = 1,
    
    val availableVouchers: List<Voucher> = emptyList(),
    val appliedVouchers: List<Voucher> = emptyList(),
    val isApplyingVoucher: Boolean = false,
    val voucherMessage: String? = null,
    val voucherCodeInput: String = "",
    
    val priceBreakdown: PriceBreakdown = PriceBreakdown(),
    
    val preferences: BookingPreferences = BookingPreferences(),
    val specialRequests: String = "",
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD
) : UiState

data class PriceBreakdown(
    val roomPrice: Double = 0.0,
    val serviceFee: Double = 0.0,
    val taxes: Double = 0.0,
    val discount: Double = 0.0,
    val voucherDiscount: Double = 0.0,
    val finalTotal: Double = 0.0
) {
    // Helper for simple total getter if needed
    val total: Double get() = finalTotal
}

sealed class BookingIntent : UiEvent {
    data class LoadBookingData(
        val hotelId: String,
        val roomId: String,
        val dateFrom: LocalDate,
        val dateTo: LocalDate
    ) : BookingIntent()
    
    data class LoadBookingById(val bookingId: String) : BookingIntent()
    
    data class UpdateGuests(
        val adults: Int,
        val children: Int,
        val rooms: Int
    ) : BookingIntent()
    
    data class UpdatePreferences(val preferences: BookingPreferences) : BookingIntent()
    
    data class UpdateSpecialRequests(val specialRequests: String) : BookingIntent()
    
    data class UpdatePaymentMethod(val paymentMethod: PaymentMethod) : BookingIntent()
    
    data class ApplyVoucher(val voucherCode: String) : BookingIntent()
    
    data class RemoveVoucher(val voucherId: String) : BookingIntent()
    
    object CreateBooking : BookingIntent()
    
    object ClearBooking : BookingIntent()
    
    data class UpdateDates(
        val dateFrom: LocalDate,
        val dateTo: LocalDate
    ) : BookingIntent()
    
    data class UpdateVoucherCodeInput(val code: String) : BookingIntent()
}

sealed class BookingEffect : UiEffect {
    data class ShowError(val message: String) : BookingEffect()
    object ShowBookingCreated : BookingEffect()
    data class NavigateToPayment(val bookingId: String) : BookingEffect()
    object NavigateBack : BookingEffect()
    data class NavigateToBookingDetail(val bookingId: String) : BookingEffect()
    object RequireAuthentication : BookingEffect()
}
