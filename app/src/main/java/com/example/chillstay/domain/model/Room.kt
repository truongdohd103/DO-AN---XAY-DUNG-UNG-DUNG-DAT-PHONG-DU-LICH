package com.example.chillstay.domain.model

data class Room(
    val id: String,
    val type: String,
    val price: Double,
    val imageUrl: String,
    val detail: RoomDetail
)


