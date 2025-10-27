package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.util.Date

data class Voucher(
    val id: String = "",
    val code: String = "",
    val title: String = "",
    val description: String = "",
    val type: VoucherType = VoucherType.PERCENTAGE,
    val value: Double = 0.0,
    val status: VoucherStatus = VoucherStatus.ACTIVE,
    val validFrom: Timestamp = Timestamp.now(),
    val validTo: Timestamp = Timestamp.now(),
    val applyForHotel: List<String>? = null, // null means apply for all hotels
    val conditions: VoucherConditions = VoucherConditions(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class VoucherDetail(
    val id: String = "",
    val voucherId: String = "",
    val description: String = "",
    val conditions: String = "",
    val applicableHotels: List<String> = emptyList(),
    val isClaimed: Boolean = false,
    val claimedAt: Timestamp? = null,
    val claimedBy: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)

data class VoucherConditions(
    val minBookingAmount: Double = 0.0,
    val maxDiscountAmount: Double = 0.0,
    val applicableForNewUsers: Boolean = false,
    val applicableForExistingUsers: Boolean = true,
    val maxUsagePerUser: Int = 1,
    val maxTotalUsage: Int = 0, // 0 means unlimited
    val currentUsage: Int = 0,
    val requiredUserLevel: String? = null, // VIP, GOLD, SILVER, etc.
    val validDays: List<String> = emptyList(), // MONDAY, TUESDAY, etc.
    val validTimeSlots: List<String> = emptyList() // MORNING, AFTERNOON, EVENING
)

enum class VoucherType {
    PERCENTAGE,
    FIXED_AMOUNT
}

enum class VoucherStatus {
    ACTIVE,
    INACTIVE,
    EXPIRED
}