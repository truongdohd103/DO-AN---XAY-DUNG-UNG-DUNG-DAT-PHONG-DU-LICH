package com.example.chillstay.ui.admin.customer.customer_view

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.ActivityType
import com.example.chillstay.domain.model.VipLevel
import com.example.chillstay.domain.usecase.user.GetCustomerActivitiesUseCase
import com.example.chillstay.domain.usecase.user.GetCustomerDetailsUseCase
import com.example.chillstay.domain.usecase.vip.GetVipStatusUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CustomerViewViewModel(
    private val getCustomerDetailsUseCase: GetCustomerDetailsUseCase,
    private val getCustomerActivitiesUseCase: GetCustomerActivitiesUseCase,
    private val getVipStatusUseCase: GetVipStatusUseCase
) : BaseViewModel<CustomerViewUiState, CustomerViewIntent, CustomerViewEffect>(
    CustomerViewUiState()
) {

    val uiState = state

    override fun onEvent(event: CustomerViewIntent) {
        when (event) {
            is CustomerViewIntent.LoadCustomerDetails -> {
                loadCustomerDetails(event.userId)
            }
            is CustomerViewIntent.SelectActivityTab -> {
                selectTab(event.tab)
            }
            is CustomerViewIntent.ViewActivity -> {
                viewActivity(event.activityId, event.activityType)
            }
            is CustomerViewIntent.SendNotification -> {
                sendNotification()
            }
            is CustomerViewIntent.AddToBlacklist -> {
                addToBlacklist()
            }
            is CustomerViewIntent.ClearError -> {
                _state.value = _state.value.clearError()
            }
            is CustomerViewIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.updateCurrentPage(event.page)
                }
            }
            is CustomerViewIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.updateCurrentPage(currentPage + 1)
                }
            }
            is CustomerViewIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.updateCurrentPage(currentPage - 1)
                }
            }
        }
    }

    private fun loadCustomerDetails(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                userId = userId,
                isLoading = true
            ).clearError()

            try {
                // Load customer details
                getCustomerDetailsUseCase(userId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.value = _state.value
                                .updateUser(result.data.user)
                                .updateStats(result.data.stats)
                                .copy(isLoading = false)

                            // Load VIP status
                            loadVipStatus(userId)

                            // Load activities
                            loadActivities(userId, null)
                        }
                        is Result.Error -> {
                            _state.value = _state.value
                                .copy(isLoading = false)
                                .updateError(result.throwable.message ?: "Failed to load customer details")
                            sendEffect {
                                CustomerViewEffect.ShowError(
                                    result.throwable.message ?: "Failed to load customer details"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value
                    .copy(isLoading = false)
                    .updateError(e.message ?: "Failed to load customer details")
            }
        }
    }

    private fun loadVipStatus(userId: String) {
        viewModelScope.launch {
            try {
                getVipStatusUseCase(userId).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            result.data?.let { _state.value = _state.value.updateVipLevel(level = it.level) }
                        }
                        is Result.Error -> {
                            // Default to Bronze if can't load VIP status
                            _state.value = _state.value.updateVipLevel(level = VipLevel.BRONZE)
                        }
                    }
                }
            } catch (_: Exception) {
                // Default to Bronze
                _state.value = _state.value.updateVipLevel(VipLevel.BRONZE)
            }
        }
    }

    private fun loadActivities(userId: String, type: String?) {
        viewModelScope.launch {
            // Set loading state for activities specifically
            _state.value = _state.value.updateIsLoadingActivities(true)

            try {
                getCustomerActivitiesUseCase(userId, type).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            _state.value = _state.value
                                .updateAllActivities(result.data)
                                .updateIsLoadingActivities(false)
                            filterActivities()
                        }
                        is Result.Error -> {
                            _state.value = _state.value.updateIsLoadingActivities(false)
                            sendEffect {
                                CustomerViewEffect.ShowError(
                                    result.throwable.message ?: "Failed to load activities"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.updateIsLoadingActivities(false)
                sendEffect {
                    CustomerViewEffect.ShowError(
                        e.message ?: "Failed to load activities"
                    )
                }
            }
        }
    }

    private fun selectTab(tab: ActivityTab) {
        _state.value = _state.value.updateSelectedTab(tab)
        filterActivities()
    }

    private fun filterActivities() {
        val allActivities = _state.value.allActivities
        val selectedTab = _state.value.selectedTab

        val filtered = when (selectedTab) {
            ActivityTab.BOOKING -> allActivities.filter {
                it.type == ActivityType.BOOKING ||
                        it.type == ActivityType.BOOKING_COMPLETED ||
                        it.type == ActivityType.BOOKING_CANCELLED
            }
            ActivityTab.REVIEW -> allActivities.filter {
                it.type == ActivityType.REVIEW
            }
        }

        _state.value = _state.value.updateFilteredActivities(filtered)
    }

    private fun viewActivity(activityId: String, activityType: String) {
        viewModelScope.launch {
            when (activityType) {
                "BOOKING", "BOOKING_COMPLETED", "BOOKING_CANCELLED" -> {
                    sendEffect { CustomerViewEffect.NavigateToBookingDetail(activityId) }
                }
                "REVIEW" -> {
                    sendEffect { CustomerViewEffect.NavigateToReviewDetail(activityId) }
                }
            }
        }
    }

    private fun sendNotification() {
        viewModelScope.launch {
            // TODO: Implement send notification logic
            val userId = _state.value.userId
            sendEffect { CustomerViewEffect.ShowNotificationSent(userId) }
        }
    }

    private fun addToBlacklist() {
        viewModelScope.launch {
            // TODO: Implement add to blacklist logic
            val userId = _state.value.userId
            sendEffect { CustomerViewEffect.ShowBlacklistSuccess(userId) }
        }
    }
}