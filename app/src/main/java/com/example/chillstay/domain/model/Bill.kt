package com.example.chillstay.domain.model

import java.time.Instant

data class Bill(
    val id: String,
    val bookingId: String,
    val amount: Double,
    val paymentMethod: String,
    val status: String,
    val createdAt: Instant,
    val paidAt: Instant?,
    val payments: List<Payment> = emptyList()
)


