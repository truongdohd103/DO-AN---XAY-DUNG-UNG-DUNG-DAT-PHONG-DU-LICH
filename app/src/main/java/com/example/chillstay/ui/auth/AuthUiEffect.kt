package com.example.chillstay.ui.auth

sealed interface AuthUiEffect {
    data object NavigateToMain : AuthUiEffect
    data object NavigateToSignIn : AuthUiEffect
    data object NavigateToAuth : AuthUiEffect
    data class ShowMessage(val message: String) : AuthUiEffect
}


