package com.example.chillstay.domain.usecase.booking

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.BookingSummary
import com.example.chillstay.domain.repository.BookingSummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetAllBookingSummariesUseCase(
    private val bookingSummaryRepository: BookingSummaryRepository
) {
    operator fun invoke(): Flow<Result<List<BookingSummary>>> = flow {
        val summaries = bookingSummaryRepository.getAllBookingSummaries()
        emit(Result.success(summaries))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}