package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.repository.VoucherRepository

class UpdateVoucherStatusUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String, isActive: Boolean): Voucher? {
        val voucher = repository.getVoucherById(voucherId)
        return if (voucher != null) {
            val updatedVoucher = voucher.copy(
                status = if (isActive) VoucherStatus.ACTIVE else VoucherStatus.INACTIVE
            )
            repository.updateVoucher(updatedVoucher)
            updatedVoucher
        } else {
            null
        }
    }
}