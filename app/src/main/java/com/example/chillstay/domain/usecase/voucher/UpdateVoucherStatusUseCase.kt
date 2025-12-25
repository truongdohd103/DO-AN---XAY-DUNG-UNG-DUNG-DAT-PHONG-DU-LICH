package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.repository.VoucherRepository

class UpdateVoucherStatusUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String, isActive: Boolean): Voucher? {
        // Get current voucher
        val voucher = repository.getVoucherById(voucherId) ?: return null

        // Update status
        val updatedVoucher = voucher.copy(
            status = if (isActive) VoucherStatus.ACTIVE else VoucherStatus.INACTIVE
        )

        return repository.updateVoucher(updatedVoucher)
    }
}