package com.example.chillstay.di

// removed sample use case
import com.example.chillstay.domain.usecase.user.SignUpUseCase
import com.example.chillstay.domain.usecase.user.SignInUseCase
import com.example.chillstay.domain.usecase.user.SignOutUseCase
import com.example.chillstay.domain.usecase.user.GetCurrentUserIdUseCase

// Hotel use cases
import com.example.chillstay.domain.usecase.hotel.GetHotelsUseCase
import com.example.chillstay.domain.usecase.hotel.SearchHotelsUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelByIdUseCase
import com.example.chillstay.domain.usecase.hotel.GetHotelRoomsUseCase
import com.example.chillstay.domain.usecase.hotel.GetRoomByIdUseCase

// Booking use cases
import com.example.chillstay.domain.usecase.booking.CreateBookingUseCase
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.CancelBookingUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.booking.DeleteBookingUseCase

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
import com.example.chillstay.domain.usecase.review.AggregateHotelRatingForHotelUseCase
import com.example.chillstay.domain.usecase.review.UpdateReviewUseCase
import com.example.chillstay.domain.usecase.review.DeleteReviewUseCase
import com.example.chillstay.domain.usecase.image.UploadAccommodationImagesUseCase

// Voucher use cases
import com.example.chillstay.domain.usecase.voucher.GetAvailableVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.ApplyVoucherToBookingUseCase
import com.example.chillstay.domain.usecase.voucher.GetVoucherByIdUseCase
import com.example.chillstay.domain.usecase.voucher.ClaimVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.CheckVoucherEligibilityUseCase

// VIP use cases
import com.example.chillstay.domain.usecase.vip.GetVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.GetVipBenefitsUseCase
import com.example.chillstay.domain.usecase.vip.GetVipStatusHistoryUseCase
import com.example.chillstay.domain.usecase.vip.CreateVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.UpdateVipStatusUseCase
import com.example.chillstay.domain.usecase.vip.AddVipStatusHistoryUseCase


import com.example.chillstay.domain.repository.VoucherRepository
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.VipStatusRepository
import org.koin.dsl.module
import com.example.chillstay.domain.repository.AuthRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.usecase.hotel.CreateHotelUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateHotelUseCase

val useCaseModule = module {
    // Sample use cases removed
    
    // Authentication use cases
    factory { SignUpUseCase(get<AuthRepository>(), get<UserRepository>()) }
    factory { SignInUseCase(get<AuthRepository>(), get<UserRepository>()) }
    factory { SignOutUseCase(get<AuthRepository>()) }
    factory { GetCurrentUserIdUseCase(get<AuthRepository>()) }
    
    // Hotel use cases
    factory { CreateHotelUseCase(get()) }
    factory { UpdateHotelUseCase(get()) }
    factory { GetHotelsUseCase(get()) }
    factory { SearchHotelsUseCase(get()) }
    factory { GetHotelByIdUseCase(get()) }
    factory { GetHotelRoomsUseCase(get()) }
    factory { GetRoomByIdUseCase(get()) }
    factory { UploadAccommodationImagesUseCase(get()) }
    
    // Booking use cases
    factory { CreateBookingUseCase(get(), get()) }
    factory { GetUserBookingsUseCase(get()) }
    factory { CancelBookingUseCase(get(), get()) }
    factory { GetBookingByIdUseCase(get()) }
    factory { DeleteBookingUseCase(get(), get()) }
    
    // User use cases
    factory { GetUserProfileUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    
    // Bookmark use cases
    factory { AddBookmarkUseCase(get()) }
    factory { RemoveBookmarkUseCase(get()) }
    factory { GetUserBookmarksUseCase(get()) }
    
    // Review use cases
    factory { AggregateHotelRatingForHotelUseCase(get(), get()) }
    factory { CreateReviewUseCase(get(), get(), get()) }
    factory { GetHotelReviewsUseCase(get()) }
    factory { UpdateReviewUseCase(get(), get()) }
    factory { DeleteReviewUseCase(get(), get()) }
    
    // Voucher use cases
    factory { GetAvailableVouchersUseCase(get<VoucherRepository>()) }
    factory { ApplyVoucherToBookingUseCase(get<VoucherRepository>(), get<BookingRepository>()) }
    factory { GetVoucherByIdUseCase(get<VoucherRepository>()) }
    factory { ClaimVoucherUseCase(get<VoucherRepository>()) }
    factory { CheckVoucherEligibilityUseCase(get<VoucherRepository>()) }
    
    // VIP use cases
    factory { GetVipStatusUseCase(get<VipStatusRepository>()) }
    factory { GetVipBenefitsUseCase(get<VipStatusRepository>()) }
    factory { GetVipStatusHistoryUseCase(get<VipStatusRepository>()) }
    factory { CreateVipStatusUseCase(get<VipStatusRepository>(), get<BookingRepository>()) }
    factory { UpdateVipStatusUseCase(get<VipStatusRepository>()) }
    factory { AddVipStatusHistoryUseCase(get<VipStatusRepository>()) }
}
