package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.BookingStatistics
import com.example.chillstay.domain.repository.BookingStatisticsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetBookingStatisticsByYearQuarterMonthUseCase(
    private val bookingStatisticsRepository: BookingStatisticsRepository
) {

    operator fun invoke(
        country: String?,
        city: String?,
        year: Int?,
        quarter: Int?,
        month: Int?
    ): Flow<Result<BookingStatistics>> = flow {
        val statistics = bookingStatisticsRepository.getBookingStatistics(
            country = country,
            city = city,
            year = year,
            quarter = quarter,
            month = month
        )
        emit(Result.Success(statistics))
    }.catch { e ->
        if (e is CancellationException) throw e
    }.flowOn(Dispatchers.IO)
}
