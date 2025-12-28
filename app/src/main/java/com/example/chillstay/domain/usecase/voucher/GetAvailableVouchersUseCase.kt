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
        totalPrice: Double? = null,
        ignoreDateValidation: Boolean = false
    ): Result<List<Voucher>> {
        return try {
            val now = Date()
            val vouchers = if (userId != null) {
                // If user is logged in, fetch only claimed vouchers
                voucherRepository.getClaimedVouchers(userId)
            } else {
                // Otherwise fetch all active vouchers
                voucherRepository.getVouchers()
            }
            
            val filteredVouchers = vouchers.filter { voucher ->
                // Filter by status
                voucher.status == VoucherStatus.ACTIVE &&
                // Filter by validity period
                (ignoreDateValidation || (voucher.validFrom.toDate().before(now) &&
                voucher.validTo.toDate().after(now))) &&
                // Filter by hotel if specified
                (hotelId == null || voucher.applyForHotel.isNullOrEmpty() || 
                 voucher.applyForHotel.contains(hotelId)) &&
                // Filter by min booking amount if specified
                (totalPrice == null || totalPrice >= voucher.conditions.minBookingAmount) &&
                // Filter used vouchers if userId is provided
                (userId == null || !voucherRepository.isVoucherUsed(voucher.id, userId))
            }
            
            Result.success(filteredVouchers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
