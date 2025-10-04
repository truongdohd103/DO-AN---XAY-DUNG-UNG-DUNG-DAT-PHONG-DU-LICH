package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import java.time.Instant


class GetAvailableVouchersUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        userId: String? = null,
        hotelId: String? = null
    ): Result<List<Voucher>> {
        return try {
            val now = Instant.now()
            val vouchers = voucherRepository.getVouchers()
                .filter { voucher ->
                    // Filter by status
                    voucher.status == "ACTIVE" &&
                    // Filter by validity period
                    voucher.validFrom.isBefore(now) &&
                    voucher.validTo.isAfter(now) &&
                    // Filter by hotel if specified
                    (hotelId == null || voucher.applyForHotel.isEmpty() || 
                     voucher.applyForHotel.any { it.id == hotelId })
                }
            
            Result.success(vouchers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
