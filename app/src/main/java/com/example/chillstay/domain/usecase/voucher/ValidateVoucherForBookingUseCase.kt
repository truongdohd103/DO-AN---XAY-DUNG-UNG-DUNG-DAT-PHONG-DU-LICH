package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.core.common.Result
import java.util.Date

class ValidateVoucherForBookingUseCase(
    private val voucherRepository: VoucherRepository
) {
    suspend operator fun invoke(
        code: String,
        hotelId: String,
        totalPrice: Double,
        userId: String
    ): Result<Pair<Voucher, Double>> {
        return try {
            val voucher = voucherRepository.getVoucherByCode(code)
            if (voucher == null) {
                return Result.failure(Exception("Voucher not found"))
            }

            // Check status
            if (voucher.status != VoucherStatus.ACTIVE) {
                return Result.failure(Exception("Voucher is not active"))
            }

            // Check dates
            val now = Date()
            if (now.before(voucher.validFrom.toDate())) {
                return Result.failure(Exception("Voucher is not valid yet"))
            }
            if (now.after(voucher.validTo.toDate())) {
                return Result.failure(Exception("Voucher has expired"))
            }

            // Check hotel applicability
            if (voucher.applyForHotel != null && voucher.applyForHotel.isNotEmpty()) {
                if (!voucher.applyForHotel.contains(hotelId)) {
                    return Result.failure(Exception("Voucher is not applicable for this hotel"))
                }
            }

            // Check minimum booking amount
            if (totalPrice < voucher.conditions.minBookingAmount) {
                return Result.failure(Exception("Booking amount must be at least ${voucher.conditions.minBookingAmount}"))
            }
            
            // Check usage limits
            // Check if already used by user
            if (voucherRepository.isVoucherUsed(voucher.id, userId)) {
                return Result.failure(Exception("You have already used this voucher"))
            }
            
            // Note: We ignore "already claimed" for validation during booking as per previous fix, 
            // but we might want to check if the USER has claimed it if it's a claim-required voucher.
            // For now, simple validation logic.

            // Calculate discount
            val discount = when (voucher.type) {
                com.example.chillstay.domain.model.VoucherType.PERCENTAGE -> {
                    val calculated = totalPrice * (voucher.value / 100.0)
                    if (voucher.conditions.maxDiscountAmount > 0) {
                        minOf(calculated, voucher.conditions.maxDiscountAmount)
                    } else {
                        calculated
                    }
                }
                com.example.chillstay.domain.model.VoucherType.FIXED_AMOUNT -> {
                    minOf(voucher.value, totalPrice)
                }
            }

            Result.success(voucher to discount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
