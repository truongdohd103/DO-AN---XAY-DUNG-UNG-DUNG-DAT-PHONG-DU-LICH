package com.example.chillstay.ui.auth

sealed interface AuthUiEvent {
    data class EmailChanged(val value: String) : AuthUiEvent
    data class PasswordChanged(val value: String) : AuthUiEvent
    data class ConfirmPasswordChanged(val value: String) : AuthUiEvent
    data class ProfileFullNameChanged(val value: String) : AuthUiEvent
    data class ProfileGenderChanged(val value: String) : AuthUiEvent
    data class ProfilePhotoUrlChanged(val value: String) : AuthUiEvent
    data class ProfileDateOfBirthChanged(val value: String) : AuthUiEvent

    object SignIn : AuthUiEvent
    object SignUp : AuthUiEvent
    object SignOut : AuthUiEvent
    object LoadProfile : AuthUiEvent
    object SaveProfile : AuthUiEvent
    object ClearMessage : AuthUiEvent
}


