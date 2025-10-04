package com.example.chillstay.domain.model

data class Hotel(
    val id: String,
    val name: String,
    val country: String,
    val city: String,
    val rating: Double,
    val numberOfReviews: Int,
    val imageUrl: String,
    val detail: HotelDetail,
    val rooms: List<Room> = emptyList()
)


