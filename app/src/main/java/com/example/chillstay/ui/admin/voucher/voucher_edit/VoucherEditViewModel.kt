package com.example.chillstay.ui.admin.voucher.voucher_edit

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherType
import com.example.chillstay.domain.usecase.image.UploadVoucherImageUseCase
import com.example.chillstay.domain.usecase.voucher.CreateVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.GetVoucherByIdUseCase
import com.example.chillstay.domain.usecase.voucher.UpdateVoucherUseCase
import com.google.firebase.Timestamp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class VoucherEditViewModel(
    private val getVoucherByIdUseCase: GetVoucherByIdUseCase,
    private val createVoucherUseCase: CreateVoucherUseCase,
    private val updateVoucherUseCase: UpdateVoucherUseCase,
    private val uploadVoucherImageUseCase: UploadVoucherImageUseCase
) : BaseViewModel<VoucherEditUiState, VoucherEditIntent, VoucherEditEffect>(
    VoucherEditUiState()
) {
    companion object {
        private const val LOG_TAG = "VoucherEdit"
    }

    val uiState = state

    override fun onEvent(event: VoucherEditIntent) {
        when (event) {
            is VoucherEditIntent.LoadForCreate -> resetForCreate()
            is VoucherEditIntent.LoadForEdit -> loadVoucherById(event.voucherId)

            is VoucherEditIntent.UpdateTitle -> _state.value =
                _state.value.copy(title = event.value)

            is VoucherEditIntent.UpdateCode -> _state.value =
                _state.value.copy(code = event.value.uppercase())

            is VoucherEditIntent.UpdateDescription -> _state.value =
                _state.value.copy(description = event.value)

            is VoucherEditIntent.SetImage -> _state.value = _state.value.copy(imageUri = event.uri)
            is VoucherEditIntent.RemoveImage -> _state.value = _state.value.copy(imageUri = Uri.EMPTY)


            is VoucherEditIntent.SelectType -> _state.value = _state.value.copy(type = event.type)
            is VoucherEditIntent.UpdateValue -> _state.value = _state.value.copy(value = event.value)
            is VoucherEditIntent.UpdateValidFrom -> _state.value = _state.value.copy(validFrom = event.date)
            is VoucherEditIntent.UpdateValidTo -> _state.value = _state.value.copy(validTo = event.date)

            is VoucherEditIntent.UpdateMaxTotalUsage -> _state.value = _state.value.copy(maxTotalUsage = event.value)
            is VoucherEditIntent.UpdateMaxUsagePerUser -> _state.value = _state.value.copy(maxUsagePerUser = event.value)

            is VoucherEditIntent.UpdateMinBookingAmount -> _state.value = _state.value.copy(minBookingAmount = event.value)
            is VoucherEditIntent.UpdateMaxDiscountAmount -> _state.value = _state.value.copy(maxDiscountAmount = event.value)

            is VoucherEditIntent.UpdateMinNights -> _state.value = _state.value.copy(minNights = event.value)
            is VoucherEditIntent.ToggleStackable -> _state.value = _state.value.copy(isStackable = !_state.value.isStackable)
            is VoucherEditIntent.SelectUserLevel -> _state.value = _state.value.copy(requiredUserLevel = event.level)
            is VoucherEditIntent.ToggleValidDay -> toggleValidDay(event.day)
            is VoucherEditIntent.ToggleTimeSlot -> toggleTimeSlot(event.slot)

            VoucherEditIntent.Create -> createVoucher()
            VoucherEditIntent.Save -> saveVoucher()
            VoucherEditIntent.NavigateBack -> navigateBack()
            VoucherEditIntent.ClearError -> _state.value = _state.value.copy()
        }
    }

    private fun resetForCreate() {
        _state.value = VoucherEditUiState()
    }

    private fun loadVoucherById(voucherId: String) {
        viewModelScope.launch {

            _state.value = _state.value.copy(
                mode = Mode.Edit,
                voucherId = voucherId,
                isSaving = true
            )

            val result = getVoucherByIdUseCase(voucherId).first()
            when (result) {
                is Result.Success -> {
                    val voucher = result.data
                    _state.value = _state.value.copy(
                        isSaving = false,
                        voucherId = voucher.id,
                        title = voucher.title,
                        code = voucher.code,
                        description = voucher.description,
                        imageUri = voucher.imageUrl.toUri(),
                        type = voucher.type,
                        value = voucher.value.toString(),
                        validFrom = voucher.validFrom.toDate(),
                        validTo = voucher.validTo.toDate(),
                        maxTotalUsage = voucher.maxTotalUsage.toString(),
                        maxUsagePerUser = voucher.maxUsagePerUser.toString(),
                        minBookingAmount = voucher.minBookingAmount.toString(),
                        maxDiscountAmount = voucher.maxDiscountAmount.toString(),
                        requiredUserLevel = voucher.requiredUserLevel,
                        validDays = voucher.validDays.toSet(),
                        validTimeSlots = voucher.validTimeSlots.toSet(),
                        status = voucher.status,
                    )
                }

                is Result.Error -> {
                    _state.value = _state.value.copy(isSaving = false, isLoadingImage = false)
                    sendEffect {
                        VoucherEditEffect.ShowError(
                            result.throwable.message ?: "Failed to load voucher"
                        )
                    }
                }
            }
        }
    }

    private fun toggleValidDay(day: String) {
        val current = _state.value.validDays.toMutableSet()
        if (current.contains(day)) {
            current.remove(day)
        } else {
            current.add(day)
        }
        _state.value = _state.value.copy(validDays = current)
    }

    private fun toggleTimeSlot(slot: String) {
        val current = _state.value.validTimeSlots.toMutableSet()
        if (current.contains(slot)) {
            current.remove(slot)
        } else {
            current.add(slot)
        }
        _state.value = _state.value.copy(validTimeSlots = current)
    }

    private fun saveVoucher() {
        viewModelScope.launch {
            try {
                val validationError = validateVoucher()
                if (validationError != null) {
                    sendEffect { VoucherEditEffect.ShowError(validationError) }
                    return@launch
                }

                _state.value = _state.value.copy(isSaving = true)
                val voucherId = _state.value.voucherId
                if(voucherId.isNullOrBlank()) throw IllegalStateException("Cannot update voucher without ID. Use Create mode instead.")
                // Upload Image
                val uploadedImage = uploadSingleImage(voucherId)
                val voucherToUpdate = buildVoucherFromState().copy(imageUrl = uploadedImage)

                updateVoucherUseCase(voucherToUpdate).first().fold(
                    onSuccess = {
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect { VoucherEditEffect.ShowSaveSuccess(voucherToUpdate) }
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            VoucherEditEffect.ShowError(
                                throwable.message ?: "Failed to save voucher"
                            )
                        }
                    }
                )
            } catch (e: IllegalStateException) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { VoucherEditEffect.ShowError(e.message ?: "Invalid state") }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { VoucherEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }

    private fun createVoucher() {
        viewModelScope.launch {
            try {
                val validationError = validateVoucher()
                if (validationError != null) {
                    sendEffect { VoucherEditEffect.ShowError(validationError) }
                    return@launch
                }

                _state.value = _state.value.copy(isSaving = true)

                // Create voucher to get ID
                val minimalVoucher = buildVoucherFromState().copy(id = "", imageUrl = "")
                createVoucherUseCase(minimalVoucher).first().fold(
                    onSuccess = { newId ->
                        _state.value = _state.value.copy(voucherId = newId)
                        // Upload Image
                        val uploadedImage = uploadSingleImage(newId)
                        val finalVoucher = buildVoucherFromState().copy(
                            id = newId,
                            imageUrl = uploadedImage
                        )
                        // Update voucher with image URL
                        updateVoucherUseCase(finalVoucher).first().fold(
                            onSuccess = {
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect { VoucherEditEffect.ShowCreateSuccess(finalVoucher) }
                            },
                            onFailure = { throwable ->
                                _state.value = _state.value.copy(isSaving = false)
                                sendEffect {
                                    VoucherEditEffect.ShowError(
                                        throwable.message ?: "Failed to finalize voucher creation"
                                    )
                                }
                            }
                        )
                    },
                    onFailure = { throwable ->
                        _state.value = _state.value.copy(isSaving = false)
                        sendEffect {
                            VoucherEditEffect.ShowError(
                                throwable.message ?: "Failed to create voucher"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isSaving = false)
                sendEffect { VoucherEditEffect.ShowError(e.message ?: "Unexpected error") }
            }
        }
    }
    private fun validateVoucher(): String? {
        if (_state.value.title.isBlank()) return "Title is required"
        if (_state.value.code.isBlank()) return "Code is required"
        if (_state.value.value.isBlank()) return "Discount value is required"

        val value = _state.value.value.toDoubleOrNull()
        if (value == null || value <= 0) return "Invalid discount value"

        if (_state.value.type == VoucherType.PERCENTAGE && value > 100) {
            return "Percentage cannot exceed 100%"
        }

        if (_state.value.validFrom == null) return "Start date is required"
        if (_state.value.validTo == null) return "End date is required"

        if (_state.value.validTo!! <= _state.value.validFrom!!) {
            return "End date must be after start date"
        }

        return null
    }

    private suspend fun uploadSingleImage(voucherId: String): String {
        val imageUri = _state.value.imageUri
        Log.d(LOG_TAG, "Uploading voucher image for voucher: $voucherId")
        return try {
            // Upload single image and get URL back
            val uploadedUrl = uploadVoucherImageUseCase(voucherId = voucherId,imageUri = imageUri)
            Log.d(LOG_TAG, "Image uploaded successfully. URL: $uploadedUrl")
            uploadedUrl
        } catch (e: CancellationException) {
            Log.w(LOG_TAG, "Upload cancelled: ${e.message}")
            throw e
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error uploading images: ${e.message}", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Cannot connect to image service. Please check your connection."
                e.message?.contains("Connection refused", ignoreCase = true) == true ->
                    "Connection refused. Please check the backend service."
                else -> "Failed to upload images: ${e.message}"
            }
            sendEffect { VoucherEditEffect.ShowError(errorMessage) }
            ""
        }
    }

    private fun buildVoucherFromState(): Voucher {
        return Voucher(
            id = _state.value.voucherId.orEmpty(),
            code = _state.value.code,
            title = _state.value.title,
            description = _state.value.description,
            type = _state.value.type,
            value = _state.value.value.toDoubleOrNull() ?: 0.0,
            status = _state.value.status,
            validFrom = Timestamp(_state.value.validFrom ?: Date()),
            validTo = Timestamp(_state.value.validTo ?: Date()),
            minBookingAmount = _state.value.minBookingAmount.toDoubleOrNull() ?: 0.0,
            maxDiscountAmount = _state.value.maxDiscountAmount.toDoubleOrNull() ?: 0.0,
            maxUsagePerUser = _state.value.maxUsagePerUser.toIntOrNull() ?: 1,
            maxTotalUsage = _state.value.maxTotalUsage.toIntOrNull() ?: 0,
            minNights = _state.value.minNights.toIntOrNull() ?: 0,
            isStackable = _state.value.isStackable,
            requiredUserLevel = _state.value.requiredUserLevel,
            validDays = _state.value.validDays.toList(),
            validTimeSlots = _state.value.validTimeSlots.toList(),
            createdAt = Timestamp.now(),
        )
    }


    private fun navigateBack() {
        viewModelScope.launch {
            sendEffect { VoucherEditEffect.NavigateBack }
        }
    }
}