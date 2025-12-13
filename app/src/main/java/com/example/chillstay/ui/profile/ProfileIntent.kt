package com.example.chillstay.ui.profile

sealed interface ProfileIntent : com.example.chillstay.core.base.UiEvent {
    data class ProfileFullNameChanged(val value: String) : ProfileIntent
    data class ProfileGenderChanged(val value: String) : ProfileIntent
    data class ProfilePhotoUrlChanged(val value: String) : ProfileIntent
    data class ProfileDateOfBirthChanged(val value: String) : ProfileIntent

    object LoadProfile : ProfileIntent
    object SaveProfile : ProfileIntent
    object ClearMessage : ProfileIntent

    object OpenPaymentMethod : ProfileIntent
    object OpenNotifications : ProfileIntent
    object OpenMyReviews : ProfileIntent
    object OpenLanguage : ProfileIntent
    object OpenHelp : ProfileIntent
    object OpenChangePassword : ProfileIntent

    data class ShowUiMessage(val message: String) : ProfileIntent
}
