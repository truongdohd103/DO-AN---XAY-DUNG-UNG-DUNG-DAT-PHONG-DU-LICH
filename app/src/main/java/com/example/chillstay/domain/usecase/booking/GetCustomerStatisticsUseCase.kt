package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.CustomerStatistics
import com.example.chillstay.domain.repository.BookingStatisticsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Use case để lấy customer statistics với filter theo year/quarter/month
 *
 * FIXED: Đã sửa để match với GetBookingStatisticsByYearQuarterMonthUseCase
 * - Xóa try-catch bên trong flow block
 * - Thêm .catch {} operator để handle exceptions
 * - Thêm .flowOn(Dispatchers.IO) để chạy trên IO thread
 */
class GetCustomerStatisticsUseCase(
    private val bookingStatisticsRepository: BookingStatisticsRepository
) {

    operator fun invoke(
        year: Int?,
        quarter: Int?,
        month: Int?
    ): Flow<Result<CustomerStatistics>> = flow {
        val statistics = bookingStatisticsRepository.getCustomerStatistics(
            year = year,
            quarter = quarter,
            month = month
        )

        emit(Result.Success(statistics))

    }.catch { e ->
        if (e is CancellationException) throw e
    }.flowOn(Dispatchers.IO)
}