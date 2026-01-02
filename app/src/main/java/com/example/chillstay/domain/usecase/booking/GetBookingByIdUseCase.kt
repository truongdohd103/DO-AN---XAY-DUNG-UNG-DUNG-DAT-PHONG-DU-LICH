package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetBookingByIdUseCase constructor(private val bookingRepository: BookingRepository) {
    operator fun invoke(bookingId: String): Flow<Result<Booking>> = flow {
        val review = bookingRepository.getBookingById(bookingId)
            ?: throw IllegalStateException("Review not found")
        emit(Result.success(review))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}

