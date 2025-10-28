package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp? = null,
    val data: Map<String, String> = emptyMap() // For additional payload
)

enum class NotificationType {
    GENERAL,
    BOOKING_CONFIRMATION,
    BOOKING_CANCELLED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    REVIEW_REMINDER,
    PROMOTION,
    SYSTEM
}
