package com.example.chillstay.domain.model

import java.time.LocalDate

data class Review(
    val id: String,
    val userId: String,
    val hotelId: String,
    val text: String,
    val rating: Int,
    val created: LocalDate
)


