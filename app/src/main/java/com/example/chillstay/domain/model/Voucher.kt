package com.example.chillstay.domain.model

import java.util.Date

data class Voucher(
    val id: String,
    val code: String,
    val title: String,
    val description: String,
    val type: VoucherType,
    val value: Double,
    val status: VoucherStatus,
    val validFrom: Date,
    val validTo: Date,
    val applyForHotel: List<String>? = null, // null means apply for all hotels
    val createdAt: Date,
    val updatedAt: Date
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