package com.example.chillstay.domain.usecase.user

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.core.common.Result
import java.time.LocalDate


class UpdateUserProfileUseCase constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        fullName: String? = null,
        gender: String? = null,
        photoUrl: String? = null,
        dateOfBirth: LocalDate? = null
    ): Result<User> {
        return try {
            val existingUser = userRepository.getUser(userId)
            if (existingUser == null) {
                return Result.failure(Exception("User not found"))
            }
            
            // Validate inputs
            if (fullName != null && fullName.isBlank()) {
                return Result.failure(Exception("Full name cannot be empty"))
            }
            
            if (gender != null && !listOf("Male", "Female", "Other").contains(gender)) {
                return Result.failure(Exception("Invalid gender"))
            }
            
            if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
                return Result.failure(Exception("Date of birth cannot be in the future"))
            }
            
            val updatedUser = existingUser.copy(
                fullName = fullName ?: existingUser.fullName,
                gender = gender ?: existingUser.gender,
                photoUrl = photoUrl ?: existingUser.photoUrl,
                dateOfBirth = dateOfBirth ?: existingUser.dateOfBirth
            )
            
            val result = userRepository.updateUser(updatedUser)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
