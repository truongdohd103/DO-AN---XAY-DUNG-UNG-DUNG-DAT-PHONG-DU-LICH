package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CreateNotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(notification: Notification): Flow<Result<Notification>> = flow {
        try {
            val createdNotification = notificationRepository.createNotification(notification)
            emit(Result.success(createdNotification))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
