package com.example.chillstay.ui.voucher

sealed class VoucherIntent {
    object LoadVouchers : VoucherIntent()
    data class LoadVoucherDetail(val voucherId: String) : VoucherIntent()
    data class ClaimVoucher(val voucherId: String) : VoucherIntent()
    data class NavigateToVoucherDetail(val voucherId: String) : VoucherIntent()
    data class RefreshVouchers(val userId: String? = null) : VoucherIntent()
    object ClearError : VoucherIntent()
}

sealed class VoucherDetailIntent {
    data class LoadVoucherDetail(val voucherId: String) : VoucherDetailIntent()
    data class ClaimVoucher(val voucherId: String, val userId: String) : VoucherDetailIntent()
    data class CheckClaimEligibility(val voucherId: String, val userId: String) : VoucherDetailIntent()
    object ClearError : VoucherDetailIntent()
}
