package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Voucher

interface VoucherRepository {
    suspend fun getVouchers(): List<Voucher>
    suspend fun getVoucherById(id: String): Voucher?
    suspend fun getVoucherByCode(code: String): Voucher?
    suspend fun createVoucher(voucher: Voucher): Voucher
    suspend fun updateVoucher(voucher: Voucher): Voucher
}
