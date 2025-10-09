package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.domain.repository.NotificationRepository
import com.example.chillstay.core.common.Result


class MarkAllNotificationsAsReadUseCase constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(userId: String): Result<Boolean> {
        return try {
            val success = notificationRepository.markAllAsRead(userId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to mark all notifications as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

