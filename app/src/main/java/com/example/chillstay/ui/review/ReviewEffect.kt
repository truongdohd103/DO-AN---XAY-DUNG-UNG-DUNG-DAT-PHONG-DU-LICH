package com.example.chillstay.ui.review

import com.example.chillstay.core.base.UiEffect

sealed interface ReviewEffect : UiEffect {
    data class ShowError(val message: String) : ReviewEffect
    object ShowReviewSubmitted : ReviewEffect
    object NavigateBack : ReviewEffect
}
