package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Booking

interface BookingRepository {
    suspend fun getBookings(userId: String): List<Booking>
    suspend fun createBooking(booking: Booking): Booking
    suspend fun updateBooking(booking: Booking): Booking
    suspend fun cancelBooking(id: String)
}


