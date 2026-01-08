package com.example.chillstay.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.usecase.notification.GetUserNotificationsUseCase
import com.example.chillstay.domain.usecase.notification.MarkNotificationReadUseCase
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationViewModel(
    private val getUserNotificationsUseCase: GetUserNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            getCurrentUserIdUseCase().collect { userIdResult ->
                if (userIdResult is Result.Success<*>) {
                    val userId = (userIdResult as Result.Success<String?>).data
                    if (!userId.isNullOrEmpty()) {
                        _uiState.update { it.copy(isLoading = true) }
                        getUserNotificationsUseCase(userId).collect { result ->
                            when (result) {
                                is Result.Success<*> -> {
                                    val notifications = (result as Result.Success<List<Notification>>).data
                                    _uiState.update { it.copy(notifications = notifications, isLoading = false) }
                                }
                                is Result.Error -> {
                                    _uiState.update { it.copy(error = result.throwable.message, isLoading = false) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            markNotificationReadUseCase(notificationId).collect {
                // Optimistically update UI or reload
                loadNotifications()
            }
        }
    }
    
    fun markAllAsRead() {
        viewModelScope.launch {
            getCurrentUserIdUseCase().collect { userIdResult ->
                 if (userIdResult is Result.Success<*>) {
                     val userId = (userIdResult as Result.Success<String?>).data
                     if (!userId.isNullOrEmpty()) {
                         markNotificationReadUseCase.markAll(userId).collect {
                             loadNotifications()
                         }
                     }
                 }
            }
        }
    }
}
