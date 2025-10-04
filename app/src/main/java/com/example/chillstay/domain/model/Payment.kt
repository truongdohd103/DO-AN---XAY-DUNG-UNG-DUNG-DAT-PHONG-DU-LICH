package com.example.chillstay.domain.model

import java.time.Instant

data class Payment(
    val id: String,
    val billId: String,
    val paymentDate: Instant,
    val gateway: String
)


