package com.example.chillstay.domain.model

data class HotelDetail(
    val address: Address? = null,
    val description: String = "",
    val photoUrls: List<String> = emptyList(),
    val hotelInformation: HotelInformation? = null,
    val facilities: List<String> = emptyList(),
    val location: Location? = null,
    val reviews: List<Review> = emptyList()
)


