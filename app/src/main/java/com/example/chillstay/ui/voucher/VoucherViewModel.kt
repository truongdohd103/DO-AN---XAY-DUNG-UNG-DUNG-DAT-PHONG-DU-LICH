package com.example.chillstay.ui.voucher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.GetVoucherByIdUseCase
import com.example.chillstay.domain.usecase.voucher.ClaimVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.CheckVoucherEligibilityUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import java.util.Date

class VoucherViewModel(
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase,
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val claimVoucherUseCase: ClaimVoucherUseCase,
    private val checkVoucherEligibilityUseCase: CheckVoucherEligibilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: VoucherIntent) {
        when (intent) {
            is VoucherIntent.LoadVouchers -> {
                loadVouchers()
            }
            is VoucherIntent.LoadVoucherDetail -> {
                // This will be handled by VoucherDetailViewModel
                Log.d("VoucherViewModel", "LoadVoucherDetail handled by VoucherDetailViewModel: ${intent.voucherId}")
            }
            is VoucherIntent.ClaimVoucher -> {
                // This will be handled by VoucherDetailViewModel
                Log.d("VoucherViewModel", "ClaimVoucher handled by VoucherDetailViewModel: ${intent.voucherId}")
            }
            is VoucherIntent.NavigateToVoucherDetail -> {
                // Navigation will be handled by the UI
                Log.d("VoucherViewModel", "Navigate to voucher detail: ${intent.voucherId}")
            }
            is VoucherIntent.RefreshVouchers -> {
                refreshVouchers(intent.userId)
            }
            is VoucherIntent.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            Log.d("VoucherViewModel", "Loading vouchers...")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val result = getAvailableVouchersUseCase(userId = userId)
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("VoucherViewModel", "Successfully loaded ${result.data.size} vouchers")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            vouchers = result.data
                        )
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VoucherViewModel", "Error loading vouchers: ${result.throwable.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Failed to load vouchers"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Exception loading vouchers: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun refreshVouchers(userId: String?) {
        viewModelScope.launch {
            Log.d("VoucherViewModel", "Refreshing vouchers...")
            _uiState.value = _uiState.value.copy(isRefreshing = true, error = null)
            
            try {
                val result = getAvailableVouchersUseCase(userId = userId)
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("VoucherViewModel", "Successfully refreshed ${result.data.size} vouchers")
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            vouchers = result.data
                        )
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VoucherViewModel", "Error refreshing vouchers: ${result.throwable.message}")
                        _uiState.value = _uiState.value.copy(
                            isRefreshing = false,
                            error = result.throwable.message ?: "Failed to refresh vouchers"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Exception refreshing vouchers: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

class VoucherDetailViewModel(
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val claimVoucherUseCase: ClaimVoucherUseCase,
    private val checkVoucherEligibilityUseCase: CheckVoucherEligibilityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoucherDetailUiState())
    val uiState: StateFlow<VoucherDetailUiState> = _uiState.asStateFlow()

    fun handleIntent(intent: VoucherDetailIntent) {
        when (intent) {
            is VoucherDetailIntent.LoadVoucherDetail -> {
                loadVoucherDetail(intent.voucherId)
            }
            is VoucherDetailIntent.ClaimVoucher -> {
                claimVoucher(intent.voucherId, intent.userId)
            }
            is VoucherDetailIntent.CheckClaimEligibility -> {
                checkClaimEligibility(intent.voucherId, intent.userId)
            }
            is VoucherDetailIntent.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadVoucherDetail(voucherId: String) {
        viewModelScope.launch {
            Log.d("VoucherDetailViewModel", "Loading voucher detail for ID: $voucherId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = getVoucherByIdUseCase(voucherId)
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val voucher = result.data
                        Log.d("VoucherDetailViewModel", "Successfully loaded voucher: ${voucher?.title}")
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            voucher = voucher,
                            voucherDetail = null,
                            applicableHotels = voucher?.applyForHotel ?: emptyList()
                        )
                        
                        // Check eligibility after loading
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            checkClaimEligibility(voucherId, userId)
                        }
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VoucherDetailViewModel", "Error loading voucher: ${result.throwable.message}")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Failed to load voucher"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherDetailViewModel", "Exception loading voucher: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun claimVoucher(voucherId: String, userId: String) {
        viewModelScope.launch {
            Log.d("VoucherDetailViewModel", "Claiming voucher: $voucherId for user: $userId")
            _uiState.value = _uiState.value.copy(isClaiming = true, error = null)
            
            try {
                val result = claimVoucherUseCase(voucherId, userId)
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("VoucherDetailViewModel", "Successfully claimed voucher")
                        _uiState.value = _uiState.value.copy(
                            isClaiming = false,
                            isClaimed = true,
                            claimSuccess = true
                        )
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VoucherDetailViewModel", "Error claiming voucher: ${result.throwable.message}")
                        _uiState.value = _uiState.value.copy(
                            isClaiming = false,
                            error = result.throwable.message ?: "Failed to claim voucher"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherDetailViewModel", "Exception claiming voucher: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isClaiming = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun checkClaimEligibility(voucherId: String, userId: String) {
        viewModelScope.launch {
            Log.d("VoucherDetailViewModel", "Checking claim eligibility for voucher: $voucherId, user: $userId")
            
            try {
                val result = checkVoucherEligibilityUseCase(voucherId, userId)
                
                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val eligibility = result.data
                        Log.d("VoucherDetailViewModel", "Eligibility check result: $eligibility")
                        _uiState.value = _uiState.value.copy(
                            isEligible = eligibility.first,
                            eligibilityMessage = eligibility.second
                        )
                    }
                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e("VoucherDetailViewModel", "Error checking eligibility: ${result.throwable.message}", result.throwable)
                        
                        // Check if error is PERMISSION_DENIED for graceful fallback
                        val errorMessage = result.throwable.message ?: ""
                        if (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("permission")) {
                            Log.w("VoucherDetailViewModel", "PERMISSION_DENIED detected - using graceful fallback")
                            _uiState.value = _uiState.value.copy(
                                isEligible = true,
                                eligibilityMessage = "Temporary issue - Try claiming anyway"
                            )
                        } else {
                            // Fallback: Basic eligibility check without complex logic
                            val voucher = _uiState.value.voucher
                            if (voucher != null) {
                                val now = Date()
                                val isValid = voucher.status == com.example.chillstay.domain.model.VoucherStatus.ACTIVE &&
                                        voucher.validFrom.toDate().before(now) &&
                                        voucher.validTo.toDate().after(now)
                                
                                if (isValid) {
                                    _uiState.value = _uiState.value.copy(
                                        isEligible = true,
                                        eligibilityMessage = "You are eligible to claim this voucher"
                                    )
                                } else {
                                    _uiState.value = _uiState.value.copy(
                                        isEligible = false,
                                        eligibilityMessage = "Voucher is not valid or has expired"
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isEligible = false,
                                    eligibilityMessage = "Unable to check eligibility"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherDetailViewModel", "Exception checking eligibility: ${e.message}", e)
                
                // Check if exception is PERMISSION_DENIED for graceful fallback
                val errorMessage = e.message ?: ""
                if (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("permission")) {
                    Log.w("VoucherDetailViewModel", "PERMISSION_DENIED exception detected - using graceful fallback")
                    _uiState.value = _uiState.value.copy(
                        isEligible = true,
                        eligibilityMessage = "Temporary issue - Try claiming anyway"
                    )
                } else {
                    // Fallback: Basic eligibility check
                    val voucher = _uiState.value.voucher
                    if (voucher != null) {
                        val now = Date()
                        val isValid = voucher.status == com.example.chillstay.domain.model.VoucherStatus.ACTIVE &&
                                voucher.validFrom.toDate().before(now) &&
                                voucher.validTo.toDate().after(now)
                        
                        _uiState.value = _uiState.value.copy(
                            isEligible = isValid,
                            eligibilityMessage = if (isValid) "You are eligible to claim this voucher" else "Voucher is not valid or has expired"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isEligible = false,
                            eligibilityMessage = "Unable to check eligibility"
                        )
                    }
                }
            }
        }
    }

    private fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun formatVoucherConditions(conditions: com.example.chillstay.domain.model.VoucherConditions): String {
        val conditionsList = mutableListOf<String>()
        
        if (conditions.minBookingAmount > 0) {
            conditionsList.add("Minimum booking amount: $${conditions.minBookingAmount}")
        }
        
        if (conditions.maxDiscountAmount > 0) {
            conditionsList.add("Maximum discount: $${conditions.maxDiscountAmount}")
        }
        
        if (conditions.maxUsagePerUser > 0) {
            conditionsList.add("Usage limit per user: ${conditions.maxUsagePerUser}")
        }
        
        if (conditions.requiredUserLevel != null) {
            conditionsList.add("Required user level: ${conditions.requiredUserLevel}")
        }
        
        if (conditions.validDays.isNotEmpty()) {
            conditionsList.add("Valid days: ${conditions.validDays.joinToString(", ")}")
        }
        
        return if (conditionsList.isEmpty()) {
            "No special conditions"
        } else {
            conditionsList.joinToString("\n")
        }
    }
}
