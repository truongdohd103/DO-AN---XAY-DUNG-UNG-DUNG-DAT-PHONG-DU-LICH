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
import com.example.chillstay.domain.usecase.room.GetRoomsByHotelIdUseCase
import com.example.chillstay.domain.usecase.room.GetRoomByIdUseCase

// Booking use cases
import com.example.chillstay.domain.usecase.booking.CreateBookingUseCase
import com.example.chillstay.domain.usecase.booking.GetUserBookingsUseCase
import com.example.chillstay.domain.usecase.booking.CancelBookingUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingByIdUseCase
import com.example.chillstay.domain.usecase.booking.DeleteBookingUseCase

// User use cases
import com.example.chillstay.domain.usecase.user.GetUserByIdUseCase
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
import com.example.chillstay.domain.usecase.voucher.GetUserVouchersUseCase

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
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.ImageUploadRepository
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.usecase.booking.GetAllBookingSummariesUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingStatisticsByDateRangeUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingStatisticsByYearQuarterMonthUseCase
import com.example.chillstay.domain.usecase.booking.GetBookingsByRoomIdUseCase
import com.example.chillstay.domain.usecase.booking.GetCustomerStatisticsUseCase
import com.example.chillstay.domain.usecase.hotel.CreateHotelUseCase
import com.example.chillstay.domain.usecase.hotel.UpdateHotelUseCase
import com.example.chillstay.domain.usecase.image.UploadRoomImagesUseCase
import com.example.chillstay.domain.usecase.image.UploadVoucherImageUseCase
import com.example.chillstay.domain.usecase.review.GetReviewByIdUseCase
import com.example.chillstay.domain.usecase.room.CreateRoomUseCase
import com.example.chillstay.domain.usecase.room.UpdateRoomUseCase
import com.example.chillstay.domain.usecase.user.GetAllUsersUseCase
import com.example.chillstay.domain.usecase.user.GetCustomerActivitiesUseCase
import com.example.chillstay.domain.usecase.user.GetCustomerDetailsUseCase
import com.example.chillstay.domain.usecase.user.UpdateUserStatusUseCase
import com.example.chillstay.domain.usecase.voucher.ApplyVoucherToHotelsUseCase
import com.example.chillstay.domain.usecase.voucher.CreateVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.DeleteVoucherUseCase
import com.example.chillstay.domain.usecase.voucher.GetAllVouchersUseCase
import com.example.chillstay.domain.usecase.voucher.GetApplicableHotelsUseCase
import com.example.chillstay.domain.usecase.voucher.UpdateVoucherStatusUseCase
import com.example.chillstay.domain.usecase.voucher.UpdateVoucherUseCase

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
    factory { UploadAccommodationImagesUseCase(get()) }

    //Room Use cases
    factory { GetRoomsByHotelIdUseCase(get()) }
    factory { GetRoomByIdUseCase(get()) }
    factory { CreateRoomUseCase(get()) }
    factory { UpdateRoomUseCase(get()) }
    factory { UploadRoomImagesUseCase(get()) }

    // Booking use cases
    factory { CreateBookingUseCase(get(), get()) }
    factory { GetUserBookingsUseCase(get()) }
    factory { CancelBookingUseCase(get(), get()) }
    factory { GetBookingByIdUseCase(get()) }
    factory { DeleteBookingUseCase(get(), get()) }
    factory { GetAllBookingSummariesUseCase(get()) }
    factory { GetBookingsByRoomIdUseCase(get()) }

    //Statistics
    factory { GetBookingStatisticsByYearQuarterMonthUseCase(get()) }
    factory { GetBookingStatisticsByDateRangeUseCase(get()) }
    factory { GetCustomerStatisticsUseCase(get()) }
    
    // User use cases
    factory { GetUserByIdUseCase(get()) }
    factory { UpdateUserProfileUseCase(get()) }
    factory { UpdateUserStatusUseCase(get()) }
    factory { GetAllUsersUseCase(get()) }
    factory { GetCustomerActivitiesUseCase(get()) }
    factory { GetCustomerDetailsUseCase(get()) }
    
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
    factory { GetReviewByIdUseCase(get<ReviewRepository>()) }

    // Voucher use cases
    factory { GetAvailableVouchersUseCase(get<VoucherRepository>()) }
    factory { GetUserVouchersUseCase(get<VoucherRepository>()) }
    factory { ApplyVoucherToBookingUseCase(get<VoucherRepository>(), get<BookingRepository>()) }
    factory { GetVoucherByIdUseCase(get<VoucherRepository>()) }
    factory { ClaimVoucherUseCase(get<VoucherRepository>()) }
    factory { CheckVoucherEligibilityUseCase(get<VoucherRepository>()) }

    //Admin Voucher use cases
    factory { GetAllVouchersUseCase(get<VoucherRepository>()) }
    factory { CreateVoucherUseCase(get<VoucherRepository>()) }
    factory { UpdateVoucherUseCase(get<VoucherRepository>()) }
    factory { UpdateVoucherStatusUseCase(get<VoucherRepository>()) }
    factory { DeleteVoucherUseCase(get<VoucherRepository>()) }
    factory { UploadVoucherImageUseCase(get<ImageUploadRepository>()) }
    
    // VIP use cases
    factory { GetVipStatusUseCase(get<VipStatusRepository>()) }
    factory { GetVipBenefitsUseCase(get<VipStatusRepository>()) }
    factory { GetVipStatusHistoryUseCase(get<VipStatusRepository>()) }
    factory { CreateVipStatusUseCase(get<VipStatusRepository>(), get<BookingRepository>()) }
    factory { UpdateVipStatusUseCase(get<VipStatusRepository>()) }
    factory { AddVipStatusHistoryUseCase(get<VipStatusRepository>()) }
    factory { ApplyVoucherToHotelsUseCase(get<VoucherRepository>()) }
    factory { GetApplicableHotelsUseCase(get<VoucherRepository>(), get<HotelRepository>()) }
}
