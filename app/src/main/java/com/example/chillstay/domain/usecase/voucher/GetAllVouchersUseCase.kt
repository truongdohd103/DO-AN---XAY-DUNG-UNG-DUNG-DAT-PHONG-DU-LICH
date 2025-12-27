package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository

class GetAllVouchersUseCase(
    private val repository: VoucherRepository
) {
    suspend operator fun invoke(): List<Voucher> {
        return repository.getVouchers()
    }
}