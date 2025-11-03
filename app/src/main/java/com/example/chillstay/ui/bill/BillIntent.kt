package com.example.chillstay.ui.bill

sealed interface BillIntent {
    data class LoadBillDetails(val bookingId: String) : BillIntent
    object RetryLoad : BillIntent
    object DownloadBill : BillIntent
    object ShareBill : BillIntent
}

