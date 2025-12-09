package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class SignOutUseCase(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<Result<Unit>> = flow {
        authRepository.signOut()
        emit(Result.success(Unit))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}


