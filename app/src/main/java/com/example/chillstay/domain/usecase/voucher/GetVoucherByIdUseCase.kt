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
            Log.d("GetVoucherByIdUseCase", "Getting voucher by ID: $voucherId")
            val voucher = voucherRepository.getVoucherById(voucherId)
            Log.d("GetVoucherByIdUseCase", "Successfully retrieved voucher: ${voucher?.title}")
            Result.success(voucher)
        } catch (e: Exception) {
            Log.e("GetVoucherByIdUseCase", "Error getting voucher by ID: ${e.message}", e)
            Result.failure(e)
        }
    }
}
