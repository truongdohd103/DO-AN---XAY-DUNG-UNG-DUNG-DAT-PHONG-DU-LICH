package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

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