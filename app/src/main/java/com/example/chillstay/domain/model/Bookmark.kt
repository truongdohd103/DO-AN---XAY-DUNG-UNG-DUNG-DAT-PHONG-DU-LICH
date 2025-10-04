package com.example.chillstay.domain.model

import java.time.Instant

data class Bookmark(
    val id: String,
    val userId: String,
    val hotelId: String,
    val createdAt: Instant
)


