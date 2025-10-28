package com.example.chillstay.ui.bookmark

import com.example.chillstay.core.base.UiEvent

sealed interface MyBookmarkIntent : UiEvent {
    data class LoadBookmarks(val userId: String) : MyBookmarkIntent
    data class RemoveBookmark(val bookmarkId: String, val hotelId: String) : MyBookmarkIntent
    data class RefreshBookmarks(val userId: String) : MyBookmarkIntent
    data class RetryLoad(val userId: String) : MyBookmarkIntent
}
