package com.example.chillstay.ui.voucher

import com.example.chillstay.core.base.UiEffect

sealed interface VoucherEffect : UiEffect {
    data class ShowError(val message: String) : VoucherEffect
    data class NavigateToVoucherDetail(val voucherId: String) : VoucherEffect
}

sealed interface VoucherDetailEffect : UiEffect {
    data class ShowError(val message: String) : VoucherDetailEffect
    object ShowVoucherClaimed : VoucherDetailEffect
    object RequireAuthentication : VoucherDetailEffect
}
