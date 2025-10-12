package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Bookmark

interface BookmarkRepository {
    suspend fun getUserBookmarks(userId: String): List<Bookmark>
    suspend fun addBookmark(bookmark: Bookmark): Bookmark
    suspend fun removeBookmark(userId: String, hotelId: String): Boolean
    suspend fun isBookmarked(userId: String, hotelId: String): Boolean
}


