package com.example.chillstay.ui.voucher

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.GetVoucherByIdUseCase
import com.example.chillstay.domain.usecase.voucher.ClaimVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.CheckVoucherEligibilityUseCase
import com.google.firebase.auth.FirebaseAuth
import com.example.chillstay.core.common.Result
import kotlinx.coroutines.launch
import android.util.Log
import com.example.chillstay.ui.voucher.VoucherDetailEffect.*
import kotlinx.coroutines.flow.first
import java.util.Date

class VoucherViewModel(
    private val getAvailableVouchersUseCase: GetAvailableVouchersUseCase
) : BaseViewModel<VoucherUiState, VoucherIntent, VoucherEffect>(VoucherUiState()) {

    val uiState = state

    override fun onEvent(event: VoucherIntent) {
        when (event) {
            is VoucherIntent.LoadVouchers -> {
                loadVouchers()
            }

            is VoucherIntent.NavigateToVoucherDetail -> {
                viewModelScope.launch {
                    sendEffect { VoucherEffect.NavigateToVoucherDetail(event.voucherId) }
                }
            }

            is VoucherIntent.RefreshVouchers -> {
                refreshVouchers(event.userId)
            }

            is VoucherIntent.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            Log.d("VoucherViewModel", "Loading vouchers...")
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                // Pass userId = null to fetch ALL system vouchers, not just the ones claimed by user
                val result = getAvailableVouchersUseCase(userId = null, ignoreDateValidation = true)

                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d(
                            "VoucherViewModel",
                            "Successfully loaded ${result.data.size} vouchers"
                        )
                        _state.value = _state.value.copy(
                            isLoading = false,
                            vouchers = result.data
                        )
                    }

                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e(
                            "VoucherViewModel",
                            "Error loading vouchers: ${result.throwable.message}"
                        )
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.throwable.message ?: "Failed to load vouchers"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Exception loading vouchers: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun refreshVouchers(userId: String?) {
        viewModelScope.launch {
            Log.d("VoucherViewModel", "Refreshing vouchers...")
            _state.value = _state.value.copy(isRefreshing = true, error = null)

            try {
                // Pass userId = null to fetch ALL system vouchers
                val result = getAvailableVouchersUseCase(userId = null, ignoreDateValidation = true)

                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d(
                            "VoucherViewModel",
                            "Successfully refreshed ${result.data.size} vouchers"
                        )
                        _state.value = _state.value.copy(
                            isRefreshing = false,
                            vouchers = result.data
                        )
                    }

                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e(
                            "VoucherViewModel",
                            "Error refreshing vouchers: ${result.throwable.message}"
                        )
                        _state.value = _state.value.copy(
                            isRefreshing = false,
                            error = result.throwable.message ?: "Failed to refresh vouchers"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherViewModel", "Exception refreshing vouchers: ${e.message}", e)
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}

class VoucherDetailViewModel(
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val claimVoucherUseCase: ClaimVoucherUseCase,
    private val checkVoucherEligibilityUseCase: CheckVoucherEligibilityUseCase
) : BaseViewModel<VoucherDetailUiState, VoucherDetailIntent, VoucherDetailEffect>(
    VoucherDetailUiState()
) {

    val uiState = state

    override fun onEvent(event: VoucherDetailIntent) {
        when (event) {
            is VoucherDetailIntent.LoadVoucherDetail -> {
                loadVoucherDetail(event.voucherId)
            }

            is VoucherDetailIntent.ClaimVoucher -> {
                claimVoucher(event.voucherId, event.userId)
            }

            is VoucherDetailIntent.CheckClaimEligibility -> {
                checkClaimEligibility(event.voucherId, event.userId)
            }

            is VoucherDetailIntent.ClearError -> {
                clearError()
            }
        }
    }

    private fun loadVoucherDetail(voucherId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val result = getVoucherByIdUseCase(voucherId).first()
            when (result) {
                is Result.Success -> {
                    val voucher = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        voucher = voucher,
                        voucherDetail = null
                    )
                }

                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.throwable.message ?: "Failed to load voucher"
                    )
                    sendEffect {
                        ShowError(result.throwable.message ?: "Failed to load voucher")
                    }
                }
            }
        }
    }

    private fun claimVoucher(voucherId: String, userId: String) {
        viewModelScope.launch {
            Log.d("VoucherDetailViewModel", "Claiming voucher: $voucherId for user: $userId")
            _state.value = _state.value.copy(isClaiming = true, error = null)

            try {
                val result = claimVoucherUseCase(voucherId, userId)

                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        Log.d("VoucherDetailViewModel", "Successfully claimed voucher")
                        _state.value = _state.value.copy(
                            isClaiming = false,
                            isClaimed = true,
                            claimSuccess = true
                        )
                        viewModelScope.launch {
                            sendEffect { VoucherDetailEffect.ShowVoucherClaimed }
                        }
                    }

                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e(
                            "VoucherDetailViewModel",
                            "Error claiming voucher: ${result.throwable.message}"
                        )
                        _state.value = _state.value.copy(
                            isClaiming = false,
                            error = result.throwable.message ?: "Failed to claim voucher"
                        )
                        viewModelScope.launch {
                            sendEffect {
                                VoucherDetailEffect.ShowError(
                                    result.throwable.message ?: "Failed to claim voucher"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("VoucherDetailViewModel", "Exception claiming voucher: ${e.message}", e)
                _state.value = _state.value.copy(
                    isClaiming = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun checkClaimEligibility(voucherId: String, userId: String) {
        viewModelScope.launch {
            Log.d(
                "VoucherDetailViewModel",
                "Checking claim eligibility for voucher: $voucherId, user: $userId"
            )

            try {
                val result = checkVoucherEligibilityUseCase(voucherId, userId)

                when (result) {
                    is com.example.chillstay.core.common.Result.Success -> {
                        val eligibility = result.data
                        Log.d("VoucherDetailViewModel", "Eligibility check result: $eligibility")
                        _state.value = _state.value.copy(
                            isEligible = eligibility.first,
                            eligibilityMessage = eligibility.second
                        )
                    }

                    is com.example.chillstay.core.common.Result.Error -> {
                        Log.e(
                            "VoucherDetailViewModel",
                            "Error checking eligibility: ${result.throwable.message}",
                            result.throwable
                        )

                        // Check if error is PERMISSION_DENIED for graceful fallback
                        val errorMessage = result.throwable.message ?: ""
                        if (errorMessage.contains("PERMISSION_DENIED") || errorMessage.contains("permission")) {
                            Log.w(
                                "VoucherDetailViewModel",
                                "PERMISSION_DENIED detected - using graceful fallback"
                            )
                            _state.value = _state.value.copy(
                                isEligible = true,
                                eligibilityMessage = "Temporary issue - Try claiming anyway"
                            )
                        } else {
                            // Fallback: Basic eligibility check without complex logic
                            val voucher = _state.value.voucher
                            if (voucher != null) {
                                val now = Date()
                                val isValid =
                                    voucher.status == com.example.chillstay.domain.model.VoucherStatus.ACTIVE &&
                                            voucher.validFrom.toDate().before(now) &&
                                            voucher.validTo.toDate().after(now)

                                if (isValid) {
                                    _state.value = _state.value.copy(
                                        isEligible = true,
                                        eligibilityMessage = "You are eligible to claim this voucher"
                                    )
                                } else {
                                    _state.value = _state.value.copy(
                                        isEligible = false,
                                        eligibilityMessage = "Voucher is not valid or has expired"
                                    )
                                }
                            } else {
                                _state.value = _state.value.copy(
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
                    Log.w(
                        "VoucherDetailViewModel",
                        "PERMISSION_DENIED exception detected - using graceful fallback"
                    )
                    _state.value = _state.value.copy(
                        isEligible = true,
                        eligibilityMessage = "Temporary issue - Try claiming anyway"
                    )
                } else {
                    // Fallback: Basic eligibility check
                    val voucher = _state.value.voucher
                    if (voucher != null) {
                        val now = Date()
                        val isValid =
                            voucher.status == com.example.chillstay.domain.model.VoucherStatus.ACTIVE &&
                                    voucher.validFrom.toDate().before(now) &&
                                    voucher.validTo.toDate().after(now)

                        _state.value = _state.value.copy(
                            isEligible = isValid,
                            eligibilityMessage = if (isValid) "You are eligible to claim this voucher" else "Voucher is not valid or has expired"
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isEligible = false,
                            eligibilityMessage = "Unable to check eligibility"
                        )
                    }
                }
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
