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
    val status: HotelStatus = HotelStatus.ACTIVE,

    val reviews: List<Review> = emptyList(),
    val rooms: List<Room> = emptyList()
)

enum class PropertyType {
    HOTEL,
    RESORT
}

enum class HotelStatus {
    ACTIVE,
    INACTIVE
}

enum class HotelCategory {
    POPULAR,
    RECOMMENDED,
    TRENDING,
    ALL
}

data class HotelListFilter(
    val category: HotelCategory = HotelCategory.POPULAR,
    val limit: Int? = null,
    val country: String? = null,
    val city: String? = null,
    val minRating: Double? = null
)


