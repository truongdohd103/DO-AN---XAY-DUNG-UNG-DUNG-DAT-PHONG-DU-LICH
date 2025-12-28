package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Voucher

interface VoucherRepository {
    suspend fun getVouchers(): List<Voucher>
    suspend fun getVoucherById(id: String): Voucher?
    suspend fun getVoucherByCode(code: String): Voucher?
    suspend fun createVoucher(voucher: Voucher): String
    suspend fun updateVoucher(voucher: Voucher)
    
    // Claim methods
    suspend fun getUserVouchers(userId: String): List<Voucher>
    suspend fun claimVoucher(voucherId: String, userId: String): Boolean
    suspend fun isVoucherClaimed(voucherId: String, userId: String): Boolean
    
    // Eligibility methods
    suspend fun checkVoucherEligibility(voucherId: String, userId: String): Pair<Boolean, String>
    suspend fun applyVoucherToHotels(voucherId: String, hotelIds: List<String>) : Boolean
    suspend fun updateVoucherAppliedHotels(voucherId: String, hotelIds: List<String>): Boolean
}
