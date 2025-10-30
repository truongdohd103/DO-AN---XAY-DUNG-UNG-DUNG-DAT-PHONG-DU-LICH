package com.example.chillstay.ui.voucher

import com.example.chillstay.core.base.UiEvent

sealed class VoucherIntent : UiEvent {
    object LoadVouchers : VoucherIntent()
    data class NavigateToVoucherDetail(val voucherId: String) : VoucherIntent()
    data class RefreshVouchers(val userId: String? = null) : VoucherIntent()
    object ClearError : VoucherIntent()
}

sealed class VoucherDetailIntent : UiEvent {
    data class LoadVoucherDetail(val voucherId: String) : VoucherDetailIntent()
    data class ClaimVoucher(val voucherId: String, val userId: String) : VoucherDetailIntent()
    data class CheckClaimEligibility(val voucherId: String, val userId: String) : VoucherDetailIntent()
    object ClearError : VoucherDetailIntent()
}
