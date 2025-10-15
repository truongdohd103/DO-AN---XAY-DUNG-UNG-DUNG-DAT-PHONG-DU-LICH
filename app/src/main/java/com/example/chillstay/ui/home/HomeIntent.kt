package com.example.chillstay.ui.home

sealed interface HomeIntent {
    data class ChangeHotelCategory(val categoryIndex: Int) : HomeIntent
    data class RefreshHotels(val categoryIndex: Int) : HomeIntent
    data class RetryLoadHotels(val categoryIndex: Int) : HomeIntent
}
