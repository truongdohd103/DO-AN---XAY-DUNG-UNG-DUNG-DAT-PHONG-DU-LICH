package com.example.chillstay.domain.usecase.vip

import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.repository.VipStatusRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.core.common.Result

class CreateVipStatusUseCase constructor(
    private val vipStatusRepository: VipStatusRepository,
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(userId: String): Result<VipStatus> {
        return try {
            // Aggregate from existing bookings (status COMPLETED)
            val completedBookings = bookingRepository.getUserBookings(userId, "COMPLETED")
            val totalSpent = completedBookings.sumOf { it.totalPrice }
            val totalBookings = completedBookings.size

            val level = VipLevel.BRONZE
            val currentMin = level.minPoints
            val nextMin = VipLevel.SILVER.minPoints
            val points = 0
            val progress = 0.0

            val vipStatus = VipStatus(
                userId = userId,
                level = level,
                points = points,
                totalSpent = totalSpent,
                totalBookings = totalBookings,
                joinDate = com.google.firebase.Timestamp.now(),
                benefits = emptyList(),
                nextLevelPoints = nextMin,
                progressPercentage = progress,
                isActive = true
            )
            val createdStatus = vipStatusRepository.createVipStatus(vipStatus)
            Result.success(createdStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

