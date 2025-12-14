package com.example.chillstay.ui.auth

sealed interface AuthEffect : com.example.chillstay.core.base.UiEffect {
    data object NavigateToMain : AuthEffect
    data object NavigateToAdminHome : AuthEffect
    data object NavigateToSignIn : AuthEffect
    data object NavigateToAuth : AuthEffect
    data class ShowMessage(val message: String) : AuthEffect
}


