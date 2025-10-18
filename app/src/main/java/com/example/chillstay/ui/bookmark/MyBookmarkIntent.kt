package com.example.chillstay.ui.bookmark

sealed interface MyBookmarkIntent {
    data class LoadBookmarks(val userId: String) : MyBookmarkIntent
    data class RemoveBookmark(val bookmarkId: String, val hotelId: String) : MyBookmarkIntent
    data class RefreshBookmarks(val userId: String) : MyBookmarkIntent
    data class RetryLoad(val userId: String) : MyBookmarkIntent
}
