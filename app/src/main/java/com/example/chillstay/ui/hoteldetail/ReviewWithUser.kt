package com.example.chillstay.ui.hoteldetail

import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.model.User

/**
 * Data class chứa Review và User info để hiển thị
 */
data class ReviewWithUser(
    val review: Review,
    val user: User?
) {
    /**
     * Lấy tên user để hiển thị
     * Ưu tiên: fullName -> email -> "User xxxx"
     */
    val userName: String
        get() = user?.fullName?.takeIf { it.isNotBlank() } 
            ?: user?.email?.takeIf { it.isNotBlank() }
            ?: "User ${review.userId.takeLast(4)}"
    
    /**
     * Lấy photoUrl của user nếu có
     */
    val userPhotoUrl: String?
        get() = user?.photoUrl?.takeIf { it.isNotBlank() }
}

