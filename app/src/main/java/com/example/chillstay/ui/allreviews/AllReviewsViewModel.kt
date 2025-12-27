package com.example.chillstay.ui.allreviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.usecase.review.GetHotelReviewsUseCase
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.ui.hoteldetail.ReviewWithUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

data class AllReviewsUiState(
    val isLoading: Boolean = true,
    val reviewsWithUser: List<ReviewWithUser> = emptyList(),
    val error: String? = null
)

class AllReviewsViewModel(
    private val getHotelReviewsUseCase: GetHotelReviewsUseCase,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AllReviewsUiState())
    val state: StateFlow<AllReviewsUiState> = _state.asStateFlow()

    fun loadReviews(hotelId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val res = getHotelReviewsUseCase(hotelId)
            when (res) {
                is Result.Success -> {
                    val reviews = res.data
                    try {
                        val tasks = reviews.map { review ->
                            async {
                                val user = try { userRepository.getUserById(review.userId) } catch (_: Exception) { null }
                                ReviewWithUser(review, user)
                            }
                        }
                        val joined = tasks.awaitAll()
                        _state.value = AllReviewsUiState(isLoading = false, reviewsWithUser = joined, error = null)
                    } catch (e: Exception) {
                        _state.value = AllReviewsUiState(isLoading = false, reviewsWithUser = reviews.map { ReviewWithUser(it, null) }, error = null)
                    }
                }
                is Result.Error -> _state.value = AllReviewsUiState(isLoading = false, reviewsWithUser = emptyList(), error = res.throwable.message)
            }
        }
    }
}
