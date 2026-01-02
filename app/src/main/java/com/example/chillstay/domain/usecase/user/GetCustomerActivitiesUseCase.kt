package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.CustomerActivity
import com.example.chillstay.domain.repository.ActivityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetCustomerActivitiesUseCase @Inject constructor(
    private val activityRepository: ActivityRepository
) {
    operator fun invoke(userId: String, type: String? = null): Flow<Result<List<CustomerActivity>>> = flow {
        try {
            val activities = activityRepository.getCustomerActivities(userId, type)
            emit(Result.Success(activities))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}