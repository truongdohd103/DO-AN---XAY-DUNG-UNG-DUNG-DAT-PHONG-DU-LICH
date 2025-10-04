package com.example.chillstay.domain.model

import java.time.Instant
import java.time.LocalDate

data class Booking(
    val id: String,
    val userId: String,
    val roomId: String,
    val dateFrom: LocalDate,
    val dateTo: LocalDate,
    val guests: Int,
    val price: Double,
    val status: String,
    val createdAt: Instant,
    val appliedVouchers: List<Voucher> = emptyList()
)


