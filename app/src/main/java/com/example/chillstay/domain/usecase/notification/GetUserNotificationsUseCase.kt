package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.repository.NotificationRepository
import com.example.chillstay.core.common.Result


class GetUserNotificationsUseCase constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        userId: String,
        isRead: Boolean? = null,
        limit: Int? = null
    ): Result<List<Notification>> {
        return try {
            val notifications = notificationRepository.getUserNotifications(userId, isRead, limit)
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

