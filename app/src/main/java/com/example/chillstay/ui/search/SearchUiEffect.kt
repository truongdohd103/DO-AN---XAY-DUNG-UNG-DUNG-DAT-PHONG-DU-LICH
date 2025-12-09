package com.example.chillstay.ui.search

import com.example.chillstay.core.base.UiEffect

sealed interface SearchUiEffect : UiEffect {
    data class ShowMessage(val message: String) : SearchUiEffect
}


