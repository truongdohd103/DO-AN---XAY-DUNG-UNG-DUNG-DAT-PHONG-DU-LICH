package com.example.chillstay.ui.auth

import com.example.chillstay.domain.model.User

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val profileFullName: String = "",
    val profileGender: String = "",
    val profilePhotoUrl: String = "",
    val profileDateOfBirth: String = "",
    val isProfileLoading: Boolean = false,
    val profileMessage: String? = null,
    val preserveMessage: Boolean = false
)


