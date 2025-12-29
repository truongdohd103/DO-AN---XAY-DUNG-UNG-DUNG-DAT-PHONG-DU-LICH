package com.example.chillstay.ui.admin.customer.review_view

import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.base.BaseViewModel
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.user.GetUserByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.review.GetReviewByIdUseCase
import com.example.chillstay.domain.usecase.vip.GetVipStatusUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ReviewViewViewModel(
    private val getReviewByIdUseCase: GetReviewByIdUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getVipStatusUseCase: GetVipStatusUseCase
) : BaseViewModel<ReviewViewUiState, ReviewViewIntent, ReviewViewEffect>(
    ReviewViewUiState()
) {

    val uiState = state

    override fun onEvent(event: ReviewViewIntent) {
        when (event) {
            is ReviewViewIntent.LoadReview -> loadReview(event.reviewId)
            is ReviewViewIntent.NavigateBack -> {
                viewModelScope.launch {
                    sendEffect { ReviewViewEffect.NavigateBack }
                }
            }
            is ReviewViewIntent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun loadReview(reviewId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                when (val reviewResult = getReviewByIdUseCase(reviewId).first()) {
                    is Result.Success -> {
                        val review = reviewResult.data
                        _state.update { it.copy(review = review) }

                        // fetch user, hotel, vip in parallel but isolated
                        supervisorScope {
                            val userDeferred = async { getUserByIdUseCase(review.userId).first() }
                            val hotelDeferred = async { getHotelByIdUseCase(review.hotelId).first() }
                            val vipDeferred = async { getVipStatusUseCase(review.userId).first() }

                            // user
                            try {
                                when (val userResult = userDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(user = userResult.data) }
                                    is Result.Error -> _state.update { it.copy(error = "Failed to load user: ${userResult.throwable.message}") }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = "Failed to load user: ${e.message ?: e::class.simpleName}") }
                            }

                            // hotel
                            try {
                                when (val hotelResult = hotelDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(hotel = hotelResult.data) }
                                    is Result.Error -> _state.update { it.copy(error = "Failed to load hotel: ${hotelResult.throwable.message}") }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = "Failed to load hotel: ${e.message ?: e::class.simpleName}") }
                            }

                            // vip
                            try {
                                when (val vipResult = vipDeferred.await()) {
                                    is Result.Success -> _state.update { it.copy(vipStatus = vipResult.data) }
                                    is Result.Error -> _state.update { it.copy(error = "Failed to load VIP status: ${vipResult.throwable.message}") }
                                }
                            } catch (e: Exception) {
                                _state.update { it.copy(error = "Failed to load VIP status: ${e.message ?: e::class.simpleName}") }
                            }
                        }

                        _state.update { it.copy(isLoading = false) }
                    }

                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load review: ${reviewResult.throwable.message}"
                            )
                        }
                        sendEffect {
                            ReviewViewEffect.ShowError(reviewResult.throwable.message ?: "Failed to load review")
                        }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "An error occurred") }
                sendEffect { ReviewViewEffect.ShowError(e.message ?: "An error occurred") }
            }
        }
    }

}