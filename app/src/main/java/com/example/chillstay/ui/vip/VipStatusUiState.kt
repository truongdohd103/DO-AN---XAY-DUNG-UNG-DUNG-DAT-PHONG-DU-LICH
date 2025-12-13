package com.example.chillstay.ui.vip

import androidx.compose.runtime.Immutable
import com.example.chillstay.core.base.UiState
import com.example.chillstay.domain.model.VipStatus
import com.example.chillstay.domain.model.VipBenefit
import com.example.chillstay.domain.model.VipStatusHistory

@Immutable
data class VipStatusUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val vipStatus: VipStatus? = null,
    val benefits: List<VipBenefit> = emptyList(),
    val history: List<VipStatusHistory> = emptyList(),
    val isRefreshing: Boolean = false,
    val showHistory: Boolean = false
) : UiState

