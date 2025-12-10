package com.example.chillstay.ui.profile

sealed interface ProfileUiEffect : com.example.chillstay.core.base.UiEffect {
    data class ShowMessage(val message: String) : ProfileUiEffect
    data object NavigateToMyReviews : ProfileUiEffect
}

