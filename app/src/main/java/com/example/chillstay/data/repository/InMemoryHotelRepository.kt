package com.example.chillstay.data.repository

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.repository.HotelRepository

class InMemoryHotelRepository : HotelRepository {
    override suspend fun getHotels(): List<Hotel> = emptyList()
    override suspend fun getHotelById(id: String): Hotel? = null
    override suspend fun searchHotels(
        query: String,
        country: String?,
        city: String?,
        minRating: Double?,
        maxPrice: Double?
    ): List<Hotel> = emptyList()
    override suspend fun getHotelsByCity(city: String): List<Hotel> = emptyList()
    override suspend fun getHotelRooms(
        hotelId: String,
        checkIn: String?,
        checkOut: String?,
        guests: Int?
    ): List<Room> = emptyList()
}


