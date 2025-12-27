package com.example.chillstay.ui.admin.home

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import kotlinx.coroutines.launch

class AdminHomeViewModel() : BaseViewModel<AdminHomeUiState, AdminHomeIntent, AdminHomeEffect>(AdminHomeUiState()) {

    val uiState = state

    override fun onEvent(event: AdminHomeIntent) {
        when (event) {
            is AdminHomeIntent.NavigateToAccommodation -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToAccommodation }
                }
            }
            is AdminHomeIntent.NavigateToVoucher -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToVoucher }
                }
            }
            is AdminHomeIntent.NavigateToCustomer -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToCustomer }
                }
            }
            is AdminHomeIntent.NavigateToNotification -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToNotification }
                }
            }
            is AdminHomeIntent.NavigateToBooking -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToBooking }
                }
            }
            is AdminHomeIntent.NavigateToStatistics -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToStatistics }
                }
            }
            is AdminHomeIntent.ToggleStatistics -> {
                toggleStatistics()
            }
            is AdminHomeIntent.NavigateToPrice -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToPrice }
                }
            }
            is AdminHomeIntent.NavigateToCalendar -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToCalendar }
                }
            }
            is AdminHomeIntent.NavigateToProfile -> {
                viewModelScope.launch {
                    sendEffect { AdminHomeEffect.NavigateToProfile }
                }
            }
            is AdminHomeIntent.ClearError -> {
                clearError()
            }
        }
    }

    private fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    private fun toggleStatistics() {
        _state.value = _state.value.copy(
            isStatisticsExpanded = !_state.value.isStatisticsExpanded
        )
    }
}
