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
        hotelId: String? = null,
        ignoreDateValidation: Boolean = false
    ): Result<List<Voucher>> {
        return try {
            val now = Date()
            val baseVouchers = if (userId != null) {
                voucherRepository.getUserVouchers(userId)
            } else {
                voucherRepository.getVouchers()
            }

            val vouchers = baseVouchers
                .filter { voucher ->
                    // Filter by status
                    voucher.status == VoucherStatus.ACTIVE &&
                    // Filter by validity period (optional)
                    (ignoreDateValidation || (voucher.validFrom.toDate().before(now) &&
                    voucher.validTo.toDate().after(now))) &&
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


