package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CreateVoucherUseCase(private val voucherRepository: VoucherRepository) {
    operator fun invoke(voucher: Voucher): Flow<Result<String>> = flow {
        val newId = voucherRepository.createVoucher(voucher)
        emit(Result.success(newId))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}