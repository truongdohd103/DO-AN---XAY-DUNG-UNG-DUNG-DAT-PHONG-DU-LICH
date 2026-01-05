package com.example.chillstay.domain.model

data class BookingStatistics(
    val totalRevenue: Double,
    val totalBookings: Int,
    val cancellationRate: Double,
    val bookingsByHotel: Map<String, HotelBookingStats>,
    val periodRevenue: Map<String, Double>, // Renamed from dailyRevenue
    val periodLabels: List<String> // Labels for chart
)

data class HotelBookingStats(
    val hotelId: String,
    val hotelName: String,
    val bookings: Int,
    val revenue: Double,
    val cancellationRate: Double
)

data class CustomerStats(
    val id: String = "",
    val name: String? = "",
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val totalReviews: Int = 0,
    val memberSince: String = ""
)

data class CustomerStatistics(
    val totalRevenue: Double,
    val totalBookings: Int,
    val totalCustomers: Int,
    val bookingsByCustomer: Map<String, CustomerStats>,
    val periodRevenue: Map<String, Double>,
    val periodLabels: List<String>
)
