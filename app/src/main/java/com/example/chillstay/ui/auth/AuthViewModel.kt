package com.example.chillstay.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.domain.usecase.user.SignInUseCase
import com.example.chillstay.domain.usecase.user.SignOutUseCase
import com.example.chillstay.domain.usecase.user.SignUpUseCase
// Profile-specific use cases removed from AuthViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AuthEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var loadProfileJob: Job? = null

    init {
        refreshAuthenticatedUser()
    }

    fun onEvent(event: AuthIntent) {
        when (event) {
            is AuthIntent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is AuthIntent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is AuthIntent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.value) }
            AuthIntent.SignIn -> signIn()
            AuthIntent.SignUp -> signUp()
            AuthIntent.SignOut -> signOut()
            // Auth no longer loads or saves profile details
            AuthIntent.ClearMessage -> clearMessages()
        }
    }

    private fun signIn() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null, preserveMessage = false) }
            signInUseCase(email, password).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isAuthenticated = true,
                                email = "",
                                password = "",
                                confirmPassword = "",
                                errorMessage = null,
                                successMessage = null,
                                preserveMessage = false
                            )
                        }
                        if (user.role.equals("admin", ignoreCase = true)) {
                            sendEffect(AuthEffect.NavigateToAdminHome)
                        } else {
                            sendEffect(AuthEffect.NavigateToMain)
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.throwable.message ?: "Failed to sign in",
                                preserveMessage = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signUp() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null, preserveMessage = false) }
            signUpUseCase(email, password).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Account created successfully. Please sign in.",
                                errorMessage = null,
                                preserveMessage = true,
                                email = "",
                                password = "",
                                confirmPassword = ""
                            )
                        }
                        sendEffect(AuthEffect.NavigateToSignIn)
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.throwable.message ?: "Failed to sign up",
                                preserveMessage = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.value = AuthState()
                        sendEffect(AuthEffect.NavigateToAuth)
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                errorMessage = result.throwable.message ?: "Failed to sign out",
                                preserveMessage = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun refreshAuthenticatedUser() {
        viewModelScope.launch {
            getCurrentUserIdUseCase().collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val userId = result.data
                        _uiState.update {
                            it.copy(
                                isAuthenticated = !userId.isNullOrBlank()
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(errorMessage = result.throwable.message ?: "Failed to load user session")
                        }
                    }
                }
            }
        }
    }

    private fun clearMessages() {
        _uiState.update { state ->
            if (state.preserveMessage) {
                state.copy(preserveMessage = false)
            } else {
                state.copy(
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    private fun sendEffect(effect: AuthEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }
}
