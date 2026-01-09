package com.example.chillstay.ui.admin.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.model.NotificationType
import com.example.chillstay.domain.usecase.notification.CreateNotificationUseCase
import com.example.chillstay.domain.usecase.notification.GetAllNotificationsUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminNotificationViewModel(
    private val getAllNotificationsUseCase: GetAllNotificationsUseCase,
    private val createNotificationUseCase: CreateNotificationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminNotificationUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AdminNotificationEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadNotifications()
    }

    fun onEvent(event: AdminNotificationIntent) {
        when (event) {
            AdminNotificationIntent.LoadNotifications -> loadNotifications()
            is AdminNotificationIntent.TitleChanged -> _uiState.update { it.copy(newNotificationTitle = event.title) }
            is AdminNotificationIntent.MessageChanged -> _uiState.update { it.copy(newNotificationMessage = event.message) }
            is AdminNotificationIntent.UserIdChanged -> _uiState.update { it.copy(newNotificationUserId = event.userId) }
            is AdminNotificationIntent.SendToAllChanged -> _uiState.update { it.copy(isSendToAll = event.isSendToAll) }
            AdminNotificationIntent.OpenCreateDialog -> _uiState.update { it.copy(showCreateDialog = true) }
            AdminNotificationIntent.DismissCreateDialog -> _uiState.update { 
                it.copy(
                    showCreateDialog = false,
                    newNotificationTitle = "",
                    newNotificationMessage = "",
                    newNotificationUserId = "",
                    isSendToAll = false
                ) 
            }
            AdminNotificationIntent.SendNotification -> sendNotification()
        }
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getAllNotificationsUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, notifications = result.data) }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.throwable.message) }
                    }
                }
            }
        }
    }

    private fun sendNotification() {
        val state = _uiState.value
        val isUserIdValid = state.isSendToAll || state.newNotificationUserId.isNotBlank()

        if (state.newNotificationTitle.isBlank() || state.newNotificationMessage.isBlank() || !isUserIdValid) {
            viewModelScope.launch {
                _uiEffect.emit(AdminNotificationEffect.ShowMessage("Please fill all fields"))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }

            val targetUserId = if (state.isSendToAll) "ALL" else state.newNotificationUserId
            
            val notification = Notification(
                userId = targetUserId,
                title = state.newNotificationTitle,
                message = state.newNotificationMessage,
                type = NotificationType.SYSTEM
            )

            createNotificationUseCase(notification).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { 
                            it.copy(
                                isSending = false, 
                                showCreateDialog = false,
                                newNotificationTitle = "",
                                newNotificationMessage = "",
                                newNotificationUserId = ""
                            ) 
                        }
                        _uiEffect.emit(AdminNotificationEffect.ShowMessage("Notification sent successfully"))
                        loadNotifications() // Reload list
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(isSending = false) }
                        _uiEffect.emit(AdminNotificationEffect.ShowMessage("Failed to send: ${result.throwable.message}"))
                    }
                }
            }
        }
    }
}
