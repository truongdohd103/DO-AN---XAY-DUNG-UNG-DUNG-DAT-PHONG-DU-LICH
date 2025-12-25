package com.example.chillstay.ui.admin.voucher.voucher_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus

@Immutable
data class VoucherManageUiState(
    val searchQuery: String = "",
    val activeCount: Int = 0,
    val inactiveCount: Int = 0,
    val vouchers: List<Voucher> = emptyList(),
    val filteredVouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState {
    companion object {
        fun Voucher.isActive(): Boolean = status == VoucherStatus.ACTIVE
    }
}