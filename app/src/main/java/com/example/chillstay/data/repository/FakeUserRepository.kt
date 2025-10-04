package com.example.chillstay.data.repository

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import java.time.LocalDate

class FakeUserRepository : UserRepository {
    
    private val users = mutableListOf(
        User(
            id = "1",
            email = "demo@chillstay.com",
            password = "demo123",
            fullName = "Demo User",
            gender = "Male",
            photoUrl = "",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )
    )
    
    override suspend fun getUser(id: String): User? {
        return users.find { it.id == id }
    }
    
    override suspend fun createUser(user: User): User {
        val newUser = user.copy(id = (users.size + 1).toString())
        users.add(newUser)
        return newUser
    }
    
    override suspend fun updateUser(user: User): User {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
        return user
    }
    
    override suspend fun deleteUser(id: String) {
        users.removeAll { it.id == id }
    }
    
    override suspend fun getUserByEmail(email: String): User? {
        return users.find { it.email == email }
    }
    
    // Helper method for authentication
    suspend fun authenticateUser(email: String, password: String): User? {
        return users.find { it.email == email && it.password == password }
    }
    
    suspend fun isEmailExists(email: String): Boolean {
        return users.any { it.email == email }
    }
}
