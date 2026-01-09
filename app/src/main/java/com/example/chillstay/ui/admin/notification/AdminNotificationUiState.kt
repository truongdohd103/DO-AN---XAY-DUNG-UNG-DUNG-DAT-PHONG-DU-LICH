package com.example.chillstay.ui.admin.notification

import com.example.chillstay.domain.model.Notification
import com.example.chillstay.core.base.UiState

data class AdminNotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showCreateDialog: Boolean = false,
    val newNotificationTitle: String = "",
    val newNotificationMessage: String = "",
    val newNotificationUserId: String = "", // Optional: specific user
    val isSendToAll: Boolean = false,
    val isSending: Boolean = false,
    val message: String? = null
)

sealed interface AdminNotificationEffect : com.example.chillstay.core.base.UiEffect {
    data class ShowMessage(val message: String) : AdminNotificationEffect
}

sealed class AdminNotificationIntent {
    data object LoadNotifications : AdminNotificationIntent()
    data class TitleChanged(val title: String) : AdminNotificationIntent()
    data class MessageChanged(val message: String) : AdminNotificationIntent()
    data class UserIdChanged(val userId: String) : AdminNotificationIntent()
    data class SendToAllChanged(val isSendToAll: Boolean) : AdminNotificationIntent()
    data object OpenCreateDialog : AdminNotificationIntent()
    data object DismissCreateDialog : AdminNotificationIntent()
    data object SendNotification : AdminNotificationIntent()
}
