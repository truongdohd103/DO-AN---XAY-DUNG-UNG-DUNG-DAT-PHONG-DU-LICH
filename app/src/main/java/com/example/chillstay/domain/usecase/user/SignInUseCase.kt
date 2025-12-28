package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.AuthRepository
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class SignInUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(email: String, password: String): Flow<Result<User>> = flow {
        val trimmedEmail = email.trim()
        val userId = authRepository.signIn(trimmedEmail, password)
        val profile = ensureProfile(userId, trimmedEmail)
        emit(Result.success(profile))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }

    private suspend fun ensureProfile(userId: String, email: String): User {
        val existingById = userRepository.getUserById(userId)
        if (existingById != null) {
            return existingById
        }

        val existingByEmail = userRepository.getUserByEmail(email)
        if (existingByEmail != null) {
            if (existingByEmail.id.isNotBlank() && existingByEmail.id != userId) {
                userRepository.deleteUser(existingByEmail.id)
            }
            val normalized = existingByEmail.copy(id = userId)
            return userRepository.updateUser(normalized)
        }

        val newUser = User(
            id = userId,
            email = email,
            password = "",
            fullName = email.substringBefore("@"),
            gender = "Other"
        )
        return userRepository.createUser(newUser)
    }
}
