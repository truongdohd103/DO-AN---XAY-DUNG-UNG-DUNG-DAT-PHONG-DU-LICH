package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.repository.NotificationRepository
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetAllNotificationsUseCase(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(): Flow<Result<List<Notification>>> = flow {
        try {
            val notifications = notificationRepository.getAllNotifications()
            emit(Result.Success(notifications))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}
