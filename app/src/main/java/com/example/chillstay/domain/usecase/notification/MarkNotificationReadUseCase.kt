package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MarkNotificationReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(notificationId: String): Flow<Result<Boolean>> = flow {
        try {
            val success = notificationRepository.markAsRead(notificationId)
            emit(Result.success(success))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    fun markAll(userId: String): Flow<Result<Boolean>> = flow {
        try {
            val success = notificationRepository.markAllAsRead(userId)
            emit(Result.success(success))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
