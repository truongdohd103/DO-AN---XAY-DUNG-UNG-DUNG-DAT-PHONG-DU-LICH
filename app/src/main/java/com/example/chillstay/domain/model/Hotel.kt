package com.example.chillstay.domain.model

data class Hotel(
    val id: String = "",
    val name: String = "",
    val country: String = "",
    val city: String = "",
    val rating: Double = 0.0,
    val numberOfReviews: Int = 0,
    val imageUrl: String = "",
    val minPrice: Double? = null,
    val photoCount: Int = 0,
    val detail: HotelDetail? = null,
    val rooms: List<Room> = emptyList()
)


