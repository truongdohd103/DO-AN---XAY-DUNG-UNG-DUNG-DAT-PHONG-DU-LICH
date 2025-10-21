package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Payment(
    val id: String = "",
    val billId: String = "",
    val paymentDate: Timestamp = Timestamp.now(),
    val gateway: String = ""
)


