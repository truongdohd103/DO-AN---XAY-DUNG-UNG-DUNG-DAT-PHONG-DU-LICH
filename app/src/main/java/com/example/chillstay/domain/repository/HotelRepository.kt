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
    suspend fun getHotelRooms(
        hotelId: String,
        checkIn: String? = null,
        checkOut: String? = null,
        guests: Int? = null
    ): List<Room>
    suspend fun getRoomById(roomId: String): Room?
}


