package com.example.chillstay.domain.model

import java.time.Instant

data class Notification(
    val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Instant,
    val priority: Int
)


