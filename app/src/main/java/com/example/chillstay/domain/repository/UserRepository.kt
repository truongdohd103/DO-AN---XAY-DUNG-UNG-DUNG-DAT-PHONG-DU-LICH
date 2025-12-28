package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.domain.model.User

interface UserRepository {
    suspend fun getAllUsers(): List<User>
    suspend fun getUserById(id: String): User?
    suspend fun createUser(user: User): User
    suspend fun updateUser(user: User): User
    suspend fun deleteUser(id: String)
    suspend fun getUserByEmail(email: String): User?
    suspend fun getCustomerStats(userId: String): CustomerStats
}


