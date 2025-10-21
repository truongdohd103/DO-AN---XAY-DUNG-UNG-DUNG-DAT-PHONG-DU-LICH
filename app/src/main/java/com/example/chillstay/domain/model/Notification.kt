package com.example.chillstay.domain.model

import com.google.firebase.Timestamp

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val priority: Int = 0
)


