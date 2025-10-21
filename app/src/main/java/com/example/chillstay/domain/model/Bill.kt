package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Bill(
    val id: String = "",
    val bookingId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val paidAt: Timestamp? = null,
    val payments: List<Payment> = emptyList()
)


