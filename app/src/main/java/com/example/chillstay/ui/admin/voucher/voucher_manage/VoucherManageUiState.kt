package com.example.chillstay.ui.admin.voucher.voucher_manage

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
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
    val error: String? = null,

    val currentPage: Int = 1,
    val itemsPerPage: Int = 5
) : UiState {
    fun updateCurrentPage(page: Int) = copy(currentPage = page)

    companion object {
        fun Voucher.isActive(): Boolean = status == VoucherStatus.ACTIVE
    }
    // Computed properties
    val totalPages: Int
        get() = if (vouchers.isEmpty()) 1 else (vouchers.size + itemsPerPage - 1) / itemsPerPage

    val paginatedVouchers: List<Voucher>
        get() {
            val startIndex = (currentPage - 1) * itemsPerPage
            val endIndex = minOf(startIndex + itemsPerPage, vouchers.size)
            return if (startIndex < vouchers.size) {
                vouchers.subList(startIndex, endIndex)
            } else {
                emptyList()
            }
        }

}