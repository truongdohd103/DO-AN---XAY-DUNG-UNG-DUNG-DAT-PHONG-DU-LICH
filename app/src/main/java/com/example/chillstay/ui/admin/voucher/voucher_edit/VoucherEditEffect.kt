package com.example.chillstay.ui.admin.voucher.voucher_edit

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Voucher

sealed class VoucherEditEffect : UiEffect {
    data class ShowSaveSuccess(val voucher: Voucher) : VoucherEditEffect()
    data class ShowCreateSuccess(val voucher: Voucher) : VoucherEditEffect()
    data class ShowError(val message: String) : VoucherEditEffect()
    data object NavigateBack : VoucherEditEffect()
}