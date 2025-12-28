package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result

class GetUserVouchersUseCase(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(userId: String): Result<List<Voucher>> {
        return try {
            val vouchers = voucherRepository.getUserVouchers(userId)
            Result.success(vouchers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
