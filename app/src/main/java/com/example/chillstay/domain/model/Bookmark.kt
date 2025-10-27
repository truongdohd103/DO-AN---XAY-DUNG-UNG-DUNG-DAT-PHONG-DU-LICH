package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Bookmark(
    val id: String = "",
    val userId: String = "",
    val hotelId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)


