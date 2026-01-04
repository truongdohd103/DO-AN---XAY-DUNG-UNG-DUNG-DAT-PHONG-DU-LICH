package com.example.chillstay.ui.admin.statistics.room_view

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Room

@Immutable
data class RoomViewUiState(
    // Loading states
    val isLoading: Boolean = false,
    val error: String? = null,

    // Room info
    val roomId: String = "",
    val room: Room? = null,
    val hotelName: String = "",

    // Date filters
    val dateFrom: Long? = null,
    val dateTo: Long? = null,
    val showDateFromPicker: Boolean = false,
    val showDateToPicker: Boolean = false,

    // Statistics data
    val totalRevenue: Double = 0.0,
    val totalBookings: Int = 0,
    val cancellationRate: Double = 0.0,

    // Chart data (daily revenue)
    val chartData: List<Pair<String, Double>> = emptyList(),
    val maxRevenueValue: Double = 0.0,

    // Recent bookings
    val recentBookings: List<BookingInfo> = emptyList(),

    // Pagination
    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {

    val totalPages: Int
        get() = if (recentBookings.isEmpty()) 1
        else (recentBookings.size + itemsPerPage - 1) / itemsPerPage

    val paginatedBookings: List<BookingInfo>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, recentBookings.size)
            return if (startIndex < recentBookings.size) {
                recentBookings.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }
}

data class BookingInfo(
    val bookingId: String,
    val guestName: String,
    val nights: Int,
    val amount: Double,
    val checkInDate: String,
    val status: String
)