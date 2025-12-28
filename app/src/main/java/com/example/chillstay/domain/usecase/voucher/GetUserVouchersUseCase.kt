package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetUserVouchersUseCase(
    private val voucherRepository: VoucherRepository
) {
    operator fun invoke(userId: String): Flow<Result<List<Voucher>>> = flow {
        try {
            // Fetch claimed vouchers from repository
            val vouchers = voucherRepository.getClaimedVouchers(userId)
            emit(Result.success(vouchers))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
