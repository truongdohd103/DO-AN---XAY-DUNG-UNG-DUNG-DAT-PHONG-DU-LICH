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
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { 
        HotelDetailViewModel(
            get(),  // GetHotelByIdUseCase
            get(),  // GetHotelRoomsUseCase
            get(),  // GetHotelReviewsUseCase
            get(),  // UserRepository
            get(),  // AddBookmarkUseCase
            get()   // RemoveBookmarkUseCase
        )
    }
    viewModel { MyBookmarkViewModel(get(), get(), get()) }
    viewModel { MyTripViewModel(get(), get(), get(), get()) }
    viewModel { RoomViewModel(get(), get()) }
    viewModel { BookingViewModel(get(), get(), get(), get(), get()) }
    viewModel { VoucherViewModel(get(), get(), get(), get()) }
    viewModel { VoucherDetailViewModel(get(), get(), get()) }
    viewModel { ReviewViewModel(get(), get()) }
    viewModel { BillViewModel(get(), get()) }
    viewModel { VipStatusViewModel(get(), get(), get(), get(), get(), get(), get()) }
}


