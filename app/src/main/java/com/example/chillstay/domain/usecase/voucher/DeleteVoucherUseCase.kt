package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.repository.VoucherRepository

class DeleteVoucherUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String): Boolean {
        val voucher = repository.getVoucherById(voucherId)
        return if (voucher != null) {
            val deletedVoucher = voucher.copy(status = VoucherStatus.INACTIVE)
            repository.updateVoucher(deletedVoucher)
            true
        } else {
            false
        }
    }
}