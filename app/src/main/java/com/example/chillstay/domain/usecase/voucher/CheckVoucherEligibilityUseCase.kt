package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import android.util.Log

class CheckVoucherEligibilityUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String, userId: String): Result<Pair<Boolean, String>> {
        return try {
            Log.d("CheckVoucherEligibilityUseCase", "Checking eligibility for voucher: $voucherId, user: $userId")
            val eligibility = voucherRepository.checkVoucherEligibility(voucherId, userId)
            Log.d("CheckVoucherEligibilityUseCase", "Eligibility result: ${eligibility.first} - ${eligibility.second}")
            Result.success(eligibility)
        } catch (e: Exception) {
            Log.e("CheckVoucherEligibilityUseCase", "Error checking eligibility: ${e.message}", e)
            Result.failure(e)
        }
    }
}
