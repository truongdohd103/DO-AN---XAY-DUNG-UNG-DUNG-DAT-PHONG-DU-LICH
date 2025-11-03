package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.time.LocalDate

data class VipStatus(
    val id: String = "",
    val userId: String = "",
    val level: VipLevel = VipLevel.BRONZE,
    val points: Int = 0,
    val totalSpent: Double = 0.0,
    val totalBookings: Int = 0,
    val joinDate: LocalDate = LocalDate.now(),
    val lastActivity: Timestamp = Timestamp.now(),
    val benefits: List<VipBenefit> = emptyList(),
    val nextLevelPoints: Int = 0,
    val progressPercentage: Double = 0.0,
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class VipBenefit(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val icon: String = "",
    val isActive: Boolean = true,
    val level: VipLevel = VipLevel.BRONZE
)

enum class VipLevel(val displayName: String, val minPoints: Int, val color: String) {
    BRONZE("Bronze", 0, "#CD853F"),
    SILVER("Silver", 1000, "#C0C0C0"),
    GOLD("Gold", 5000, "#FFD700"),
    PLATINUM("Platinum", 15000, "#E5E4E2"),
    DIAMOND("Diamond", 50000, "#B9F2FF")
}

data class VipStatusHistory(
    val id: String = "",
    val userId: String = "",
    val action: VipAction = VipAction.POINTS_EARNED,
    val pointsChange: Int = 0,
    val description: String = "",
    val bookingId: String? = null,
    val createdAt: Timestamp = Timestamp.now()
)

enum class VipAction {
    POINTS_EARNED,
    POINTS_REDEEMED,
    LEVEL_UP,
    LEVEL_DOWN,
    BOOKING_COMPLETED,
    REVIEW_SUBMITTED,
    REFERRAL_BONUS
}

