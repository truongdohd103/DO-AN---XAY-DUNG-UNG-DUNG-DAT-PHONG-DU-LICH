package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(private val userRepository: UserRepository) {
    operator fun invoke(): Flow<Result<List<User>>> = flow {
        try {
            val users = userRepository.getAllUsers()
            emit(Result.Success(users))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}