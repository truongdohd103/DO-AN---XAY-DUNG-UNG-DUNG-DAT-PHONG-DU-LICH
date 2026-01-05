package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.BookingStatistics
import com.example.chillstay.domain.model.CustomerStatistics

interface BookingStatisticsRepository {
    suspend fun getBookingStatistics(
        country: String? = null,
        city: String? = null,
        year: Int? = null,
        quarter: Int? = null,
        month: Int? = null
    ): BookingStatistics

    suspend fun getCustomerStatistics(
        year: Int?,
        quarter: Int?,
        month: Int?
    ): CustomerStatistics

    suspend fun getBookingStatisticsByDateRange(
        country: String?,
        city: String?,
        dateFrom: Long?,
        dateTo: Long?
    ): BookingStatistics
}