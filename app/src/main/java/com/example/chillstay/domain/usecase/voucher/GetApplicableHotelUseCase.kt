package com.example.chillstay.domain.usecase.voucher

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelStatus
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class GetApplicableHotelsUseCase(
    private val voucherRepository: VoucherRepository,
    private val hotelRepository: HotelRepository
) {
    operator fun invoke(voucherId: String): Flow<Result<List<Hotel>>> = flow {
        // Get voucher details
        val voucher = voucherRepository.getVoucherById(voucherId)
            ?: throw IllegalStateException("Voucher not found")

        // Get all hotels
        val allHotels = hotelRepository.getHotels()

        // Filter hotels based on voucher's applyForHotel list
        val applicableHotels = if (voucher.applyForHotel == null) {
            // null means apply for all hotels
            allHotels.filter { it.status == HotelStatus.ACTIVE }
        } else {
            // Filter by hotel IDs specified in voucher
            allHotels.filter { hotel ->
                hotel.status == HotelStatus.ACTIVE &&
                        voucher.applyForHotel.contains(hotel.id)
            }
        }

        emit(Result.success(applicableHotels))
    }.catch { throwable ->
        emit(Result.failure(throwable))
    }
}