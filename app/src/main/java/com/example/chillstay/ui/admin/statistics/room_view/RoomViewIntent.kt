package com.example.chillstay.ui.admin.statistics.room_view

import com.example.chillstay.core.base.UiEvent

sealed interface RoomViewIntent : UiEvent {
    // Load operations
    data class LoadRoomStatistics(val roomId: String) : RoomViewIntent
    data object ApplyFilters : RoomViewIntent

    // Date filter operations
    data class DateFromChanged(val dateMillis: Long?) : RoomViewIntent
    data class DateToChanged(val dateMillis: Long?) : RoomViewIntent
    data object ToggleDateFromPicker : RoomViewIntent
    data object ToggleDateToPicker : RoomViewIntent

    // Pagination operations
    data class GoToPage(val page: Int) : RoomViewIntent
    data object NextPage : RoomViewIntent
    data object PreviousPage : RoomViewIntent

    // Navigation
    data object NavigateBack : RoomViewIntent
}