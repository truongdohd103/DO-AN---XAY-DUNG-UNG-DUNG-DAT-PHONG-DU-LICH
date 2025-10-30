package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Payment(
    val id: String = "",
    val billId: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "",
    val paymentDate: Timestamp = Timestamp.now(),
    val gateway: String = "",
    val transactionId: String = ""
)
