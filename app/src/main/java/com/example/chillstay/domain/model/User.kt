package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val gender: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val dateOfBirth: LocalDate = LocalDate.of(2000, 1, 1),
    val isActive: Boolean = true,
    val role : UserRole = UserRole.USER,
    val memberSince: Timestamp? = Timestamp.now()
)

enum class UserRole {
    ADMIN,
    USER
}


