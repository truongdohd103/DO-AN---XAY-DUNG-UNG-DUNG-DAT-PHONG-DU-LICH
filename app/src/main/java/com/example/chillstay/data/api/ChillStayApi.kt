package com.example.chillstay.data.api

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Room

interface ChillStayApi {
    suspend fun getPopularHotels(limit: Int = 5): List<Hotel>
    suspend fun getRecommendedHotels(limit: Int = 3): List<Hotel>
    suspend fun getTrendingHotels(limit: Int = 2): List<Hotel>
    suspend fun getHotelById(hotelId: String): Hotel?
    suspend fun getRooms(hotelId: String): List<Room>
    suspend fun getUserBookings(userId: String): List<Booking>
    suspend fun getUserBookmarks(userId: String): List<Bookmark>
}


