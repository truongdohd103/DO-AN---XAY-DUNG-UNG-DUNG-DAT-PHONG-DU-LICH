package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val userId: String = "",
    val hotelId: String = "",
    val comment: String = "",
    val rating: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)


