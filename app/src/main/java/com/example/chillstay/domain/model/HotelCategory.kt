package com.example.chillstay.domain.model

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


