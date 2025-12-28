package com.example.chillstay.domain.model

import java.time.LocalDate

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val gender: String = "",
    val photoUrl: String = "",
    val dateOfBirth: LocalDate = LocalDate.of(2000, 1, 1),
    val role: String = "user"
)


