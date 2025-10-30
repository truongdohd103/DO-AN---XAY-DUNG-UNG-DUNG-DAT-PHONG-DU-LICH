package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.repository.BookingRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    override suspend fun createBooking(booking: Booking): Booking {
        return try {
            val documentRef = firestore.collection("bookings").add(booking).await()
            booking.copy(id = documentRef.id)
        } catch (e: Exception) {
            booking
        }
    }

    override suspend fun getUserBookings(userId: String, status: String?): List<Booking> {
        return try {
            Log.d("FirestoreBookingRepository", "Getting bookings for user: $userId, status: $status")
            
            var query = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
            
            status?.let { query = query.whereEqualTo("status", it) }
            
            val snapshot = query.get().await()
            
            Log.d("FirestoreBookingRepository", "Found ${snapshot.documents.size} booking documents")
            
            // Sort in memory instead of using orderBy to avoid index requirement
            val bookings = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Booking::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.e("FirestoreBookingRepository", "Error parsing booking ${document.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            Log.d("FirestoreBookingRepository", "Returning ${bookings.size} bookings")
            bookings
        } catch (e: Exception) {
            Log.e("FirestoreBookingRepository", "Error getting user bookings: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getBookingById(id: String): Booking? {
        return try {
            val document = firestore.collection("bookings")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Booking::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateBooking(booking: Booking): Booking {
        return try {
            firestore.collection("bookings")
                .document(booking.id)
                .set(booking)
                .await()
            booking
        } catch (e: Exception) {
            booking
        }
    }

    override suspend fun getBookingHotelId(bookingId: String): String? {
        return try {
            val document = firestore.collection("bookings")
                .document(bookingId)
                .get()
                .await()
            
            if (document.exists()) {
                document.getString("hotelId")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            Log.d("FirestoreBookingRepository", "Cancelling booking: $bookingId")
            
            // Update booking status to CANCELLED
            firestore.collection("bookings")
                .document(bookingId)
                .update(
                    "status", "CANCELLED",
                    "updatedAt", Timestamp.now()
                )
                .await()
            
            Log.d("FirestoreBookingRepository", "Successfully cancelled booking: $bookingId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreBookingRepository", "Error cancelling booking: ${e.message}")
            false
        }
    }
}
