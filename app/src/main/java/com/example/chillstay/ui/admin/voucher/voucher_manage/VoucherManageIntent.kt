package com.example.chillstay.ui.admin.voucher.voucher_manage

import com.example.chillstay.core.base.UiEvent

sealed interface VoucherManageIntent : UiEvent {
    data class GoToPage(val page: Int) : VoucherManageIntent
    data object NextPage : VoucherManageIntent
    data object PreviousPage : VoucherManageIntent
    data object LoadVouchers : VoucherManageIntent
    data class UpdateSearchQuery(val query: String) : VoucherManageIntent

    data class EditVoucher(val voucherId: String) : VoucherManageIntent
    data class ToggleVoucherStatus(val voucherId: String) : VoucherManageIntent
    data class DeleteVoucher(val voucherId: String) : VoucherManageIntent

    data object CreateNewVoucher : VoucherManageIntent
    data object NavigateBack : VoucherManageIntent
    data object ClearError : VoucherManageIntent
}