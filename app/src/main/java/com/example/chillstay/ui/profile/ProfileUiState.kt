package com.example.chillstay.ui.profile

import com.example.chillstay.domain.model.User

data class ProfileUiState(
    val currentUser: User? = null,
    val isAuthenticated: Boolean = false,
    val profileFullName: String = "",
    val profileGender: String = "",
    val profilePhotoUrl: String = "",
    val profileDateOfBirth: String = "",
    val isProfileLoading: Boolean = false,
    val profileMessage: String? = null
) : com.example.chillstay.core.base.UiState
