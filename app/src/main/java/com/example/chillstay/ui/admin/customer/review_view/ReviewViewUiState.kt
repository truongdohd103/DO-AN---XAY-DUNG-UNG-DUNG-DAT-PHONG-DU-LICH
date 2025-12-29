package com.example.chillstay.ui.admin.customer.review_view

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.VipStatus

@Immutable
data class ReviewViewUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val review: Review? = null,
    val user: User? = null,
    val hotel: Hotel? = null,
    val vipStatus : VipStatus? = null
) : UiState