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
import com.example.chillstay.ui.admin.booking.booking_manage.BookingManageViewModel
import com.example.chillstay.ui.admin.booking.booking_view.BookingViewViewModel
import com.example.chillstay.ui.admin.customer.customer_manage.CustomerManageViewModel
import com.example.chillstay.ui.admin.customer.customer_view.CustomerViewViewModel
import com.example.chillstay.ui.admin.customer.review_view.ReviewViewViewModel
import com.example.chillstay.ui.admin.voucher.voucher_apply.VoucherApplyViewModel
import com.example.chillstay.ui.admin.voucher.voucher_edit.VoucherEditViewModel
import com.example.chillstay.ui.admin.voucher.voucher_manage.VoucherManageViewModel
import com.example.chillstay.ui.admin.notification.AdminNotificationViewModel
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
    viewModel { VoucherViewModel(get()) }
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
    viewModel { BillViewModel(get(), get(), get()) }
    viewModel { VipStatusViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel {
        ProfileViewModel(
            get(), // GetCurrentUserIdUseCase
            get(), // GetUserByIdUseCase
            get()  // UpdateUserProfileUseCase
        )
    }
    viewModel { MyReviewsViewModel(get(), get(), get()) }
    viewModel { AllReviewsViewModel(get(), get()) }

    viewModel { AdminHomeViewModel(get()) }

    viewModel { AccommodationManageViewModel(get()) }
    viewModel { AccommodationEditViewModel(get(), get(), get(), get()) }
    viewModel { RoomManageViewModel(get()) }
    viewModel { RoomEditViewModel(get(), get(), get(), get()) }

    viewModel { CustomerManageViewModel(get(), get()) }
    viewModel { CustomerViewViewModel(get(), get(), get()) }
    viewModel { ReviewViewViewModel(get(), get(), get(), get()) }

    viewModel { BookingManageViewModel(get()) }
    viewModel { BookingViewViewModel(get(), get(), get(), get()) }

    viewModel { VoucherManageViewModel(get(), get(), get()) }
    viewModel { VoucherEditViewModel(get(), get(), get(), get()) }
    viewModel { VoucherApplyViewModel(get(), get(), get(), get()) }

    viewModel { AdminNotificationViewModel(get(), get()) }
}
