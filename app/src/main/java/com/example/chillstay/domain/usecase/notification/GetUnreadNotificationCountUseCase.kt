package com.example.chillstay.domain.usecase.notification

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    operator fun invoke(userId: String): Flow<Result<Int>> = flow {
        try {
            val count = notificationRepository.getUnreadCount(userId)
            emit(Result.success(count))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
