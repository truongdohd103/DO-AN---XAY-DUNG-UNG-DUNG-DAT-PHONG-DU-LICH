package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.repository.VoucherRepository

class DeleteVoucherUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String): Boolean {
        // Get voucher first to check if exists
        val voucher = repository.getVoucherById(voucherId) ?: return false

        // Set status to INACTIVE instead of actual delete (soft delete)
        val deletedVoucher = voucher.copy(
            status = VoucherStatus.INACTIVE
        )

        val result = repository.updateVoucher(deletedVoucher)
        return result.id == voucherId
    }
}