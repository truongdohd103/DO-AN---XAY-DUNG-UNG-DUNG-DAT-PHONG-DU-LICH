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
import kotlinx.coroutines.flow.first
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

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

                // Lấy kết quả VIP status (flow -> lấy emission đầu)
                val vipStatusResult = getVipStatusUseCase(currentUserId).first()

                when (vipStatusResult) {
                    is Result.Success -> {
                        val vipStatus = vipStatusResult.data
                        if (vipStatus != null) {
                            Log.d("VipStatusViewModel", "VIP status loaded: ${vipStatus.level}")

                            // Lấy benefits cho level hiện tại (flow -> lấy emission đầu)
                            val benefitsResult = getVipBenefitsUseCase(vipStatus.level).first()
                            val benefits = when (benefitsResult) {
                                is Result.Success -> benefitsResult.data
                                is Result.Error -> emptyList()
                            }

                            // Lấy lịch sử (flow -> lấy emission đầu)
                            val historyResult = getVipStatusHistoryUseCase(currentUserId).first()
                            val history = when (historyResult) {
                                is Result.Success -> historyResult.data
                                is Result.Error -> emptyList()
                            }

                            // Recompute totals nếu cần (giữ nguyên suspend function)
                            val recomputed = recomputeTotalsIfNeeded(currentUserId, vipStatus)

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    vipStatus = recomputed,
                                    benefits = benefits,
                                    history = history,
                                    error = null
                                )
                            }

                            // Phát effect báo đã load xong
                            sendEffect { VipStatusEffect.ShowVipStatusLoaded }
                        } else {
                            // Nếu chưa có VIP status -> tạo mới
                            Log.d("VipStatusViewModel", "No VIP status found, creating new one")
                            createVipStatus(currentUserId)
                        }
                    }

                    is Result.Error -> {
                        val msg = vipStatusResult.throwable.message ?: "Failed to load VIP status"
                        Log.e("VipStatusViewModel", "Error loading VIP status: $msg")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = msg
                            )
                        }
                        sendEffect { VipStatusEffect.ShowError(msg) }
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error occurred"
                Log.e("VipStatusViewModel", "Exception loading VIP status: $msg", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = msg
                    )
                }
                sendEffect { VipStatusEffect.ShowError(msg) }
            }
        }
    }
    private fun createVipStatus(userId: String) {
        viewModelScope.launch {
            try {
                Log.d("VipStatusViewModel", "Creating new VIP status for user: $userId")
                val createResult = createVipStatusUseCase(userId)
                
                when (createResult) {
                    is Result.Success -> {
                        val vipStatus = createResult.data
                        Log.d("VipStatusViewModel", "VIP status created successfully")
                        
                        // Load benefits for new level
                        val benefitsResult = getVipBenefitsUseCase(vipStatus.level).first()

                        val benefits = when (benefitsResult) {
                            is Result.Success -> benefitsResult.data
                            is Result.Error -> emptyList()
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

                // 1️⃣ Lấy VIP status (Flow -> first)
                val vipStatusResult = getVipStatusUseCase(currentUserId).first()

                when (vipStatusResult) {
                    is Result.Success -> {
                        val vipStatus = vipStatusResult.data
                        if (vipStatus == null) {
                            _state.update { it.copy(isRefreshing = false) }
                            return@launch
                        }

                        Log.d(
                            "VipStatusViewModel",
                            "VIP status refreshed: ${vipStatus.level}"
                        )

                        coroutineScope {
                            val benefitsDeferred = async {
                                getVipBenefitsUseCase(vipStatus.level).first()
                            }
                            val historyDeferred = async {
                                getVipStatusHistoryUseCase(currentUserId).first()
                            }

                            val benefits = when (val result = benefitsDeferred.await()) {
                                is Result.Success -> result.data
                                is Result.Error -> emptyList()
                            }

                            val history = when (val result = historyDeferred.await()) {
                                is Result.Success -> result.data
                                is Result.Error -> emptyList()
                            }

                            // 3️⃣ Recompute totals nếu cần
                            val recomputed = recomputeTotalsIfNeeded(currentUserId, vipStatus)

                            _state.update {
                                it.copy(
                                    isRefreshing = false,
                                    vipStatus = recomputed,
                                    benefits = benefits,
                                    history = history,
                                    error = null
                                )
                            }
                        }
                    }

                    is Result.Error -> {
                        val msg =
                            vipStatusResult.throwable.message ?: "Failed to refresh VIP status"
                        Log.e("VipStatusViewModel", msg)
                        _state.update {
                            it.copy(
                                isRefreshing = false,
                                error = msg
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Unknown error occurred"
                Log.e("VipStatusViewModel", "Exception refreshing VIP status: $msg", e)
                _state.update {
                    it.copy(
                        isRefreshing = false,
                        error = msg
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
            val historyResult = getVipStatusHistoryUseCase(userId).first()
            val history = when (historyResult) {
                is Result.Success -> historyResult.data
                is Result.Error -> emptyList()
            }
            val pointsFromHistory = history.sumOf { it.pointsChange }
            // Do not downgrade user if stored points are higher than history sum
            val points = kotlin.math.max(pointsFromHistory, current.points)

            // Query bookings
            val bookingsResult = getUserBookingsUseCase(userId, BookingStatus.COMPLETED.name)
            val completedBookings = when (bookingsResult) {
                is Result.Success -> bookingsResult.data
                is Result.Error -> emptyList()
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
        } catch (_: Exception) {
            current
        }
    }
}
