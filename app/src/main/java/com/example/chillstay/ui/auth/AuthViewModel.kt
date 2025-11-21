package com.example.chillstay.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.domain.usecase.user.SignInUseCase
import com.example.chillstay.domain.usecase.user.SignOutUseCase
import com.example.chillstay.domain.usecase.user.SignUpUseCase
import com.example.chillstay.domain.usecase.user.GetUserProfileUseCase
import com.example.chillstay.domain.usecase.user.UpdateUserProfileUseCase
import java.time.LocalDate
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
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AuthUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var loadProfileJob: Job? = null

    init {
        refreshAuthenticatedUser()
    }

    fun onEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.EmailChanged -> _uiState.update { it.copy(email = event.value) }
            is AuthUiEvent.PasswordChanged -> _uiState.update { it.copy(password = event.value) }
            is AuthUiEvent.ConfirmPasswordChanged -> _uiState.update { it.copy(confirmPassword = event.value) }
            is AuthUiEvent.ProfileFullNameChanged -> _uiState.update { it.copy(profileFullName = event.value) }
            is AuthUiEvent.ProfileGenderChanged -> _uiState.update { it.copy(profileGender = event.value) }
            is AuthUiEvent.ProfilePhotoUrlChanged -> _uiState.update { it.copy(profilePhotoUrl = event.value) }
            is AuthUiEvent.ProfileDateOfBirthChanged -> _uiState.update { it.copy(profileDateOfBirth = event.value) }
            AuthUiEvent.SignIn -> signIn()
            AuthUiEvent.SignUp -> signUp()
            AuthUiEvent.SignOut -> signOut()
            AuthUiEvent.LoadProfile -> refreshAuthenticatedUser()
            AuthUiEvent.SaveProfile -> saveProfile()
            AuthUiEvent.ClearMessage -> clearMessages()
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
                                currentUser = user,
                                isAuthenticated = true,
                                email = "",
                                password = "",
                                confirmPassword = "",
                                profileFullName = user.fullName,
                                profileGender = user.gender,
                                profilePhotoUrl = user.photoUrl,
                                profileDateOfBirth = user.dateOfBirth.toString(),
                                errorMessage = null,
                                successMessage = null,
                                profileMessage = null,
                                preserveMessage = false
                            )
                        }
                        sendEffect(AuthUiEffect.NavigateToMain)
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
                        sendEffect(AuthUiEffect.NavigateToSignIn)
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
                        _uiState.value = AuthUiState()
                        sendEffect(AuthUiEffect.NavigateToAuth)
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

    private fun saveProfile() {
        val user = _uiState.value.currentUser ?: return
        val dateInput = _uiState.value.profileDateOfBirth
        val parsedDate = if (dateInput.isBlank()) {
            null
        } else {
            runCatching { LocalDate.parse(dateInput) }.getOrElse {
                _uiState.update { it.copy(profileMessage = "Date of birth must follow yyyy-MM-dd") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true, profileMessage = null) }
            updateUserProfileUseCase(
                userId = user.id,
                fullName = _uiState.value.profileFullName.ifBlank { null },
                gender = _uiState.value.profileGender.ifBlank { null },
                photoUrl = _uiState.value.profilePhotoUrl.ifBlank { null },
                dateOfBirth = parsedDate
            ).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val updated = result.data
                        _uiState.update {
                            it.copy(
                                isProfileLoading = false,
                                currentUser = updated,
                                profileFullName = updated.fullName,
                                profileGender = updated.gender,
                                profilePhotoUrl = updated.photoUrl,
                                profileDateOfBirth = updated.dateOfBirth.toString(),
                                profileMessage = "Profile updated successfully"
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isProfileLoading = false,
                                profileMessage = result.throwable.message ?: "Failed to update profile"
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
                        if (userId.isNullOrBlank()) {
                            _uiState.update {
                                it.copy(
                                    currentUser = null,
                                    isAuthenticated = false
                                )
                            }
                        } else {
                            loadProfile(userId)
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

    private fun loadProfile(userId: String) {
        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true) }
            getUserProfileUseCase(userId).collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        val user = result.data
                        _uiState.update {
                            it.copy(
                                isProfileLoading = false,
                                currentUser = user,
                                isAuthenticated = true,
                                profileFullName = user.fullName,
                                profileGender = user.gender,
                                profilePhotoUrl = user.photoUrl,
                                profileDateOfBirth = user.dateOfBirth.toString(),
                                profileMessage = null
                            )
                        }
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isProfileLoading = false,
                                profileMessage = result.throwable.message ?: "Failed to load profile"
                            )
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
                    successMessage = null,
                    profileMessage = null
                )
            }
        }
    }

    private fun sendEffect(effect: AuthUiEffect) {
        viewModelScope.launch {
            _uiEffect.emit(effect)
        }
    }
}

