package com.example.chillstay.domain.usecase.voucher

import android.util.Log
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.VoucherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ApplyVoucherToHotelsUseCase(
    private val voucherRepository: VoucherRepository
) {
    companion object {
        private const val TAG = "AccommodationSelect"
    }

    operator fun invoke(voucherId: String, hotelIds: List<String>): Flow<Result<Boolean>> = flow {
        Log.d(TAG, "=== START ApplyVoucherToHotelsUseCase ===")
        Log.d(TAG, "voucherId: $voucherId")
        Log.d(TAG, "hotelIds: $hotelIds (count: ${hotelIds.size})")

        // Validate inputs
        if (hotelIds.isEmpty()) {
            Log.e(TAG, "❌ Validation failed: No hotels selected")
            throw IllegalArgumentException("No hotels selected")
        }

        Log.d(TAG, "✅ Validation passed")

        // Step 1: Create application record
        Log.d(TAG, "Step 1: Creating voucher application record...")
        val applicationResult = voucherRepository.applyVoucherToHotels(
            voucherId = voucherId,
            hotelIds = hotelIds)

        Log.d(TAG, "Step 1 result: $applicationResult")

        if (!applicationResult) {
            Log.e(TAG, "❌ Step 1 failed: Could not create application record")
            throw IllegalStateException("Failed to create voucher application")
        }

        Log.d(TAG, "✅ Step 1 success: Application record created")

        // Step 2: Update voucher's applyForHotel list
        Log.d(TAG, "Step 2: Updating voucher's applyForHotel list...")
        val updateResult = voucherRepository.updateVoucherAppliedHotels(
            voucherId = voucherId,
            hotelIds = hotelIds
        )

        Log.d(TAG, "Step 2 result: $updateResult")

        if (!updateResult) {
            Log.e(TAG, "❌ Step 2 failed: Could not update applyForHotel")
            throw IllegalStateException("Failed to update voucher applied hotels list")
        }

        Log.d(TAG, "✅ Step 2 success: applyForHotel updated")
        Log.d(TAG, "✅✅✅ UseCase completed successfully!")
        Log.d(TAG, "=== END ApplyVoucherToHotelsUseCase ===")

        emit(Result.success(true))
    }.catch { throwable ->
        Log.e(TAG, "❌❌❌ UseCase failed with exception: ${throwable.message}", throwable)
        emit(Result.failure(throwable))
    }
}