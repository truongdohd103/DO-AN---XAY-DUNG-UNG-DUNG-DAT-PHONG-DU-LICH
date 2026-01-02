package com.example.chillstay.ui.admin.customer.review_view

import com.example.chillstay.core.base.UiEffect

sealed interface ReviewViewEffect : UiEffect {
    object NavigateBack : ReviewViewEffect
    data class ShowError(val message: String) : ReviewViewEffect
}