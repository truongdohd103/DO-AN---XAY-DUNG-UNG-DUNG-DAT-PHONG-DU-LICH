package com.example.chillstay.domain.usecase

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import java.time.LocalDate

class SignUpUseCase(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            // For demo, we'll create a simple user
            val user = User(
                id = "", // Will be set by repository
                email = email,
                password = password,
                fullName = email.substringBefore("@"),
                gender = "Unknown",
                photoUrl = "",
                dateOfBirth = LocalDate.now().minusYears(25)
            )
            val createdUser = userRepository.createUser(user)
            Result.success(createdUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
