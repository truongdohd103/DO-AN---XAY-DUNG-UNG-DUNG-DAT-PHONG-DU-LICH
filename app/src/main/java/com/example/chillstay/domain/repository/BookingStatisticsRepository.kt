package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingStatistics
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.StatisticsPeriod


interface BookingStatisticsRepository {
    suspend fun getBookingStatistics(
        country: String? = null,
        city: String? = null,
        year: Int? = null,
        quarter: Int? = null,
        month: Int? = null
    ): BookingStatistics

    suspend fun getBookingStatisticsByDateRange(
        country: String?,
        city: String?,
        dateFrom: Long?,
        dateTo: Long?
    ): BookingStatistics
}