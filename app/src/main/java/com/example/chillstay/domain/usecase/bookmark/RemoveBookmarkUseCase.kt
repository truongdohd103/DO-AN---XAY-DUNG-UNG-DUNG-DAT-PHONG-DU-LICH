package com.example.chillstay.domain.usecase.bookmark

import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.core.common.Result


class RemoveBookmarkUseCase constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(userId: String, hotelId: String): Result<Boolean> {
        return try {
            val success = bookmarkRepository.removeBookmark(userId, hotelId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to remove bookmark"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


