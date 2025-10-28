package com.example.chillstay.data.repository.firestore

import com.example.chillstay.domain.model.Bill
import com.example.chillstay.domain.repository.BillRepository
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
