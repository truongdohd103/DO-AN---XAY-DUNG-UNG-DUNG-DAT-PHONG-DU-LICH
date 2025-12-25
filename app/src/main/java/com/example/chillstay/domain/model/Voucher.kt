package com.example.chillstay.domain.model

import com.google.firebase.Timestamp
import java.util.Date
import java.util.concurrent.TimeUnit

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

// Helper để tính số ngày còn lại
fun Voucher.daysLeft(): Long {
    val now = Date()
    val expiryDate = validTo.toDate()
    val diff = expiryDate.time - now.time
    return TimeUnit.MILLISECONDS.toDays(diff)
}

// Helper để format expiry text
fun Voucher.expiryText(): String {
    val days = daysLeft()
    return when {
        days < 0 -> "Expired"
        days == 0L -> "Expires today"
        days == 1L -> "Expires tomorrow"
        else -> "Expires in $days days"
    }
}

// Helper để format time left
fun Voucher.timeLeftText(): String {
    val days = daysLeft()
    return when {
        days < 0 -> "Expired"
        days == 0L -> "Last day"
        else -> "$days days left"
    }
}

// Helper để lấy discount text
fun Voucher.discountText(): String {
    return when (type) {
        VoucherType.PERCENTAGE -> "UP TO\n${value.toInt()}%"
        VoucherType.FIXED_AMOUNT -> "$$${value.toInt()}"
    }
}

// Helper để lấy gradient colors (mặc định theo type)
fun Voucher.gradientColors(): List<String> {
    return when {
        value <= 5 -> listOf("#87CEEB", "#4169E1") // Blue
        value <= 10 -> listOf("#1AB6B6", "#159999") // Teal
        value <= 20 -> listOf("#FFB347", "#FF8C00") // Orange
        else -> listOf("#A855F7", "#9333EA") // Purple
    }
}

// Helper để lấy decorative emojis
fun Voucher.decorativeEmojis(): List<String> {
    return when {
        title.contains("domestic", ignoreCase = true) -> listOf("🌴", "🏨", "☀️")
        title.contains("special", ignoreCase = true) -> listOf("⭐", "✨", "💎")
        else -> listOf("⭐", "✨", "☁️")
    }
}

// Helper để format info text
fun Voucher.infoText(): String {
    val parts = mutableListOf<String>()

    parts.add(expiryText())

    if (conditions.minBookingAmount > 0) {
        parts.add("Minimum spend $${conditions.minBookingAmount.toInt()}")
    }

    parts.add("Promo Code: $code")

    return parts.joinToString(" | ")
}

// Helper để check if active và valid
fun Voucher.isActiveAndValid(): Boolean {
    if (status != VoucherStatus.ACTIVE) return false
    val now = Date()
    return validFrom.toDate().before(now) && validTo.toDate().after(now)
}