package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room

interface HotelRepository {
    suspend fun getHotels(): List<Hotel>
    suspend fun getHotelById(id: String): Hotel?
    suspend fun searchHotels(
        query: String,
        country: String? = null,
        city: String? = null,
        minRating: Double? = null,
        maxPrice: Double? = null
    ): List<Hotel>
    suspend fun getHotelsByCity(city: String): List<Hotel>
    suspend fun updateHotelAggregation(hotelId: String, rating: Double, numberOfReviews: Int): Boolean
    suspend fun createHotel(hotel: Hotel): String
    suspend fun updateHotel(hotel: Hotel)
}


