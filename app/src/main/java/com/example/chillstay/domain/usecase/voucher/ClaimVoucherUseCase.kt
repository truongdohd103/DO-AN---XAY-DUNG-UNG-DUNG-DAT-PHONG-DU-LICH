package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import android.util.Log

class ClaimVoucherUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(voucherId: String, userId: String): Result<Boolean> {
        return try {
            Log.d("ClaimVoucherUseCase", "Claiming voucher: $voucherId for user: $userId")
            
            // Check eligibility first
            val eligibilityResult = voucherRepository.checkVoucherEligibility(voucherId, userId)
            if (!eligibilityResult.first) {
                Log.d("ClaimVoucherUseCase", "User not eligible: ${eligibilityResult.second}")
                return Result.failure(Exception(eligibilityResult.second))
            }
            
            // Claim the voucher
            val success = voucherRepository.claimVoucher(voucherId, userId)
            if (success) {
                Log.d("ClaimVoucherUseCase", "Successfully claimed voucher: $voucherId")
                Result.success(true)
            } else {
                Log.e("ClaimVoucherUseCase", "Failed to claim voucher: $voucherId")
                Result.failure(Exception("Failed to claim voucher"))
            }
        } catch (e: Exception) {
            Log.e("ClaimVoucherUseCase", "Error claiming voucher: ${e.message}", e)
            Result.failure(e)
        }
    }
}
