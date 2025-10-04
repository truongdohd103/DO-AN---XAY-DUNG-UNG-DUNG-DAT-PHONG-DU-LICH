package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result
import java.time.Instant


class ApplyVoucherToBookingUseCase constructor(
    private val voucherRepository: VoucherRepository,
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        bookingId: String,
        voucherCode: String
    ): Result<Double> { // Returns discount amount
        return try {
            val booking = bookingRepository.getBookingById(bookingId)
            if (booking == null) {
                return Result.failure(Exception("Booking not found"))
            }
            
            if (booking.status != "PENDING") {
                return Result.failure(Exception("Cannot apply voucher to non-pending booking"))
            }
            
            val voucher = voucherRepository.getVoucherByCode(voucherCode)
            if (voucher == null) {
                return Result.failure(Exception("Voucher not found"))
            }
            
            val now = Instant.now()
            if (voucher.status != "ACTIVE") {
                return Result.failure(Exception("Voucher is not active"))
            }
            
            if (voucher.validFrom.isAfter(now) || voucher.validTo.isBefore(now)) {
                return Result.failure(Exception("Voucher is not valid at this time"))
            }
            
            // Check if voucher applies to this hotel
            if (voucher.applyForHotel.isNotEmpty()) {
                val hotelId = bookingRepository.getBookingHotelId(bookingId)
                if (hotelId == null || !voucher.applyForHotel.any { it.id == hotelId }) {
                    return Result.failure(Exception("Voucher does not apply to this hotel"))
                }
            }
            
            // Calculate discount amount
            val discountAmount = when (voucher.type) {
                "PERCENTAGE" -> booking.price * (voucher.value / 100.0)
                "FIXED" -> voucher.value
                else -> 0.0
            }
            
            // Ensure discount doesn't exceed booking price
            val finalDiscount = minOf(discountAmount, booking.price)
            
            Result.success(finalDiscount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
