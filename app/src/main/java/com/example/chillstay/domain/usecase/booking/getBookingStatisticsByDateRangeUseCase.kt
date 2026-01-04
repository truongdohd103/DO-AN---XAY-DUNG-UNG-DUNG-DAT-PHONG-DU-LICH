// GetBookingStatisticsByDateRangeUseCase.kt
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

class GetBookingStatisticsByDateRangeUseCase(
    private val bookingStatisticsRepository: BookingStatisticsRepository
) {

    operator fun invoke(
        country: String?,
        city: String?,
        dateFrom: Long?,  // milliseconds
        dateTo: Long?     // milliseconds
    ): Flow<Result<BookingStatistics>> = flow {
        val statistics = bookingStatisticsRepository.getBookingStatisticsByDateRange(
            country = country,
            city = city,
            dateFrom = dateFrom,
            dateTo = dateTo
        )
        emit(Result.Success(statistics))
    }.catch { e ->
        if (e is CancellationException) throw e
    }.flowOn(Dispatchers.IO)
}