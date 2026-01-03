package com.example.chillstay.domain.usecase.booking

import android.util.Log
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.BookingStatistics
import com.example.chillstay.domain.repository.BookingStatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetBookingStatisticsUseCase(
    private val bookingStatisticsRepository: BookingStatisticsRepository
) {

    companion object {
        private const val TAG = "GetBookingStatsUseCase"
    }

    operator fun invoke(
        country: String?,
        city: String?,
        year: Int?,
        quarter: Int?,
        month: Int?
    ): Flow<Result<BookingStatistics>> = flow {
        try {
            Log.d(TAG, "Requesting statistics: country=$country, city=$city, year=$year, quarter=$quarter, month=$month")

            // Repository handles all the heavy lifting
            val statistics = bookingStatisticsRepository.getBookingStatistics(
                country = country,
                city = city,
                year = year,
                quarter = quarter,
                month = month
            )

            Log.d(TAG, "Successfully loaded statistics: revenue=${statistics.totalRevenue}, bookings=${statistics.totalBookings}")
            emit(Result.Success(statistics))
        } catch (e: Exception) {
            Log.e(TAG, "Error loading statistics", e)
            emit(Result.Error(e))
        }
    }
}