package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Booking

interface BookingRepository {
    suspend fun getAllBookings(): List<Booking>
    suspend fun getBookingById(id: String): Booking?
    suspend fun getUserBookings(userId: String, status: String? = null): List<Booking>
    suspend fun createBooking(booking: Booking): Booking
    suspend fun updateBooking(booking: Booking): Booking
    suspend fun cancelBooking(bookingId: String): Boolean
    suspend fun getBookingHotelId(bookingId: String): String?
    suspend fun deleteBooking(bookingId: String): Boolean
}
