package com.example.chillstay.domain.usecase

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository

class SignInUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            // For demo, we'll use a simple authentication
            // In real app, this would check password hash
            val user = userRepository.getUserByEmail(email)
            if (user != null && user.password == password) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
