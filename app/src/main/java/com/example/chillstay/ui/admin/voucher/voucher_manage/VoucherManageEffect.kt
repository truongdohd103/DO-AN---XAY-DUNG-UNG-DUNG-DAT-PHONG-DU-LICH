package com.example.chillstay.ui.admin.voucher.voucher_manage

import com.example.chillstay.core.base.UiEffect

sealed class VoucherManageEffect : UiEffect {
    data class NavigateToEdit(val voucherId: String) : VoucherManageEffect()
    data object NavigateToCreate : VoucherManageEffect()
    data class ShowSuccess(val message: String) : VoucherManageEffect()
    data class ShowError(val message: String) : VoucherManageEffect()
    data object NavigateBack : VoucherManageEffect()
}