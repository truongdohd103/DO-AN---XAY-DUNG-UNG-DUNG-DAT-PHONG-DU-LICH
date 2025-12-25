package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository

class CreateVoucherUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(voucher: Voucher): Voucher {
        return repository.createVoucher(voucher)
    }
}