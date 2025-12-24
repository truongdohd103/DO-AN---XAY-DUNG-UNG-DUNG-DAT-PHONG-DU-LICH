package com.example.chillstay.di

import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.hoteldetail.HotelDetailViewModel
import com.example.chillstay.ui.bookmark.MyBookmarkViewModel
import com.example.chillstay.ui.trip.MyTripViewModel
import com.example.chillstay.ui.room.RoomViewModel
import com.example.chillstay.ui.booking.BookingViewModel
import com.example.chillstay.ui.voucher.VoucherViewModel
import com.example.chillstay.ui.voucher.VoucherDetailViewModel
import com.example.chillstay.ui.review.ReviewViewModel
import com.example.chillstay.ui.bill.BillViewModel
import com.example.chillstay.ui.vip.VipStatusViewModel
import com.example.chillstay.ui.auth.AuthViewModel
import com.example.chillstay.ui.profile.ProfileViewModel
import com.example.chillstay.ui.search.SearchViewModel
import com.example.chillstay.ui.roomgallery.RoomGalleryViewModel
import com.example.chillstay.ui.myreviews.MyReviewsViewModel
import com.example.chillstay.ui.allreviews.AllReviewsViewModel
import com.example.chillstay.ui.admin.home.AdminHomeViewModel
import com.example.chillstay.ui.admin.accommodation.accommodation_manage.AccommodationManageViewModel
import com.example.chillstay.ui.admin.accommodation.accommodation_edit.AccommodationEditViewModel
import com.example.chillstay.ui.admin.accommodation.room_manage.RoomManageViewModel
import com.example.chillstay.ui.admin.accommodation.room_edit.RoomEditViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        AuthViewModel(
            get(), // SignInUseCase
            get(), // SignUpUseCase
            get(), // SignOutUseCase
            get()  // GetCurrentUserIdUseCase
        )
    }
    viewModel { HomeViewModel(
            get(), // GetHotelsUseCase
            get(), // AddBookmarkUseCase
            get(), // RemoveBookmarkUseCase
            get(), // GetUserBookmarksUseCase
            get(), // GetUserBookingsUseCase
            get(), // GetHotelByIdUseCase
            get(), // GetRoomByIdUseCase
            get()  // GetCurrentUserIdUseCase
        )
    }
    viewModel { SearchViewModel(get()) }
    viewModel { 
        HotelDetailViewModel(
            get(),  // GetHotelByIdUseCase
            get(),  // GetRoomsByHotelIdUseCase
            get(),  // GetHotelReviewsUseCase
            get(),  // UserRepository
            get(),  // AddBookmarkUseCase
            get()   // RemoveBookmarkUseCase
        )
    }
    viewModel { MyBookmarkViewModel(get(), get(), get()) }
    viewModel { MyTripViewModel(get(), get(), get(), get(), get()) }
    viewModel { RoomViewModel(get(), get()) }
    viewModel { RoomGalleryViewModel(get(), get()) }
    viewModel { BookingViewModel(get(), get(), get(), get(), get()) }
    viewModel { VoucherViewModel(get(), get(), get(), get()) }
    viewModel { VoucherDetailViewModel(get(), get(), get()) }
    viewModel { ReviewViewModel(
        get(), // GetUserBookingsUseCase
        get(), // GetHotelByIdUseCase
        get(), // GetBookingByIdUseCase
        get(), // CreateReviewUseCase
        get(), // UpdateReviewUseCase
        get(), // GetCurrentUserIdUseCase
        get()  // ReviewRepository
    ) }
    viewModel { BillViewModel(get(), get()) }
    viewModel { VipStatusViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel {
        ProfileViewModel(
            get(), // GetCurrentUserIdUseCase
            get(), // GetUserProfileUseCase
            get()  // UpdateUserProfileUseCase
        )
    }
    viewModel { AdminHomeViewModel() }
    viewModel { MyReviewsViewModel(get(), get(), get()) }
    viewModel { AllReviewsViewModel(get(), get()) }
    viewModel { AccommodationManageViewModel(get()) }
    viewModel { AccommodationEditViewModel(get(), get(), get(), get(), get()) }
    viewModel { RoomManageViewModel(get()) }
    viewModel { RoomEditViewModel(get(), get(), get(), get()) }
}
