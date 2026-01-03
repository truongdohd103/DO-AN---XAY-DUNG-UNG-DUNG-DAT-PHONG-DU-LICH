package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingPreferences
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.PaymentMethod
import com.example.chillstay.domain.repository.BookingRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBookingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingRepository {

    // Caches để tránh query trùng lặp
    private val hotelCache = mutableMapOf<String, Map<String, Any?>>()
    private val roomCache = mutableMapOf<String, Map<String, Any?>>()

    companion object {
        private const val TAG = "FirestoreBookingRepo"
        private const val WHERE_IN_MAX = 10
    }

    override suspend fun getAllBookings(): List<Booking> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting to load all bookings...")
            val startTime = System.currentTimeMillis()

            // BƯỚC 1: Load tất cả bookings
            val snapshot = firestore.collection("bookings")
                .orderBy("dateFrom", Query.Direction.DESCENDING)
                .get()
                .await()

            val bookingDocs = snapshot.documents
            Log.d(TAG, "Loaded ${bookingDocs.size} booking documents")

            if (bookingDocs.isEmpty()) {
                return@withContext emptyList()
            }

            // BƯỚC 2: Collect unique IDs
            val hotelIds = bookingDocs.mapNotNull { it.getString("hotelId") }.distinct()
            val roomIds = bookingDocs.mapNotNull { it.getString("roomId") }.distinct()

            Log.d(TAG, "Unique IDs - Hotels: ${hotelIds.size}, Rooms: ${roomIds.size}")

            // BƯỚC 3: Load related data SONG SONG
            coroutineScope {
                val hotelLoad = async { loadHotelData(hotelIds) }
                val roomLoad = async { loadRoomData(roomIds) }

                hotelLoad.await()
                roomLoad.await()
            }

            Log.d(TAG, "Loaded all related data")

            // BƯỚC 4: Map sang Booking với data đã cache
            val bookings = withContext(Dispatchers.Default) {
                bookingDocs.mapNotNull { doc ->
                    mapBookingDocumentWithCache(doc)
                }
            }

            val endTime = System.currentTimeMillis()
            Log.d(TAG, "Completed in ${endTime - startTime}ms - Total: ${bookings.size} bookings")

            bookings
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching bookings: ${e.message}", e)
            emptyList()
        }
    }

    private suspend fun loadHotelData(hotelIds: List<String>) {
        if (hotelIds.isEmpty()) return

        val idsToFetch = hotelIds.filterNot { hotelCache.containsKey(it) }
        if (idsToFetch.isEmpty()) return

        val chunks = idsToFetch.chunked(WHERE_IN_MAX)

        supervisorScope {
            chunks.map { chunk ->
                async {
                    try {
                        val querySnapshot = firestore.collection("hotels")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        for (doc in querySnapshot.documents) {
                            hotelCache[doc.id] = doc.data ?: emptyMap()
                        }

                        // Mark missing docs
                        val returnedIds = querySnapshot.documents.map { it.id }.toSet()
                        chunk.forEach { id ->
                            if (!returnedIds.contains(id) && !hotelCache.containsKey(id)) {
                                hotelCache[id] = emptyMap()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading hotels for chunk: ${e.message}")
                        chunk.forEach { id ->
                            if (!hotelCache.containsKey(id)) {
                                hotelCache[id] = emptyMap()
                            }
                        }
                    }
                }
            }.forEach { it.await() }
        }
    }

    private suspend fun loadRoomData(roomIds: List<String>) {
        if (roomIds.isEmpty()) return

        val idsToFetch = roomIds.filterNot { roomCache.containsKey(it) }
        if (idsToFetch.isEmpty()) return

        val chunks = idsToFetch.chunked(WHERE_IN_MAX)

        supervisorScope {
            chunks.map { chunk ->
                async {
                    try {
                        val querySnapshot = firestore.collection("rooms")
                            .whereIn(FieldPath.documentId(), chunk)
                            .get()
                            .await()

                        for (doc in querySnapshot.documents) {
                            roomCache[doc.id] = doc.data ?: emptyMap()
                        }

                        // Mark missing docs
                        val returnedIds = querySnapshot.documents.map { it.id }.toSet()
                        chunk.forEach { id ->
                            if (!returnedIds.contains(id) && !roomCache.containsKey(id)) {
                                roomCache[id] = emptyMap()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error loading rooms for chunk: ${e.message}")
                        chunk.forEach { id ->
                            if (!roomCache.containsKey(id)) {
                                roomCache[id] = emptyMap()
                            }
                        }
                    }
                }
            }.forEach { it.await() }
        }
    }

    private fun mapBookingDocumentWithCache(doc: DocumentSnapshot): Booking? {
        val data = doc.data ?: return null

        fun Any?.toDoubleSafe(): Double = when (this) {
            is Number -> this.toDouble()
            is String -> this.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        fun Any?.toIntSafe(default: Int = 0): Int = when (this) {
            is Number -> this.toInt()
            is String -> this.toIntOrNull() ?: default
            else -> default
        }

        val status = (data["status"] as? String)?.let {
            try { BookingStatus.valueOf(it) } catch (_: Exception) { BookingStatus.PENDING }
        } ?: BookingStatus.PENDING

        val paymentMethod = (data["paymentMethod"] as? String)?.let {
            try { PaymentMethod.valueOf(it) } catch (_: Exception) { PaymentMethod.CREDIT_CARD }
        } ?: PaymentMethod.CREDIT_CARD

        val prefMap = data["preferences"] as? Map<*, *>
        val preferences = BookingPreferences(
            highFloor = prefMap?.get("highFloor") as? Boolean ?: false,
            quietRoom = prefMap?.get("quietRoom") as? Boolean ?: false,
            extraPillows = prefMap?.get("extraPillows") as? Boolean ?: false,
            airportShuttle = prefMap?.get("airportShuttle") as? Boolean ?: false,
            earlyCheckIn = prefMap?.get("earlyCheckIn") as? Boolean ?: false,
            lateCheckOut = prefMap?.get("lateCheckOut") as? Boolean ?: false
        )

        val appliedVouchers = (data["appliedVouchers"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

        val dateFrom = when (val v = data["dateFrom"]) {
            is Timestamp -> v.toDate().toString()
            is String -> v
            else -> ""
        }
        val dateTo = when (val v = data["dateTo"]) {
            is Timestamp -> v.toDate().toString()
            is String -> v
            else -> ""
        }

        return Booking(
            id = doc.id,
            userId = data["userId"] as? String ?: "",
            hotelId = data["hotelId"] as? String ?: "",
            roomId = data["roomId"] as? String ?: "",
            dateFrom = dateFrom,
            dateTo = dateTo,
            guests = data["guests"].toIntSafe(1),
            adults = data["adults"].toIntSafe(1),
            children = data["children"].toIntSafe(0),
            rooms = data["rooms"].toIntSafe(1),
            price = data["price"].toDoubleSafe(),
            originalPrice = data["originalPrice"].toDoubleSafe(),
            discount = data["discount"].toDoubleSafe(),
            serviceFee = data["serviceFee"].toDoubleSafe(),
            taxes = data["taxes"].toDoubleSafe(),
            totalPrice = data["totalPrice"].toDoubleSafe(),
            status = status,
            paymentMethod = paymentMethod,
            specialRequests = data["specialRequests"] as? String ?: "",
            preferences = preferences,
            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
            updatedAt = data["updatedAt"] as? Timestamp ?: Timestamp.now(),
            appliedVouchers = appliedVouchers,
            hotel = null,
            room = null,
            inventoryReserved = data["inventoryReserved"] as? Boolean ?: false
        )
    }

    override suspend fun createBooking(booking: Booking): Booking {
        return try {
            val documentRef = firestore.collection("bookings").add(booking).await()
            booking.copy(id = documentRef.id)
        } catch (_: Exception) {
            booking
        }
    }

    override suspend fun getUserBookings(userId: String, status: String?): List<Booking> {
        return try {
            Log.d(TAG, "Getting bookings for user: $userId, status: $status")

            var query = firestore.collection("bookings")
                .whereEqualTo("userId", userId)

            status?.let { query = query.whereEqualTo("status", it) }

            val snapshot = query.get().await()

            Log.d(TAG, "Found ${snapshot.documents.size} booking documents")

            val bookings = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Booking::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing booking ${document.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }

            Log.d(TAG, "Returning ${bookings.size} bookings")
            bookings
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user bookings: ${e.message}")
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            Log.d(TAG, "Cancelling booking: $bookingId")

            firestore.collection("bookings")
                .document(bookingId)
                .update(
                    "status", "CANCELLED",
                    "updatedAt", Timestamp.now()
                )
                .await()

            Log.d(TAG, "Successfully cancelled booking: $bookingId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling booking: ${e.message}")
            false
        }
    }

    override suspend fun deleteBooking(bookingId: String): Boolean {
        return try {
            firestore.collection("bookings")
                .document(bookingId)
                .delete()
                .await()
            true
        } catch (_: Exception) {
            false
        }
    }
}