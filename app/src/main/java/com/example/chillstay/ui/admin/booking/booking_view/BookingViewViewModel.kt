package com.example.chillstay.ui.admin.booking.booking_view

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.user.GetUserByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.vip.GetVipStatusUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class BookingViewViewModel(
    private val getBookingByIdUseCase: GetBookingByIdUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getVipStatusUseCase: GetVipStatusUseCase
) : BaseViewModel<BookingViewUiState, BookingViewIntent, BookingViewEffect>(
    BookingViewUiState()
) {

    val uiState = state

    override fun onEvent(event: BookingViewIntent) {
        when (event) {
            is BookingViewIntent.LoadBooking -> loadBooking(event.bookingId)
            is BookingViewIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { BookingViewEffect.NavigateBack }
                }
            }
            is BookingViewIntent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (val bookingResult = getBookingByIdUseCase(bookingId).first()) {
                    is Result.Success -> {
                        val booking = bookingResult.data
                        _state.update { it.copy(booking = booking) }

                        supervisorScope {
                            val userDeferred = async { getUserByIdUseCase(booking.userId).first() }
                            val vipDeferred = async { getVipStatusUseCase(booking.userId).first() }
                            val hotelDeferred = async { getHotelByIdUseCase(booking.hotelId).first() }

                            try {
                                when (val userResult = userDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(user = userResult.data) }
                                    is Result.Error -> _state.update { it.copy(error = "Failed to load user: ${userResult.throwable.message}") }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = "Failed to load user: ${e.message ?: e::class.simpleName}") }
                            }

                            try {
                                when (val vipResult = vipDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(vipStatus = vipResult.data) }
                                    is Result.Error -> { }
                                }
                            } catch (_: Exception) { }

                            try {
                                when (val hotelResult = hotelDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(hotel = hotelResult.data) }
                                    is Result.Error -> _state.update { it.copy(error = "Failed to load hotel: ${hotelResult.throwable.message}") }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = "Failed to load hotel: ${e.message ?: e::class.simpleName}") }
                            }
                        }
                    }
                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = "Failed to load booking: ${bookingResult.throwable.message}") }
                        sendEffect { BookingViewEffect.ShowError(bookingResult.throwable.message ?: "Failed to load booking") }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
                sendEffect { BookingViewEffect.ShowError(e.message ?: "An error occurred") }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

}