package com.example.chillstay.domain.usecase.bookmark

import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.core.common.Result


class GetUserBookmarksUseCase constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Bookmark>> {
        return try {
            val bookmarks = bookmarkRepository.getUserBookmarks(userId)
            Result.success(bookmarks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

