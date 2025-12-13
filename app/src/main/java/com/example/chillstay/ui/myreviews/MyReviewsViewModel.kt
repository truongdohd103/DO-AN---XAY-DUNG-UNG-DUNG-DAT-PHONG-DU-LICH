package com.example.chillstay.ui.myreviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MyReviewsUiState(
    val isLoading: Boolean = true,
    val reviews: List<Review> = emptyList(),
    val error: String? = null,
    val hotelMap: Map<String, Hotel> = emptyMap()
)

class MyReviewsViewModel(
    private val reviewRepository: ReviewRepository,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val getHotelByIdUseCase: GetHotelByIdUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(MyReviewsUiState())
    val state: StateFlow<MyReviewsUiState> = _state.asStateFlow()

    init {
        loadMyReviews()
    }

    fun loadMyReviews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            getCurrentUserIdUseCase().collectLatest { res ->
                when (res) {
                    is Result.Success -> {
                        val uid = res.data
                        if (uid.isNullOrBlank()) {
                            _state.value = MyReviewsUiState(isLoading = false, reviews = emptyList(), error = "Vui lòng đăng nhập")
                        } else {
                            try {
                                val reviews = reviewRepository.getUserReviews(uid)
                                val hotelIds = reviews.map { it.hotelId }.distinct()
                                val hotels = mutableMapOf<String, Hotel>()
                                for (hid in hotelIds) {
                                    try {
                                        when (val res = getHotelByIdUseCase(hid).first()) {
                                            is Result.Success -> hotels[hid] = res.data
                                            is Result.Error -> {}
                                        }
                                    } catch (_: Exception) { }
                                }
                                _state.value = MyReviewsUiState(
                                    isLoading = false,
                                    reviews = reviews,
                                    error = null,
                                    hotelMap = hotels
                                )
                            } catch (e: Exception) {
                                _state.value = MyReviewsUiState(isLoading = false, reviews = emptyList(), error = e.message)
                            }
                        }
                    }
                    is Result.Error -> {
                        _state.value = MyReviewsUiState(isLoading = false, reviews = emptyList(), error = res.throwable.message)
                    }
                }
            }
        }
    }
}
