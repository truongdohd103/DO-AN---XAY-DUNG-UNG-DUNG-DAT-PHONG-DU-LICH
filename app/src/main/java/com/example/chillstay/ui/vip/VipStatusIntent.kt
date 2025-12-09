package com.example.chillstay.ui.vip

import com.example.chillstay.core.base.UiEvent

sealed class VipStatusIntent : UiEvent {
    object LoadVipStatus : VipStatusIntent()
    object RefreshVipStatus : VipStatusIntent()
    object ToggleHistory : VipStatusIntent()
    object ClearError : VipStatusIntent()
}

