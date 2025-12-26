package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import android.util.Log

class GetVoucherByIdUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String): Result<Voucher?> {
        return try {
            val voucher = voucherRepository.getVoucherById(voucherId)
            Result.success(voucher)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
