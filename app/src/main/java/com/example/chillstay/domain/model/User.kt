package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val gender: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: LocalDate = LocalDate.of(2000, 1, 1),
    val isActive: Boolean = true,
    val role : UserRole = UserRole.USER,
    val memberSince: Timestamp? = Timestamp.now()
)

enum class UserRole {
    ADMIN,
    USER
}


data class CustomerStats(
    val totalBookings: Int = 0,
    val totalSpent: Double = 0.0,
    val totalReviews: Int = 0,
    val memberSince: String = ""
)

data class CustomerActivity(
    val id: String = "",
    val userId: String = "",
    val type: ActivityType = ActivityType.BOOKING,
    val title: String = "",
    val description: String = "",
    val relatedId: String = "", // bookingId or reviewId
    val createdAt: Timestamp = Timestamp.now()
)

enum class ActivityType {
    BOOKING,
    REVIEW,
    BOOKING_COMPLETED,
    BOOKING_CANCELLED
}