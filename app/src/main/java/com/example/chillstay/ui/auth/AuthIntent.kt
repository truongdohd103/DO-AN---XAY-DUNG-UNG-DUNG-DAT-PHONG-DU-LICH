package com.example.chillstay.ui.auth

sealed interface AuthIntent : com.example.chillstay.core.base.UiEvent {
    data class EmailChanged(val value: String) : AuthIntent
    data class PasswordChanged(val value: String) : AuthIntent
    data class ConfirmPasswordChanged(val value: String) : AuthIntent

    object SignIn : AuthIntent
    object SignUp : AuthIntent
    object SignOut : AuthIntent
    object ClearMessage : AuthIntent
}
