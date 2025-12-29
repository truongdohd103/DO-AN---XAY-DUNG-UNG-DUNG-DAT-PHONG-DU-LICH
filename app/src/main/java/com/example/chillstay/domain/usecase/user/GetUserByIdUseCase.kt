package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow


class GetUserByIdUseCase constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<User>> = flow {
        val user = userRepository.getUserById(userId) ?: throw IllegalStateException("User not found")
        emit(Result.success(user))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
