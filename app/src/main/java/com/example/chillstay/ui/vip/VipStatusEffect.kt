package com.example.chillstay.ui.vip

import com.example.chillstay.core.base.UiEffect

sealed class VipStatusEffect : UiEffect {
    data class ShowError(val message: String) : VipStatusEffect()
    object ShowVipStatusLoaded : VipStatusEffect()
    object ShowHistoryToggled : VipStatusEffect()
}

