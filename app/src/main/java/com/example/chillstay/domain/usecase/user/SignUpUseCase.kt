package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.AuthRepository
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class SignUpUseCase(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(email: String, password: String): Flow<Result<User>> = flow {
        val trimmedEmail = email.trim()
        val userId = authRepository.signUp(trimmedEmail, password)

        userRepository.getUserByEmail(trimmedEmail)?.let { existing ->
            if (existing.id.isNotBlank() && existing.id != userId) {
                userRepository.deleteUser(existing.id)
            }
        }

        val profile = User(
            id = userId,
            email = trimmedEmail,
            password = password,
            fullName = trimmedEmail.substringBefore("@"),
            gender = "Other",
            photoUrl = "",
            dateOfBirth = LocalDate.now().minusYears(18)
        )

        val createdUser = userRepository.createUser(profile)
        emit(Result.success(createdUser))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
