package com.example.chillstay.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.domain.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Thêm welcome message
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatMessage(
                        text = "Xin chào! Tôi là trợ lý ảo ChillStay. Tôi có thể giúp gì cho bạn?",
                        isUser = false
                    )
                )
            )
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Thêm message của user
        val userMessage = ChatMessage(text = message, isUser = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true,
                errorMessage = null
            )
        }

        // Gọi API
        viewModelScope.launch {
            chatRepository.sendMessage(message)
                .onSuccess { response ->
                    val botMessage = ChatMessage(text = response, isUser = false)
                    _uiState.update {
                        it.copy(
                            messages = it.messages + botMessage,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    val errorMsg = ChatMessage(
                        text = "Xin lỗi, đã có lỗi xảy ra: ${error.message}. Vui lòng thử lại.",
                        isUser = false,
                        isError = true
                    )
                    _uiState.update {
                        it.copy(
                            messages = it.messages + errorMsg,
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            messages = listOf(
                                ChatMessage(
                                    text = "Xin chào! Tôi là trợ lý ảo ChillStay. Tôi có thể giúp gì cho bạn?",
                                    isUser = false
                                )
                            ),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(errorMessage = error.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}