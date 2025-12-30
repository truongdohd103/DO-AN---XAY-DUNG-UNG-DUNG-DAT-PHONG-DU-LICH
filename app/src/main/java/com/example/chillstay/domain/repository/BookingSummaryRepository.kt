package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.BookingSummary

interface BookingSummaryRepository {
    suspend fun getAllBookingSummaries(): List<BookingSummary>
}