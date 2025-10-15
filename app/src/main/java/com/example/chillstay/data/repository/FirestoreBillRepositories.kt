package com.example.chillstay.data.repository

import com.example.chillstay.domain.model.Bill
import com.example.chillstay.domain.model.Payment
import com.example.chillstay.domain.repository.BillRepository
import com.example.chillstay.domain.repository.PaymentRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBillRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BillRepository {

    override suspend fun getBillById(id: String): Bill? {
        return try {
            val document = firestore.collection("bills")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Bill::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getBillsByBookingId(bookingId: String): List<Bill> {
        return try {
            val snapshot = firestore.collection("bills")
                .whereEqualTo("bookingId", bookingId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Bill::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createBill(bill: Bill): Bill {
        return try {
            val documentRef = firestore.collection("bills").add(bill).await()
            bill.copy(id = documentRef.id)
        } catch (e: Exception) {
            bill
        }
    }

    override suspend fun updateBill(bill: Bill): Bill {
        return try {
            firestore.collection("bills")
                .document(bill.id)
                .set(bill)
                .await()
            bill
        } catch (e: Exception) {
            bill
        }
    }

    override suspend fun deleteBill(id: String): Boolean {
        return try {
            firestore.collection("bills")
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

@Singleton
class FirestorePaymentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : PaymentRepository {

    override suspend fun getPaymentById(id: String): Payment? {
        return try {
            val document = firestore.collection("payments")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Payment::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPaymentsByBillId(billId: String): List<Payment> {
        return try {
            val snapshot = firestore.collection("payments")
                .whereEqualTo("billId", billId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Payment::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createPayment(payment: Payment): Payment {
        return try {
            val documentRef = firestore.collection("payments").add(payment).await()
            payment.copy(id = documentRef.id)
        } catch (e: Exception) {
            payment
        }
    }

    override suspend fun updatePayment(payment: Payment): Payment {
        return try {
            firestore.collection("payments")
                .document(payment.id)
                .set(payment)
                .await()
            payment
        } catch (e: Exception) {
            payment
        }
    }

    override suspend fun deletePayment(id: String): Boolean {
        return try {
            firestore.collection("payments")
                .document(id)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}






