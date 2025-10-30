package com.example.chillstay.ui.voucher

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherDetail

@Immutable
data class VoucherUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vouchers: List<Voucher> = emptyList(),
    val isRefreshing: Boolean = false
) : UiState

@Immutable
data class VoucherDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val voucher: Voucher? = null,
    val voucherDetail: VoucherDetail? = null,
    val applicableHotels: List<String> = emptyList(),
    val isClaiming: Boolean = false,
    val isClaimed: Boolean = false,
    val isEligible: Boolean = false,
    val eligibilityMessage: String = "",
    val claimSuccess: Boolean = false
) : UiState
