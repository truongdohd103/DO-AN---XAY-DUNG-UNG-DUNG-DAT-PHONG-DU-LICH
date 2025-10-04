package com.example.chillstay.domain.model

import java.time.Instant

data class Voucher(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val type: String,
    val value: Double,
    val validFrom: Instant,
    val validTo: Instant,
    val status: String,
    val applyForHotel: List<Hotel> = emptyList()
)


