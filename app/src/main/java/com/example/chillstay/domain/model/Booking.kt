package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate

data class Booking(
    val id: String = "",
    val userId: String = "",
    val hotelId: String = "",
    val roomId: String = "",
    val dateFrom: LocalDate = LocalDate.now(),
    val dateTo: LocalDate = LocalDate.now().plusDays(1),
    val guests: Int = 1,
    val adults: Int = 1,
    val children: Int = 0,
    val rooms: Int = 1,
    val price: Double = 0.0,
    val originalPrice: Double = 0.0,
    val discount: Double = 0.0,
    val serviceFee: Double = 0.0,
    val taxes: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.CREDIT_CARD,
    val specialRequests: String = "",
    val preferences: BookingPreferences = BookingPreferences(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val appliedVouchers: List<Voucher> = emptyList(),
    val hotel: Hotel? = null,
    val room: Room? = null
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    REFUNDED
}

enum class PaymentMethod {
    CREDIT_CARD,
    DEBIT_CARD,
    DIGITAL_WALLET,
    BANK_TRANSFER,
    CASH
}

data class BookingPreferences(
    val highFloor: Boolean = false,
    val quietRoom: Boolean = false,
    val extraPillows: Boolean = false,
    val airportShuttle: Boolean = false,
    val earlyCheckIn: Boolean = false,
    val lateCheckOut: Boolean = false
)


