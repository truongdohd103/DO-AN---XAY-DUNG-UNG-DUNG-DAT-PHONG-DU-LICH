package com.example.chillstay.domain.model

import java.time.Instant
import java.time.LocalDate

data class Booking(
    val id: String,
    val userId: String,
    val hotelId: String,
    val roomId: String,
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val guests: Int,
    val adults: Int,
    val children: Int,
    val rooms: Int,
    val price: Double,
    val originalPrice: Double,
    val discount: Double,
    val serviceFee: Double,
    val taxes: Double,
    val totalPrice: Double,
    val status: BookingStatus,
    val paymentMethod: PaymentMethod,
    val specialRequests: String = "",
    val preferences: BookingPreferences = BookingPreferences(),
    val createdAt: Instant,
    val updatedAt: Instant,
    val appliedVouchers: List<Voucher> = emptyList(),
    val hotel: Hotel? = null,
    val room: Room? = null
) {
    // Firestore mapping helper
    constructor() : this(
        id = "",
        userId = "",
        hotelId = "",
        roomId = "",
        dateFrom = LocalDate.now(),
        dateTo = LocalDate.now().plusDays(1),
        guests = 1,
        adults = 1,
        children = 0,
        rooms = 1,
        price = 0.0,
        originalPrice = 0.0,
        discount = 0.0,
        serviceFee = 0.0,
        taxes = 0.0,
        totalPrice = 0.0,
        status = BookingStatus.PENDING,
        paymentMethod = PaymentMethod.CREDIT_CARD,
        specialRequests = "",
        preferences = BookingPreferences(),
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
        appliedVouchers = emptyList(),
        hotel = null,
        room = null
    )
}

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


