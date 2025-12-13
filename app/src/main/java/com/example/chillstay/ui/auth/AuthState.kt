package com.example.chillstay.ui.auth

data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val preserveMessage: Boolean = false
) : com.example.chillstay.core.base.UiState
