package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Notification>>> = flow {
        try {
            val notifications = notificationRepository.getUserNotifications(userId)
            emit(Result.success(notifications))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
