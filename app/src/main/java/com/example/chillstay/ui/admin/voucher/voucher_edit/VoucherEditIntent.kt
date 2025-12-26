package com.example.chillstay.ui.admin.voucher.voucher_edit

import android.net.Uri
import com.example.chillstay.core.base.UiEvent
import com.example.chillstay.domain.model.VoucherType
import java.util.Date

sealed interface VoucherEditIntent : UiEvent {
    data class LoadForEdit(val voucherId: String) : VoucherEditIntent

    // Basic Information
    data class UpdateTitle(val value: String) : VoucherEditIntent
    data class UpdateCode(val value: String) : VoucherEditIntent
    data class UpdateDescription(val value: String) : VoucherEditIntent
    class SetImage(val uri: Uri) : VoucherEditIntent
    data object RemoveImage : VoucherEditIntent

    // Discount Value
    data class SelectType(val type: VoucherType) : VoucherEditIntent
    data class UpdateValue(val value: String) : VoucherEditIntent

    // Validity Period
    data class UpdateValidFrom(val date: Date) : VoucherEditIntent
    data class UpdateValidTo(val date: Date) : VoucherEditIntent

    // Usage Limits
    data class UpdateMaxTotalUsage(val value: String) : VoucherEditIntent
    data class UpdateMaxUsagePerUser(val value: String) : VoucherEditIntent

    // Conditions
    data class UpdateMinBookingAmount(val value: String) : VoucherEditIntent
    data class UpdateMaxDiscountAmount(val value: String) : VoucherEditIntent
    data class UpdateMinNights(val value: String) : VoucherEditIntent

    // Customer Segment
    data class SelectUserLevel(val level: String?) : VoucherEditIntent // null = All

    // Advanced Conditions
    data class ToggleValidDay(val day: String) : VoucherEditIntent
    data class ToggleTimeSlot(val slot: String) : VoucherEditIntent
    data object LoadForCreate : VoucherEditIntent
    data object ToggleStackable : VoucherEditIntent

    data object Create : VoucherEditIntent
    data object Save : VoucherEditIntent
    data object NavigateBack : VoucherEditIntent
    data object ClearError : VoucherEditIntent
}