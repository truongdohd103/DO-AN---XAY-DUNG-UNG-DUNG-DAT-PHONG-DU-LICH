package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Notification

interface NotificationRepository {
    suspend fun getUserNotifications(userId: String, isRead: Boolean? = null, limit: Int? = null): List<Notification>
    suspend fun createNotification(notification: Notification): Notification
    suspend fun markAsRead(notificationId: String): Boolean
    suspend fun markAllAsRead(userId: String): Boolean
    suspend fun deleteNotification(notificationId: String): Boolean
    suspend fun getUnreadCount(userId: String): Int
}
