package com.example.chillstay.domain.repository

import com.example.chillstay.domain.model.Bill
import com.example.chillstay.domain.model.Payment

interface BillRepository {
    suspend fun getBillById(id: String): Bill?
    suspend fun getBillsByBookingId(bookingId: String): List<Bill>
    suspend fun createBill(bill: Bill): Bill
    suspend fun updateBill(bill: Bill): Bill
    suspend fun deleteBill(id: String): Boolean
}

interface PaymentRepository {
    suspend fun getPaymentById(id: String): Payment?
    suspend fun getPaymentsByBillId(billId: String): List<Payment>
    suspend fun createPayment(payment: Payment): Payment
    suspend fun updatePayment(payment: Payment): Payment
    suspend fun deletePayment(id: String): Boolean
}






