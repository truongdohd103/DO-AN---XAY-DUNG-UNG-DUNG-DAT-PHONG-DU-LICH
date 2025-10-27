package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Voucher

interface VoucherRepository {
    suspend fun getVouchers(): List<Voucher>
    suspend fun getVoucherById(id: String): Voucher?
    suspend fun getVoucherByCode(code: String): Voucher?
    suspend fun createVoucher(voucher: Voucher): Voucher
    suspend fun updateVoucher(voucher: Voucher): Voucher
    
    // Claim methods
    suspend fun claimVoucher(voucherId: String, userId: String): Boolean
    suspend fun isVoucherClaimed(voucherId: String, userId: String): Boolean
    
    // Eligibility methods
    suspend fun checkVoucherEligibility(voucherId: String, userId: String): Pair<Boolean, String>
}
