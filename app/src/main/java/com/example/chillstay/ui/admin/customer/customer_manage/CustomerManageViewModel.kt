package com.example.chillstay.ui.admin.customer.customer_manage

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.usecase.user.GetAllUsersUseCase
import com.example.chillstay.domain.usecase.user.UpdateUserStatusUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class CustomerManageViewModel(
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase
) : BaseViewModel<CustomerManageUiState, CustomerManageIntent, CustomerManageEffect>(
    CustomerManageUiState()
) {

    val uiState = state
    private var allCustomersCache: List<User> = emptyList()
    private var filterJob: Job? = null

    init {
        loadCustomers()
    }

    override fun onEvent(event: CustomerManageIntent) {
        when (event) {
            is CustomerManageIntent.LoadCustomers -> {
                loadCustomers()
            }
            is CustomerManageIntent.SearchQueryChanged -> {
                _state.value = _state.value.updateSearchQuery(event.query)
                applyFiltersDebounced()
            }
            is CustomerManageIntent.PerformSearch -> {
                applyFiltersAsync()
            }
            is CustomerManageIntent.StatusFilterChanged -> {
                _state.value = _state.value.updateSelectedStatus(event.status)
                applyFiltersAsync()
            }
            is CustomerManageIntent.VipLevelFilterChanged -> {
                _state.value = _state.value.updateSelectedVipLevel(event.level)
                applyFiltersAsync()
            }
            is CustomerManageIntent.ToggleStatusDropdown -> {
                _state.value = _state.value.toggleStatusExpanded()
            }
            is CustomerManageIntent.ToggleVipLevelDropdown -> {
                _state.value = _state.value.toggleVipLevelExpanded()
            }
            is CustomerManageIntent.GoToPage -> {
                val maxPage = _state.value.totalPages
                if (event.page in 1..maxPage) {
                    _state.value = _state.value.updateCurrentPage(event.page)
                }
            }
            is CustomerManageIntent.NextPage -> {
                val currentPage = _state.value.currentPage
                val maxPage = _state.value.totalPages
                if (currentPage < maxPage) {
                    _state.value = _state.value.updateCurrentPage(currentPage + 1)
                }
            }
            is CustomerManageIntent.PreviousPage -> {
                val currentPage = _state.value.currentPage
                if (currentPage > 1) {
                    _state.value = _state.value.updateCurrentPage(currentPage - 1)
                }
            }
            is CustomerManageIntent.ViewCustomer -> {
                viewModelScope.launch {
                    sendEffect { CustomerManageEffect.NavigateToCustomerView(event.user.id) }
                }
            }
            is CustomerManageIntent.ToggleCustomerStatus -> {
                toggleCustomerStatus(event.user)
            }
            is CustomerManageIntent.ClearError -> {
                _state.value = _state.value.clearError()
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            _state.value = _state.value.updateIsLoading(true).clearError()
            allCustomersCache = emptyList()

            try {
                getAllUsersUseCase().collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            val customers = result.data
                            allCustomersCache = customers

                            withContext(Dispatchers.Default) {
                                withContext(Dispatchers.Main) {
                                    _state.value = _state.value
                                        .updateAllCustomers(customers)
                                        .copy(isLoading = false)
                                        .clearError()

                                    applyFiltersAsync()
                                }
                            }
                        }
                        is Result.Error -> {
                            _state.value = _state.value
                                .copy(isLoading = false)
                                .updateError(result.throwable.message ?: "Failed to load customers")
                            sendEffect {
                                CustomerManageEffect.ShowError(
                                    result.throwable.message ?: "Failed to load customers"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value
                    .copy(isLoading = false)
                    .updateError(e.message ?: "Failed to load customers")
            }
        }
    }

    private fun applyFiltersDebounced() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            delay(300)
            applyFiltersAsync()
        }
    }

    private fun applyFiltersAsync() {
        viewModelScope.launch(Dispatchers.Default) {
            val searchQuery = _state.value.searchQuery.lowercase()
            val statusFilter = _state.value.selectedStatus
            val vipLevelFilter = _state.value.selectedVipLevel

            val filtered = allCustomersCache.filter { user ->
                val matchesSearch = searchQuery.isBlank() ||
                        user.fullName.lowercase().contains(searchQuery) ||
                        user.email.lowercase().contains(searchQuery)

                // TODO: Add isActive field to User model
                // For now, assume all users are active
                val matchesStatus = statusFilter.isBlank() || statusFilter == "ACTIVE"

                // TODO: Get VipLevel for user
                // For now, skip VIP level filtering
                val matchesVipLevel = vipLevelFilter.isBlank()

                matchesSearch && matchesStatus && matchesVipLevel
            }

            val totalCustomers = filtered.size
            val activeCustomers = filtered.size // TODO: Filter by actual status
            val inactiveCustomers = 0 // TODO: Calculate from actual status

            withContext(Dispatchers.Main) {
                _state.value = _state.value.updateFilteredCustomers(filtered).copy(
                    totalCustomers = totalCustomers,
                    activeCustomers = activeCustomers)
            }
        }
    }

    private fun toggleCustomerStatus(user: User) {
        viewModelScope.launch {
            try {
                // TODO: Implement actual status toggle
                val newStatus = true // Toggle logic here
                updateUserStatusUseCase(user.id, newStatus).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            sendEffect {
                                CustomerManageEffect.ShowStatusChangeSuccess(user.id, newStatus)
                            }
                            loadCustomers()
                        }
                        is Result.Error -> {
                            sendEffect {
                                CustomerManageEffect.ShowError(
                                    result.throwable.message ?: "Failed to update customer status"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                sendEffect {
                    CustomerManageEffect.ShowError(
                        e.message ?: "Failed to update customer status"
                    )
                }
            }
        }
    }
}