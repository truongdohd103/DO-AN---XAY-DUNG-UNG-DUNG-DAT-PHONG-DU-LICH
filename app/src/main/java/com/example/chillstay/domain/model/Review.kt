package com.example.chillstay.domain.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val hotelId: String = "",
    val comment: String = "",
    val rating: Int = 0
)


