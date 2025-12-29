package com.example.chillstay.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.domain.usecase.user.GetUserByIdUseCase
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

class ProfileViewModel(
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    private var loadProfileJob: Job? = null

    init {
        onEvent(ProfileIntent.LoadProfile)
    }

    fun onEvent(event: ProfileIntent) {
        when (event) {
            is ProfileIntent.ProfileFullNameChanged -> _uiState.update { it.copy(profileFullName = event.value) }
            is ProfileIntent.ProfileGenderChanged -> _uiState.update { it.copy(profileGender = event.value) }
            is ProfileIntent.ProfilePhotoUrlChanged -> _uiState.update { it.copy(profilePhotoUrl = event.value) }
            is ProfileIntent.ProfileDateOfBirthChanged -> _uiState.update { it.copy(profileDateOfBirth = event.value) }
            ProfileIntent.LoadProfile -> refreshAuthenticatedUser()
            ProfileIntent.SaveProfile -> saveProfile()
            ProfileIntent.ClearMessage -> clearMessages()
            ProfileIntent.OpenPaymentMethod -> sendEffect(ProfileUiEffect.ShowMessage("Payment method sẽ sớm có"))
            ProfileIntent.OpenNotifications -> sendEffect(ProfileUiEffect.ShowMessage("Notifications sẽ sớm có"))
            ProfileIntent.OpenMyReviews -> sendEffect(ProfileUiEffect.NavigateToMyReviews)
            ProfileIntent.OpenLanguage -> sendEffect(ProfileUiEffect.ShowMessage("Language sẽ sớm có"))
            ProfileIntent.OpenHelp -> sendEffect(ProfileUiEffect.ShowMessage("Help sẽ sớm có"))
            ProfileIntent.OpenChangePassword -> sendEffect(ProfileUiEffect.ShowMessage("Change password sẽ sớm có"))
            is ProfileIntent.ShowUiMessage -> sendEffect(ProfileUiEffect.ShowMessage(event.message))
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
                            _uiState.update { it.copy(currentUser = null, isAuthenticated = false) }
                        } else {
                            loadProfile(userId)
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(profileMessage = result.throwable.message ?: "Failed to load user session") }
                    }
                }
            }
        }
    }

    private fun loadProfile(userId: String) {
        loadProfileJob?.cancel()
        loadProfileJob = viewModelScope.launch {
            _uiState.update { it.copy(isProfileLoading = true) }
            getUserByIdUseCase(userId).collectLatest { result ->
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
                        _uiState.update { it.copy(isProfileLoading = false, profileMessage = result.throwable.message ?: "Failed to load profile") }
                    }
                }
            }
        }
    }

    private fun clearMessages() {
        _uiState.update { it.copy(profileMessage = null) }
    }

    private fun sendEffect(effect: ProfileUiEffect) {
        viewModelScope.launch { _uiEffect.emit(effect) }
    }
}
