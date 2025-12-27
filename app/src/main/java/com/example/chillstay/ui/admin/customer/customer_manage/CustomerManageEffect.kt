package com.example.chillstay.ui.admin.customer.customer_manage

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.User

sealed interface CustomerManageEffect : UiEffect {
    data object NavigateBack : CustomerManageEffect
    data class NavigateToCustomerView(val userId: String) : CustomerManageEffect
    data class ShowStatusChangeSuccess(val userId: String, val isActive: Boolean) : CustomerManageEffect
    data class ShowError(val message: String) : CustomerManageEffect
}