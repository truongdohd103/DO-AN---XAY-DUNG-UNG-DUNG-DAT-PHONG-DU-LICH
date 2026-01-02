package com.example.chillstay.domain.usecase.user

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class CustomerDetails(
    val user: User,
    val stats: CustomerStats
)

class GetCustomerDetailsUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(userId: String): Flow<Result<CustomerDetails>> = flow {
        try {
            val user = userRepository.getUserById(userId)
            if (user != null) {
                val stats = userRepository.getCustomerStats(userId)
                emit(Result.Success(CustomerDetails(user, stats)))
            } else {
                emit(Result.Error(Exception("User not found")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
}