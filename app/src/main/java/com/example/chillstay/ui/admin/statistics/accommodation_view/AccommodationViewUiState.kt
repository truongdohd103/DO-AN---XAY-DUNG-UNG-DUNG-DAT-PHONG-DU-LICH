package com.example.chillstay.ui.admin.statistics.accommodation_view

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel

@Immutable
data class AccommodationViewUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Hotel info
    val hotelId: String = "",
    val hotel: Hotel? = null,

    // Date filters
    val dateFrom: Long? = null,  // milliseconds
    val dateTo: Long? = null,    // milliseconds
    val showDateFromPicker: Boolean = false,
    val showDateToPicker: Boolean = false,

    // Statistics data
    val totalRevenue: Double = 0.0,
    val totalBookings: Int = 0,
    val cancellationRate: Double = 0.0,

    // Room type revenue (for pie chart)
    val roomTypeRevenue: List<RoomTypeRevenue> = emptyList(),

    // Room stats (for table with pagination)
    val roomStats: List<RoomStats> = emptyList(),

    // Top performers
    val topByBookings: RoomStats? = null,
    val topByRevenue: RoomStats? = null,

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {

    // Computed properties
    val totalPages: Int
        get() = if (roomStats.isEmpty()) 1
        else (roomStats.size + itemsPerPage - 1) / itemsPerPage

    val paginatedRoomStats: List<RoomStats>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, roomStats.size)
            return if (startIndex < roomStats.size) {
                roomStats.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

    // Total revenue for percentage calculation
    val totalRoomTypeRevenue: Double
        get() = roomTypeRevenue.sumOf { it.revenue }
}

data class RoomTypeRevenue(
    val roomType: String,
    val revenue: Double,
    val color: Int  // Color for pie chart
)

data class RoomStats(
    val roomType: String,
    val bookings: Int,
    val revenue: Double,
    val avgRate: Double
)