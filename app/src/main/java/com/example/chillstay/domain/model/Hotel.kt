package com.example.chillstay.domain.model

data class Hotel(
    val id: String = "",
    val city: String = "",
    val coordinate: Coordinate = Coordinate(0.0, 0.0) ,
    val country: String = "",
    val description: String = "",
    val feature: List<String> = emptyList(),
    val formattedAddress: String = "",
    val imageUrl: List<String> = emptyList(),
    val language: List<String> = emptyList(),
    val minPrice: Double? = null,
    val name: String = "",
    val numberOfReviews: Int = 0,
    val policy: List<Policy> = emptyList(),
    val propertyType: PropertyType = PropertyType.HOTEL,
    val rating: Double = 0.0,

    val reviews: List<Review> = emptyList(),
    val rooms: List<Room> = emptyList()
)

enum class PropertyType {
    HOTEL,
    RESORT
}

enum class Status {
    ACTIVE,
    INACTIVE
}


