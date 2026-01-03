package com.example.chillstay.domain.model

data class BookingStatistics(
    val totalRevenue: Double,
    val totalBookings: Int,
    val cancellationRate: Double,
    val bookingsByHotel: Map<String, HotelBookingStats>,
    val periodRevenue: Map<String, Double>, // Renamed from dailyRevenue
    val periodLabels: List<String> // Labels for chart
)

enum class StatisticsPeriod {
    DAILY,      // Last 7 days
    MONTHLY,    // Last 12 months
    QUARTERLY,  // Last 4 quarters
    YEARLY      // Last 5 years
}

data class HotelBookingStats(
    val hotelId: String,
    val hotelName: String,
    val bookings: Int,
    val revenue: Double,
    val cancellationRate: Double
)

data class CustomerStats(
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val totalReviews: Int = 0,
    val memberSince: String = ""
)
