package com.example.chillstay.ui.booking

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingPreferences
import com.example.chillstay.domain.model.PaymentMethod
import java.time.LocalDate

sealed class BookingIntent {
    data class LoadBookingData(
        val hotelId: String,
        val roomId: String,
        val dateFrom: LocalDate,
        val dateTo: LocalDate
    ) : BookingIntent()
    
    data class UpdateGuests(
        val adults: Int,
        val children: Int,
        val rooms: Int
    ) : BookingIntent()
    
    data class UpdatePreferences(
        val preferences: BookingPreferences
    ) : BookingIntent()
    
    data class UpdateSpecialRequests(
        val specialRequests: String
    ) : BookingIntent()
    
    data class UpdatePaymentMethod(
        val paymentMethod: PaymentMethod
    ) : BookingIntent()
    
    data class ApplyVoucher(
        val voucherCode: String
    ) : BookingIntent()
    
    data class RemoveVoucher(
        val voucherId: String
    ) : BookingIntent()
    
    object CreateBooking : BookingIntent()
    object ClearBooking : BookingIntent()
}
