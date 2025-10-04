package com.example.chillstay.di

import com.example.chillstay.domain.usecase.GetSampleItems
import com.example.chillstay.domain.usecase.SignUpUseCase
import com.example.chillstay.domain.usecase.SignInUseCase

// Hotel use cases
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import com.example.chillstay.domain.usecase.hotel.SearchHotelsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase

// Booking use cases
import com.example.chillstay.domain.usecase.booking.CreateBookingUseCase
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.CancelBookingUseCase

// User use cases
import com.example.chillstay.domain.usecase.user.GetUserProfileUseCase
import com.example.chillstay.domain.usecase.user.UpdateUserProfileUseCase

// Bookmark use cases
import com.example.chillstay.domain.usecase.bookmark.AddBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.RemoveBookmarkUseCase
import com.example.chillstay.domain.usecase.bookmark.GetUserBookmarksUseCase

// Review use cases
import com.example.chillstay.domain.usecase.review.CreateReviewUseCase
import com.example.chillstay.domain.usecase.review.GetHotelReviewsUseCase

// Voucher use cases
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.ApplyVoucherToBookingUseCase

// Notification use cases
import com.example.chillstay.domain.usecase.notification.GetUserNotificationsUseCase
import com.example.chillstay.domain.usecase.notification.MarkNotificationAsReadUseCase
import com.example.chillstay.domain.usecase.notification.MarkAllNotificationsAsReadUseCase

import org.koin.dsl.module

val useCaseModule = module {
    // Sample use cases
    factory { GetSampleItems(get()) }
    
    // Authentication use cases
    factory { SignUpUseCase(get()) }
    factory { SignInUseCase(get()) }
    
    // Hotel use cases
    factory { GetHotelsUseCase(get()) }
    factory { SearchHotelsUseCase(get()) }
    factory { GetHotelByIdUseCase(get()) }
    factory { GetHotelRoomsUseCase(get()) }
    
    // Booking use cases
    factory { CreateBookingUseCase(get()) }
    factory { GetUserBookingsUseCase(get()) }
    factory { CancelBookingUseCase(get()) }
    
    // User use cases
    factory { GetUserProfileUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    
    // Bookmark use cases
    factory { AddBookmarkUseCase(get()) }
    factory { RemoveBookmarkUseCase(get()) }
    factory { GetUserBookmarksUseCase(get()) }
    
    // Review use cases
    factory { CreateReviewUseCase(get()) }
    factory { GetHotelReviewsUseCase(get()) }
    
    // Voucher use cases
    factory { GetAvailableVouchersUseCase(get()) }
    factory { ApplyVoucherToBookingUseCase(get(), get()) }
    
    // Notification use cases
    factory { GetUserNotificationsUseCase(get()) }
    factory { MarkNotificationAsReadUseCase(get()) }
    factory { MarkAllNotificationsAsReadUseCase(get()) }
}
