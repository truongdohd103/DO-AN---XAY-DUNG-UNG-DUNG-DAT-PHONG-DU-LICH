package com.example.chillstay.ui.admin.voucher.voucher_edit

import android.net.Uri
import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.VoucherType
import com.example.chillstay.domain.model.VoucherStatus
import java.util.Date

@Immutable
data class VoucherEditUiState(
    val mode: Mode = Mode.Create,
    val voucherId: String? = null,

    // Basic Information
    val title: String = "",
    val code: String = "",
    val description: String = "",
    val imageUri: Uri = Uri.EMPTY,
    val isLoadingImage: Boolean = false,

    // Discount Value
    val type: VoucherType = VoucherType.PERCENTAGE,
    val value: String = "",

    // Validity Period
    val validFrom: Date? = null,
    val validTo: Date? = null,

    // Usage Limits
    val maxTotalUsage: String = "", // 0 = unlimited
    val maxUsagePerUser: String = "1",

    // Conditions
    val minBookingAmount: String = "0",
    val maxDiscountAmount: String = "", // Optional
    val minNights: String = "0", // NEW field
    val isStackable: Boolean = true, // NEW field

    // Customer Segment
    val requiredUserLevel: String? = null, // null = All, or "Bronze", "Silver", "Gold"

    // Advanced Conditions
    val validDays: Set<String> = emptySet(), // "MONDAY", "TUESDAY", etc.
    val validTimeSlots: Set<String> = emptySet(), // "MORNING", "AFTERNOON", "EVENING"

    // Status
    val status: VoucherStatus = VoucherStatus.ACTIVE,

    // UI State
    val isSaving: Boolean = false,
    val error: String? = null
) : UiState

enum class Mode {
    Create,
    Edit
}