package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetVoucherByIdUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    operator fun invoke(voucherId: String): Flow<Result<Voucher>> = flow {
        val voucher = voucherRepository.getVoucherById(voucherId)
            ?: throw IllegalStateException("Voucher not found")
        emit(Result.success(voucher))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}
