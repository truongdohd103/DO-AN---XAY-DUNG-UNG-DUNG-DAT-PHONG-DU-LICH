package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class GetCurrentUserIdUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<String?>> = authRepository.observeCurrentUserId()
}


