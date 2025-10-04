package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Hotel

interface HotelRepository {
    suspend fun getHotels(): List<Hotel>
    suspend fun getHotel(id: String): Hotel?
    suspend fun searchHotels(query: String): List<Hotel>
    suspend fun getHotelsByCity(city: String): List<Hotel>
}


