package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateUserStatusUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String, isActive: Boolean): Flow<Result<User>> = flow {
        try {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(isActive = isActive)
                emit(Result.Success(updatedUser))
            } else {
                emit(Result.Error(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}