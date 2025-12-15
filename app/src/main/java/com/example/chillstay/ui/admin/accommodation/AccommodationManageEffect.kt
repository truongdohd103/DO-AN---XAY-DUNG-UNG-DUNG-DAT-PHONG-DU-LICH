package com.example.chillstay.ui.admin.accommodation

import com.example.chillstay.core.base.UiEffect
import com.example.chillstay.domain.model.Hotel

sealed interface AccommodationManageEffect : UiEffect {
    object NavigateBack : AccommodationManageEffect
    object NavigateToCreateNew : AccommodationManageEffect
    data class NavigateToEdit(val hotel: Hotel) : AccommodationManageEffect
    data class ShowInvalidateSuccess(val hotel: Hotel) : AccommodationManageEffect
    data class ShowDeleteSuccess(val hotel: Hotel) : AccommodationManageEffect
    data class ShowError(val message: String) : AccommodationManageEffect
    object NavigateToViewAll : AccommodationManageEffect
}
