package com.example.chillstay.ui.vip

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.vip.GetVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.GetVipBenefitsUseCase
import com.example.chillstay.domain.usecase.vip.GetVipStatusHistoryUseCase
import com.example.chillstay.domain.usecase.vip.CreateVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.UpdateVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.AddVipStatusHistoryUseCase
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.model.BookingStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.update
import android.util.Log

class VipStatusViewModel(
    private val getVipStatusUseCase: GetVipStatusUseCase,
    private val getVipBenefitsUseCase: GetVipBenefitsUseCase,
    private val getVipStatusHistoryUseCase: GetVipStatusHistoryUseCase,
    private val createVipStatusUseCase: CreateVipStatusUseCase,
    private val updateVipStatusUseCase: UpdateVipStatusUseCase,
    private val getUserBookingsUseCase: GetUserBookingsUseCase,
    private val addVipStatusHistoryUseCase: AddVipStatusHistoryUseCase
) : BaseViewModel<VipStatusUiState, VipStatusIntent, VipStatusEffect>(VipStatusUiState()) {

    init {
        loadVipStatus()
    }

    override fun onEvent(event: VipStatusIntent) = when (event) {
        is VipStatusIntent.LoadVipStatus -> loadVipStatus()
        is VipStatusIntent.RefreshVipStatus -> refreshVipStatus()
        is VipStatusIntent.ToggleHistory -> toggleHistory()
        is VipStatusIntent.ClearError -> clearError()
    }

    private fun loadVipStatus() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w("VipStatusViewModel", "No authenticated user found")
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                Log.d("VipStatusViewModel", "Loading VIP status for user: $currentUserId")
                
                // Load VIP status
                val vipStatusResult = getVipStatusUseCase(currentUserId)
                when (vipStatusResult) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val vipStatus = vipStatusResult.data
                        if (vipStatus != null) {
                            Log.d("VipStatusViewModel", "VIP status loaded: ${vipStatus.level}")
                            
                            // Load benefits for current level
                            val benefitsResult = getVipBenefitsUseCase(vipStatus.level)
                            val benefits = when (benefitsResult) {
                                is com.example.chillstay.core.common.Result.Success -> benefitsResult.data
                                is com.example.chillstay.core.common.Result.Error -> emptyList()
                            }
                            
                            // Load history
                            val historyResult = getVipStatusHistoryUseCase(currentUserId)
                            val history = when (historyResult) {
                                is com.example.chillstay.core.common.Result.Success -> historyResult.data
                                is com.example.chillstay.core.common.Result.Error -> emptyList()
                            }
                            
                            // Recompute totals from bookings and update if mismatched
                            val recomputed = recomputeTotalsIfNeeded(currentUserId, vipStatus)

                            _state.update { 
                                it.copy(
                                    isLoading = false,
                                    vipStatus = recomputed,
                                    benefits = benefits,
                                    history = history
                                )
                            }
                            
                            viewModelScope.launch {
                                sendEffect { VipStatusEffect.ShowVipStatusLoaded }
                            }
                        } else {
                            // Create new VIP status if doesn't exist
                            Log.d("VipStatusViewModel", "No VIP status found, creating new one")
                            createVipStatus(currentUserId)
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VipStatusViewModel", "Error loading VIP status: ${vipStatusResult.throwable.message}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = vipStatusResult.throwable.message ?: "Failed to load VIP status"
                            )
                        }
                        viewModelScope.launch {
                            sendEffect { VipStatusEffect.ShowError(vipStatusResult.throwable.message ?: "Failed to load VIP status") }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VipStatusViewModel", "Exception loading VIP status: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                viewModelScope.launch {
                    sendEffect { VipStatusEffect.ShowError(e.message ?: "Unknown error occurred") }
                }
            }
        }
    }

    private fun createVipStatus(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("VipStatusViewModel", "Creating new VIP status for user: $userId")
                val createResult = createVipStatusUseCase(userId)
                
                when (createResult) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val vipStatus = createResult.data
                        Log.d("VipStatusViewModel", "VIP status created successfully")
                        
                        // Load benefits for new level
                        val benefitsResult = getVipBenefitsUseCase(vipStatus.level)
                        val benefits = when (benefitsResult) {
                            is com.example.chillstay.core.common.Result.Success -> benefitsResult.data
                            is com.example.chillstay.core.common.Result.Error -> emptyList()
                        }
                        
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                vipStatus = vipStatus,
                                benefits = benefits,
                                history = emptyList()
                            )
                        }
                        
                        viewModelScope.launch {
                            sendEffect { VipStatusEffect.ShowVipStatusLoaded }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VipStatusViewModel", "Error creating VIP status: ${createResult.throwable.message}")
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                error = createResult.throwable.message ?: "Failed to create VIP status"
                            )
                        }
                        viewModelScope.launch {
                            sendEffect { VipStatusEffect.ShowError(createResult.throwable.message ?: "Failed to create VIP status") }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VipStatusViewModel", "Exception creating VIP status: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                viewModelScope.launch {
                    sendEffect { VipStatusEffect.ShowError(e.message ?: "Unknown error occurred") }
                }
            }
        }
    }

    private fun refreshVipStatus() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            Log.w("VipStatusViewModel", "No authenticated user found for refresh")
            return
        }

        _state.update { it.copy(isRefreshing = true, error = null) }

        viewModelScope.launch {
            try {
                Log.d("VipStatusViewModel", "Refreshing VIP status for user: $currentUserId")
                
                // Load VIP status
                val vipStatusResult = getVipStatusUseCase(currentUserId)
                when (vipStatusResult) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val vipStatus = vipStatusResult.data
                        if (vipStatus != null) {
                            Log.d("VipStatusViewModel", "VIP status refreshed: ${vipStatus.level}")
                            
                            // Load benefits for current level
                            val benefitsResult = getVipBenefitsUseCase(vipStatus.level)
                            val benefits = when (benefitsResult) {
                                is com.example.chillstay.core.common.Result.Success -> benefitsResult.data
                                is com.example.chillstay.core.common.Result.Error -> emptyList()
                            }
                            
                            // Load history
                            val historyResult = getVipStatusHistoryUseCase(currentUserId)
                            val history = when (historyResult) {
                                is com.example.chillstay.core.common.Result.Success -> historyResult.data
                                is com.example.chillstay.core.common.Result.Error -> emptyList()
                            }
                            
                            val recomputed = recomputeTotalsIfNeeded(currentUserId, vipStatus)

                            _state.update { 
                                it.copy(
                                    isRefreshing = false,
                                    vipStatus = recomputed,
                                    benefits = benefits,
                                    history = history
                                )
                            }
                        } else {
                            _state.update { it.copy(isRefreshing = false) }
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VipStatusViewModel", "Error refreshing VIP status: ${vipStatusResult.throwable.message}")
                        _state.update { 
                            it.copy(
                                isRefreshing = false,
                                error = vipStatusResult.throwable.message ?: "Failed to refresh VIP status"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VipStatusViewModel", "Exception refreshing VIP status: ${e.message}", e)
                _state.update { 
                    it.copy(
                        isRefreshing = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun toggleHistory() {
        _state.update { it.copy(showHistory = !it.showHistory) }
        viewModelScope.launch {
            sendEffect { VipStatusEffect.ShowHistoryToggled }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private suspend fun recomputeTotalsIfNeeded(
        userId: String,
        current: com.example.chillstay.domain.model.VipStatus
    ): com.example.chillstay.domain.model.VipStatus {
        return try {
            // Query history và tổng điểm
            val historyResult = getVipStatusHistoryUseCase(userId)
            val history = when (historyResult) {
                is com.example.chillstay.core.common.Result.Success -> historyResult.data
                is com.example.chillstay.core.common.Result.Error -> emptyList()
            }
            val pointsFromHistory = history.sumOf { it.pointsChange }
            // Do not downgrade user if stored points are higher than history sum
            val points = kotlin.math.max(pointsFromHistory, current.points)

            // Query bookings
            val bookingsResult = getUserBookingsUseCase(userId, BookingStatus.COMPLETED.name)
            val completedBookings = when (bookingsResult) {
                is com.example.chillstay.core.common.Result.Success -> bookingsResult.data
                is com.example.chillstay.core.common.Result.Error -> emptyList()
            }
            val totalSpent = completedBookings.sumOf { it.totalPrice }
            val totalBookings = completedBookings.size

            // Determine level based on points
            val newLevel = when {
                points >= com.example.chillstay.domain.model.VipLevel.DIAMOND.minPoints -> com.example.chillstay.domain.model.VipLevel.DIAMOND
                points >= com.example.chillstay.domain.model.VipLevel.PLATINUM.minPoints -> com.example.chillstay.domain.model.VipLevel.PLATINUM
                points >= com.example.chillstay.domain.model.VipLevel.GOLD.minPoints -> com.example.chillstay.domain.model.VipLevel.GOLD
                points >= com.example.chillstay.domain.model.VipLevel.SILVER.minPoints -> com.example.chillstay.domain.model.VipLevel.SILVER
                else -> com.example.chillstay.domain.model.VipLevel.BRONZE
            }
            val isLevelUp = newLevel.ordinal > current.level.ordinal

            val currentMin = newLevel.minPoints
            val nextMin = when (newLevel) {
                com.example.chillstay.domain.model.VipLevel.BRONZE -> com.example.chillstay.domain.model.VipLevel.SILVER.minPoints
                com.example.chillstay.domain.model.VipLevel.SILVER -> com.example.chillstay.domain.model.VipLevel.GOLD.minPoints
                com.example.chillstay.domain.model.VipLevel.GOLD -> com.example.chillstay.domain.model.VipLevel.PLATINUM.minPoints
                com.example.chillstay.domain.model.VipLevel.PLATINUM -> com.example.chillstay.domain.model.VipLevel.DIAMOND.minPoints
                com.example.chillstay.domain.model.VipLevel.DIAMOND -> com.example.chillstay.domain.model.VipLevel.DIAMOND.minPoints
            }

            // UI requires progress as (points / nextLevelMin), not relative to current level min
            val progress = if (newLevel == com.example.chillstay.domain.model.VipLevel.DIAMOND) 1.0
            else points.toDouble() / nextMin.toDouble()

            if (
                totalSpent != current.totalSpent ||
                totalBookings != current.totalBookings ||
                points != current.points ||
                newLevel != current.level ||
                nextMin != current.nextLevelPoints ||
                kotlin.math.abs(progress - current.progressPercentage) > 0.0001
            ) {
                val updated = current.copy(
                    totalSpent = totalSpent,
                    totalBookings = totalBookings,
                    points = points,
                    level = newLevel,
                    nextLevelPoints = nextMin,
                    progressPercentage = progress,
                    updatedAt = com.google.firebase.Timestamp.now()
                )
                when (val update = updateVipStatusUseCase(updated)) {
                    is com.example.chillstay.core.common.Result.Success -> update.data
                    is com.example.chillstay.core.common.Result.Error -> current
                }
                .also { after ->
                    if (isLevelUp) {
                        // Add LEVEL_UPGRADED history entry for audit
                        val history = com.example.chillstay.domain.model.VipStatusHistory(
                            userId = userId,
                            action = com.example.chillstay.domain.model.VipAction.LEVEL_UPGRADED,
                            pointsChange = 0,
                            description = "VIP level upgraded to ${newLevel.name}",
                            bookingId = null,
                            createdAt = com.google.firebase.Timestamp.now()
                        )
                        addVipStatusHistoryUseCase(history)
                    }
                }
            } else current
        } catch (e: Exception) {
            current
        }
    }
}
