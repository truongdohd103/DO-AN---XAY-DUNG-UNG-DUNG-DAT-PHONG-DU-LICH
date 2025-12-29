package com.example.chillstay.ui.admin.booking.booking_view

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipStatus

@Immutable
data class BookingViewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val booking: Booking? = null,
    val user: User? = null,
    val hotel: Hotel? = null,
    val vipStatus: VipStatus? = null
) : UiState