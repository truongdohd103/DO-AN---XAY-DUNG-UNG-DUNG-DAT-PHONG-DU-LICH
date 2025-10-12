package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.core.common.Result
import java.util.Date

class GetAvailableVouchersUseCase constructor(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        userId: String? = null,
        hotelId: String? = null
    ): Result<List<Voucher>> {
        return try {
            val now = Date()
            val vouchers = voucherRepository.getVouchers()
                .filter { voucher ->
                    // Filter by status
                    voucher.status == VoucherStatus.ACTIVE &&
                    // Filter by validity period
                    voucher.validFrom.before(now) &&
                    voucher.validTo.after(now) &&
                    // Filter by hotel if specified
                    (hotelId == null || voucher.applyForHotel == null || 
                     voucher.applyForHotel.contains(hotelId))
                }
            
            Result.success(vouchers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


