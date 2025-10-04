package com.example.chillstay.data.repository

import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.repository.HotelRepository

class InMemoryHotelRepository : HotelRepository {
    override suspend fun getHotels(): List<Hotel> = emptyList()
    override suspend fun getHotel(id: String): Hotel? = null
    override suspend fun searchHotels(query: String): List<Hotel> = emptyList()
    override suspend fun getHotelsByCity(city: String): List<Hotel> = emptyList()
}


