package com.example.chillstay.domain.model

data class HotelDetail(
    val hotel: Hotel,
    val address: Address,
    val description: String,
    val photoUrls: List<String> = emptyList(),
    val hotelInformation: HotelInformation,
    val facilities: List<String> = emptyList(),
    val location: Location,
    val reviews: List<Review> = emptyList()
)


