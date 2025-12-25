package com.example.chillstay.ui.admin.voucher.voucher_manage

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.usecase.voucher.GetAllVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.UpdateVoucherStatusUseCase
import com.example.chillstay.domain.usecase.voucher.DeleteVoucherUseCase
import kotlinx.coroutines.launch

class VoucherManageViewModel(
    private val getAllVouchersUseCase: GetAllVouchersUseCase,
    private val updateVoucherStatusUseCase: UpdateVoucherStatusUseCase,
    private val deleteVoucherUseCase: DeleteVoucherUseCase
) : BaseViewModel<VoucherManageUiState, VoucherManageIntent, VoucherManageEffect>(
    VoucherManageUiState()
) {
    companion object {
        private const val LOG_TAG = "VoucherManage"
    }

    val uiState = state

    init {
        loadVouchers()
    }

    override fun onEvent(event: VoucherManageIntent) {
        when (event) {
            VoucherManageIntent.LoadVouchers -> loadVouchers()
            is VoucherManageIntent.UpdateSearchQuery -> updateSearchQuery(event.query)
            is VoucherManageIntent.EditVoucher -> navigateToEdit(event.voucherId)
            is VoucherManageIntent.ToggleVoucherStatus -> toggleVoucherStatus(event.voucherId)
            is VoucherManageIntent.DeleteVoucher -> deleteVoucher(event.voucherId)
            VoucherManageIntent.CreateNewVoucher -> navigateToCreate()
            VoucherManageIntent.NavigateBack -> navigateBack()
            VoucherManageIntent.ClearError -> _state.value = _state.value.copy(error = null)
        }
    }

    private fun loadVouchers() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val vouchers = getAllVouchersUseCase()
                val activeCount = vouchers.count { it.status == VoucherStatus.ACTIVE }
                val inactiveCount = vouchers.count { it.status != VoucherStatus.ACTIVE }

                _state.value = _state.value.copy(
                    vouchers = vouchers,
                    filteredVouchers = vouchers,
                    activeCount = activeCount,
                    inactiveCount = inactiveCount,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error loading vouchers: ${e.message}", e)
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
                sendEffect {
                    VoucherManageEffect.ShowError(
                        e.message ?: "Failed to load vouchers"
                    )
                }
            }
        }
    }

    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        val filtered = if (query.isBlank()) {
            _state.value.vouchers
        } else {
            _state.value.vouchers.filter { voucher ->
                voucher.code.contains(query, ignoreCase = true) ||
                        voucher.title.contains(query, ignoreCase = true) ||
                        voucher.description.contains(query, ignoreCase = true)
            }
        }

        _state.value = _state.value.copy(filteredVouchers = filtered)
    }

    private fun toggleVoucherStatus(voucherId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val voucher = _state.value.vouchers.find { it.id == voucherId }
                if (voucher == null) {
                    _state.value = _state.value.copy(isLoading = false)
                    sendEffect { VoucherManageEffect.ShowError("Voucher not found") }
                    return@launch
                }

                val isCurrentlyActive = voucher.status == VoucherStatus.ACTIVE
                val result = updateVoucherStatusUseCase(voucherId, !isCurrentlyActive)

                if (result != null) {
                    sendEffect {
                        VoucherManageEffect.ShowSuccess(
                            if (isCurrentlyActive) "Voucher deactivated" else "Voucher activated"
                        )
                    }
                    loadVouchers() // Reload to get updated data
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                    sendEffect { VoucherManageEffect.ShowError("Failed to update voucher status") }
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error toggling voucher status: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false)
                sendEffect {
                    VoucherManageEffect.ShowError(
                        e.message ?: "Failed to update voucher status"
                    )
                }
            }
        }
    }

    private fun deleteVoucher(voucherId: String) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)

                val success = deleteVoucherUseCase(voucherId)

                if (success) {
                    sendEffect { VoucherManageEffect.ShowSuccess("Voucher deleted") }
                    loadVouchers() // Reload to get updated data
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                    sendEffect { VoucherManageEffect.ShowError("Failed to delete voucher") }
                }
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error deleting voucher: ${e.message}", e)
                _state.value = _state.value.copy(isLoading = false)
                sendEffect {
                    VoucherManageEffect.ShowError(
                        e.message ?: "Failed to delete voucher"
                    )
                }
            }
        }
    }

    private fun navigateToEdit(voucherId: String) {
        viewModelScope.launch {
            sendEffect { VoucherManageEffect.NavigateToEdit(voucherId) }
        }
    }

    private fun navigateToCreate() {
        viewModelScope.launch {
            sendEffect { VoucherManageEffect.NavigateToCreate }
        }
    }

    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { VoucherManageEffect.NavigateBack }
        }
    }
}