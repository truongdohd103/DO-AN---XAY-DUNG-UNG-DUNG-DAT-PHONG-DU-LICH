package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.domain.repository.NotificationRepository
import com.example.chillstay.core.common.Result


class MarkNotificationAsReadUseCase constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Boolean> {
        return try {
            val success = notificationRepository.markAsRead(notificationId)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to mark notification as read"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
