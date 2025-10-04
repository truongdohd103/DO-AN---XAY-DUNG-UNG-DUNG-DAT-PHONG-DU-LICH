package com.example.chillstay.domain.usecase.user

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.core.common.Result


class GetUserProfileUseCase constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return try {
            val user = userRepository.getUser(userId)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
