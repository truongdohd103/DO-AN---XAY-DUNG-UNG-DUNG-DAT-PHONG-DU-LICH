package com.example.chillstay.domain.usecase.bookmark

import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.core.common.Result
import java.time.Instant


class AddBookmarkUseCase constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(userId: String, hotelId: String): Result<Bookmark> {
        return try {
            // Check if bookmark already exists
            val existingBookmark = bookmarkRepository.getUserBookmarks(userId)
                .find { it.hotelId == hotelId }
            
            if (existingBookmark != null) {
                return Result.failure(Exception("Hotel is already bookmarked"))
            }
            
            val bookmark = Bookmark(
                id = "", // Will be set by repository
                userId = userId,
                hotelId = hotelId,
                createdAt = Instant.now()
            )
            
            val createdBookmark = bookmarkRepository.addBookmark(bookmark)
            Result.success(createdBookmark)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

