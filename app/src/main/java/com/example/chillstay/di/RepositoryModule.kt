package com.example.chillstay.di

import com.example.chillstay.data.repository.InMemorySampleRepository
import com.example.chillstay.data.repository.InMemoryHotelRepository
import com.example.chillstay.data.repository.FakeUserRepository
import com.example.chillstay.domain.repository.SampleRepository
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.domain.repository.NotificationRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SampleRepository> { InMemorySampleRepository() }
    single<HotelRepository> { InMemoryHotelRepository() }
    single<UserRepository> { FakeUserRepository() }
    
    // TODO: Implement these repositories
    single<BookingRepository> { 
        // Placeholder - implement InMemoryBookingRepository
        object : BookingRepository {
            override suspend fun getBookingById(id: String): com.example.chillstay.domain.model.Booking? = null
            override suspend fun getUserBookings(userId: String, status: String?): List<com.example.chillstay.domain.model.Booking> = emptyList()
            override suspend fun createBooking(booking: com.example.chillstay.domain.model.Booking): com.example.chillstay.domain.model.Booking = booking
            override suspend fun updateBooking(booking: com.example.chillstay.domain.model.Booking): com.example.chillstay.domain.model.Booking = booking
            override suspend fun cancelBooking(bookingId: String): Boolean = false
            override suspend fun getBookingHotelId(bookingId: String): String? = null
        }
    }
    
    single<BookmarkRepository> { 
        // Placeholder - implement InMemoryBookmarkRepository
        object : BookmarkRepository {
            override suspend fun getUserBookmarks(userId: String): List<com.example.chillstay.domain.model.Bookmark> = emptyList()
            override suspend fun addBookmark(bookmark: com.example.chillstay.domain.model.Bookmark): com.example.chillstay.domain.model.Bookmark = bookmark
            override suspend fun removeBookmark(userId: String, hotelId: String): Boolean = false
            override suspend fun isBookmarked(userId: String, hotelId: String): Boolean = false
        }
    }
    
    single<ReviewRepository> { 
        // Placeholder - implement InMemoryReviewRepository
        object : ReviewRepository {
            override suspend fun getHotelReviews(hotelId: String, limit: Int?, offset: Int): List<com.example.chillstay.domain.model.Review> = emptyList()
            override suspend fun getUserReviewForHotel(userId: String, hotelId: String): com.example.chillstay.domain.model.Review? = null
            override suspend fun createReview(review: com.example.chillstay.domain.model.Review): com.example.chillstay.domain.model.Review = review
            override suspend fun updateReview(review: com.example.chillstay.domain.model.Review): com.example.chillstay.domain.model.Review = review
            override suspend fun deleteReview(reviewId: String): Boolean = false
        }
    }
    
    single<VoucherRepository> { 
        // Placeholder - implement InMemoryVoucherRepository
        object : VoucherRepository {
            override suspend fun getVouchers(): List<com.example.chillstay.domain.model.Voucher> = emptyList()
            override suspend fun getVoucherById(id: String): com.example.chillstay.domain.model.Voucher? = null
            override suspend fun getVoucherByCode(code: String): com.example.chillstay.domain.model.Voucher? = null
            override suspend fun createVoucher(voucher: com.example.chillstay.domain.model.Voucher): com.example.chillstay.domain.model.Voucher = voucher
            override suspend fun updateVoucher(voucher: com.example.chillstay.domain.model.Voucher): com.example.chillstay.domain.model.Voucher = voucher
        }
    }
    
    single<NotificationRepository> { 
        // Placeholder - implement InMemoryNotificationRepository
        object : NotificationRepository {
            override suspend fun getUserNotifications(userId: String, isRead: Boolean?, limit: Int?): List<com.example.chillstay.domain.model.Notification> = emptyList()
            override suspend fun createNotification(notification: com.example.chillstay.domain.model.Notification): com.example.chillstay.domain.model.Notification = notification
            override suspend fun markAsRead(notificationId: String): Boolean = false
            override suspend fun markAllAsRead(userId: String): Boolean = false
            override suspend fun deleteNotification(notificationId: String): Boolean = false
        }
    }
}
