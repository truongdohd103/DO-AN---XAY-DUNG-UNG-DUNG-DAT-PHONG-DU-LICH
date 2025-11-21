package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.time.LocalDate


class UpdateUserProfileUseCase constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(
        userId: String,
        fullName: String? = null,
        gender: String? = null,
        photoUrl: String? = null,
        dateOfBirth: LocalDate? = null
    ): Flow<Result<User>> = flow {
        val existingUser = userRepository.getUser(userId)
            ?: throw IllegalStateException("User not found")

        if (fullName != null && fullName.isBlank()) {
            throw IllegalArgumentException("Full name cannot be empty")
        }

        val allowedGenders = listOf("Male", "Female", "Other")
        if (gender != null && gender.isNotBlank() && gender !in allowedGenders) {
            throw IllegalArgumentException("Invalid gender")
        }

        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("Date of birth cannot be in the future")
        }

        val updatedUser = existingUser.copy(
            fullName = fullName ?: existingUser.fullName,
            gender = gender ?: existingUser.gender,
            photoUrl = photoUrl ?: existingUser.photoUrl,
            dateOfBirth = dateOfBirth ?: existingUser.dateOfBirth
        )

        val result = userRepository.updateUser(updatedUser)
        emit(Result.success(result))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
