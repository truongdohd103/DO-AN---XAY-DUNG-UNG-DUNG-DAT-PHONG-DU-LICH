package com.example.chillstay.domain.model

data class CustomerStats(
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val totalReviews: Int = 0,
    val memberSince: String = ""
)