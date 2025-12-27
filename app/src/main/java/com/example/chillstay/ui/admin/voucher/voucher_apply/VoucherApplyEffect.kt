package com.example.chillstay.ui.admin.voucher.voucher_apply

import com.example.chillstay.core.base.UiEffect

sealed interface VoucherApplyEffect : UiEffect {
    data object NavigateBack : VoucherApplyEffect
    data class ShowSuccess(val message: String) : VoucherApplyEffect
    data class ShowError(val message: String) : VoucherApplyEffect
    data object NavigateToConfirmation : VoucherApplyEffect
}