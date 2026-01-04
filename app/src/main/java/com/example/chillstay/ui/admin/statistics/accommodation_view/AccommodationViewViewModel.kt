package com.example.chillstay.ui.admin.statistics.accommodation_view

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.HotelBookingStats
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.usecase.booking.GetBookingStatisticsByDateRangeUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.room.GetRoomsByHotelIdUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccommodationViewViewModel(
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getRoomsByHotelIdUseCase: GetRoomsByHotelIdUseCase,
    private val getBookingStatisticsByDateRangeUseCase: GetBookingStatisticsByDateRangeUseCase
) : BaseViewModel<AccommodationViewUiState, AccommodationViewIntent, AccommodationViewEffect>(
    AccommodationViewUiState()
) {

    companion object {
        private const val TAG = "AccommodationViewVM"

        // Colors for pie chart
        private val CHART_COLORS = listOf(
            0xFF1AB5B5.toInt(),  // Teal
            0xFF4CAF50.toInt(),  // Green
            0xFFFFC107.toInt(),  // Amber
            0xFFFF9800.toInt(),  // Orange
            0xFF2196F3.toInt()   // Blue
        )
    }

    val uiState = state

    override fun onEvent(event: AccommodationViewIntent) {
        when (event) {
            is AccommodationViewIntent.LoadHotelStatistics -> {
                _state.value = _state.value.copy(hotelId = event.hotelId)
                loadHotelData(event.hotelId)
                loadStatistics()
            }
            is AccommodationViewIntent.ApplyFilters -> {
                loadStatistics()
            }
            is AccommodationViewIntent.DateFromChanged -> {
                _state.value = _state.value.copy(
                    dateFrom = event.dateMillis,
                    showDateFromPicker = false
                )
            }
            is AccommodationViewIntent.DateToChanged -> {
                _state.value = _state.value.copy(
                    dateTo = event.dateMillis,
                    showDateToPicker = false
                )
            }
            is AccommodationViewIntent.ToggleDateFromPicker -> {
                _state.value = _state.value.copy(
                    showDateFromPicker = !_state.value.showDateFromPicker,
                    showDateToPicker = false
                )
            }
            is AccommodationViewIntent.ToggleDateToPicker -> {
                _state.value = _state.value.copy(
                    showDateToPicker = !_state.value.showDateToPicker,
                    showDateFromPicker = false
                )
            }
            is AccommodationViewIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.copy(currentPage = event.page)
                }
            }
            is AccommodationViewIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.copy(currentPage = currentPage + 1)
                }
            }
            is AccommodationViewIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.copy(currentPage = currentPage - 1)
                }
            }
            is AccommodationViewIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { AccommodationViewEffect.NavigateBack }
                }
            }
            is AccommodationViewIntent.NavigateToRoom -> {
                viewModelScope.launch {
                    sendEffect { AccommodationViewEffect.NavigateToRoom(event.roomId) }
                }
            }
        }
    }

    private fun loadHotelData(hotelId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "========================================")
                Log.d(TAG, "🏨 Loading hotel data for ID: $hotelId")
                Log.d(TAG, "========================================")

                val result = getHotelByIdUseCase(hotelId).first()

                when(result) {
                    is Result.Success -> {
                        val hotel = result.data
                        Log.d(TAG, "✅ Hotel loaded successfully:")
                        Log.d(TAG, "   - ID: ${hotel.id}")
                        Log.d(TAG, "   - Name: ${hotel.name}")
                        Log.d(TAG, "   - City: ${hotel.city}")
                        Log.d(TAG, "   - Country: ${hotel.country}")
                        Log.d(TAG, "   - Rating: ${hotel.rating}")

                        _state.value = _state.value.copy(hotel = hotel)

                        Log.d(TAG, "✅ Hotel saved to state")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "❌ Error loading hotel: ${result.throwable.message}")
                        sendEffect {
                            AccommodationViewEffect.ShowError(
                                result.throwable.message ?: "Failed to load hotel"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading hotel: ${e.message}", e)
                sendEffect {
                    AccommodationViewEffect.ShowError(
                        e.message ?: "Failed to load hotel"
                    )
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            val hotelId = _state.value.hotelId
            if (hotelId.isEmpty()) {
                Log.e(TAG, "❌ Hotel ID is empty")
                return@launch
            }

            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Ensure we have hotel data
                var hotel = _state.value.hotel
                if (hotel == null) {
                    val hotelResult = getHotelByIdUseCase(hotelId).first()
                    if (hotelResult is Result.Success) {
                        hotel = hotelResult.data
                        _state.value = _state.value.copy(hotel = hotel)
                    } else if (hotelResult is Result.Error) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = hotelResult.throwable.message ?: "Failed to load hotel"
                        )
                        sendEffect {
                            AccommodationViewEffect.ShowError(
                                hotelResult.throwable.message ?: "Failed to load hotel"
                            )
                        }
                        return@launch
                    }
                }

                // Load rooms
                Log.d(TAG, "🛏️ Fetching rooms for hotel...")
                val roomResult = getRoomsByHotelIdUseCase(
                    hotelId = hotelId,
                    checkIn = null,
                    checkOut = null,
                    guests = null
                ).first()

                when (roomResult) {
                    is Result.Success -> {
                        val rooms = roomResult.data
                        Log.d(TAG, "Rooms loaded: ${rooms.size} rooms")

                        if (rooms.isEmpty()) {
                            Log.w(TAG, "No rooms found for this hotel")
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = "No rooms found for this hotel"
                            )
                            return@launch
                        }

                        val statsResult = getBookingStatisticsByDateRangeUseCase(
                            country = hotel?.country,
                            city = hotel?.city,
                            dateFrom = _state.value.dateFrom,
                            dateTo = _state.value.dateTo
                        ).first()

                        when (statsResult) {
                            is Result.Success -> {
                                val stats = statsResult.data
                                val hotelStats = stats.bookingsByHotel[hotelId]

                                if (hotelStats == null) {
                                    Log.w(TAG, "No booking data found for hotel ID: $hotelId")
                                    stats.bookingsByHotel.keys.forEachIndexed { idx, id ->
                                        Log.d(TAG, "      ${idx + 1}. $id")
                                    }
                                    // Use empty stats
                                    hotel?.name?.let {
                                        processAndUpdateState(
                                            rooms = rooms,
                                            hotelStats = HotelBookingStats(
                                                hotelId = hotelId,
                                                hotelName = it,
                                                bookings = 0,
                                                revenue = 0.0,
                                                cancellationRate = 0.0
                                            ),
                                            globalCancellationRate = stats.cancellationRate
                                        )
                                    }
                                } else {
                                    Log.d(TAG, "✅ Hotel-specific stats found:")
                                    Log.d(TAG, "   - Hotel Name: ${hotelStats.hotelName}")
                                    Log.d(TAG, "   - Revenue: \$${hotelStats.revenue}")
                                    Log.d(TAG, "   - Bookings: ${hotelStats.bookings}")
                                    Log.d(TAG, "   - Cancellation Rate: ${hotelStats.cancellationRate}%")

                                    processAndUpdateState(
                                        rooms = rooms,
                                        hotelStats = hotelStats,
                                        globalCancellationRate = stats.cancellationRate
                                    )
                                }
                            }
                            is Result.Error -> {
                                Log.e(TAG, "❌ Error loading statistics: ${statsResult.throwable.message}")
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = statsResult.throwable.message ?: "Failed to load statistics"
                                )
                                sendEffect {
                                    AccommodationViewEffect.ShowError(
                                        statsResult.throwable.message ?: "Failed to load statistics"
                                    )
                                }
                            }
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "❌ Error loading rooms: ${roomResult.throwable.message}")
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = roomResult.throwable.message ?: "Failed to load rooms"
                        )
                        sendEffect {
                            AccommodationViewEffect.ShowError(
                                roomResult.throwable.message ?: "Failed to load rooms"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception loading statistics: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load statistics"
                )
                sendEffect {
                    AccommodationViewEffect.ShowError(
                        e.message ?: "Failed to load statistics"
                    )
                }
            }
        }
    }

    private suspend fun processAndUpdateState(
        rooms: List<Room>,
        hotelStats: HotelBookingStats,
        globalCancellationRate: Double
    ) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "🔄 Processing room statistics...")
        Log.d(TAG, "========================================")

        val roomTypeRevenue = processRoomTypeRevenue(rooms, hotelStats)
        val roomStats = processRoomStats(rooms, hotelStats)

        Log.d(TAG, "✅ Processing complete:")
        Log.d(TAG, "   - Room Types: ${roomStats.size}")
        Log.d(TAG, "   - Revenue breakdown items: ${roomTypeRevenue.size}")

        roomStats.forEachIndexed { index, stat ->
            Log.d(TAG, "   ${index + 1}. ${stat.roomType}:")
            Log.d(TAG, "      - Bookings: ${stat.bookings}")
            Log.d(TAG, "      - Revenue: \$${String.format("%.2f", stat.revenue)}")
            Log.d(TAG, "      - Avg Rate: \$${String.format("%.2f", stat.avgRate)}")
        }

        val topByBookings = roomStats.maxByOrNull { it.bookings }
        val topByRevenue = roomStats.maxByOrNull { it.revenue }

        if (topByBookings != null) {
            Log.d(TAG, "🏆 Top by Bookings: ${topByBookings.roomType} (${topByBookings.bookings} bookings)")
        }
        if (topByRevenue != null) {
            Log.d(TAG, "💰 Top by Revenue: ${topByRevenue.roomType} (\$${String.format("%.2f", topByRevenue.revenue)})")
        }

        withContext(Dispatchers.Main) {
            _state.value = _state.value.copy(
                isLoading = false,
                totalRevenue = hotelStats.revenue,
                totalBookings = hotelStats.bookings,
                cancellationRate = hotelStats.cancellationRate,
                roomTypeRevenue = roomTypeRevenue,
                roomStats = roomStats,
                topByBookings = topByBookings,
                topByRevenue = topByRevenue,
                currentPage = 1,
                error = null
            )
            Log.d(TAG, "✅ State updated successfully")
            Log.d(TAG, "========================================")
        }
    }


    private fun processRoomTypeRevenue(
        rooms: List<Room>,
        hotelStats: HotelBookingStats
    ): List<RoomTypeRevenue> {
        if (rooms.isEmpty()) {
            Log.w(TAG, "⚠️ No rooms to process for revenue")
            return emptyList()
        }

        if (hotelStats.revenue == 0.0) {
            Log.w(TAG, "⚠️ Hotel has zero revenue")
            return emptyList()
        }

        Log.d(TAG, "📊 Revenue by Room Type calculation:")
        Log.d(TAG, "   - Total rooms: ${rooms.size}")
        Log.d(TAG, "   - Total hotel revenue: \$${hotelStats.revenue}")

        // Calculate total price for proportional distribution
        val totalPrice = rooms.sumOf { it.price }

        if (totalPrice == 0.0) {
            Log.w(TAG, "⚠️ Total price is 0")
            return emptyList()
        }

        Log.d(TAG, "   - Total price sum: \$${totalPrice}")

        // ✅ Create room data with proper names
        return rooms.mapIndexed { index, room ->
            // ✅ FIX #2: Handle empty room name
            val displayName = if (room.name.isBlank()) {
                "Unknown Room ${index + 1}"
            } else {
                room.name
            }

            // ✅ Allocate revenue based on price proportion
            val revenue = hotelStats.revenue * (room.price / totalPrice)

            Log.d(TAG, "   ${index + 1}. $displayName:")
            Log.d(TAG, "      - Price: \$${String.format("%.2f", room.price)}")
            Log.d(TAG, "      - Revenue Share: \$${String.format("%.2f", revenue)} (${String.format("%.1f", (room.price / totalPrice) * 100)}%)")

            RoomTypeRevenue(
                roomType = displayName,
                revenue = revenue,
                color = CHART_COLORS[index % CHART_COLORS.size]
            )
        }.filter { it.revenue > 0 }
            .sortedByDescending { it.revenue }
            .take(5)
    }

    private fun processRoomStats(
        rooms: List<Room>,
        hotelStats: HotelBookingStats
    ): List<RoomStats> {
        if (rooms.isEmpty()) {
            Log.w(TAG, "No rooms to process for stats")
            return emptyList()
        }

        // Calculate total price for proportional distribution
        val totalPrice = rooms.sumOf { it.price }

        if (totalPrice == 0.0) {
            Log.w(TAG, "Total price is 0")
            return emptyList()
        }

        return rooms.mapIndexed { index, room ->
            val displayName = room.name.ifBlank {
                "Unknown Room ${index + 1}"
            }

            val bookings = (hotelStats.bookings * (room.price / totalPrice)).toInt()

            val revenue = hotelStats.revenue * (room.price / totalPrice)

            RoomStats(
                roomId = room.id,
                roomType = displayName,
                bookings = bookings,
                revenue = revenue,
                avgRate = room.price
            )
        }.sortedByDescending { it.revenue }
    }
}