package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingSummary
import com.example.chillstay.domain.repository.BookingSummaryRepository
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBookingSummaryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookingSummaryRepository {

    // Caches để tránh query trùng lặp
    private val userNameCache = mutableMapOf<String, String>()
    private val hotelNameCache = mutableMapOf<String, String>()
    private val roomNameCache = mutableMapOf<String, String>()

    companion object {
        private const val TAG = "BookingSummaryRepository"
        private const val WHERE_IN_MAX = 10
    }

    override suspend fun getAllBookingSummaries(): List<BookingSummary> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting to load booking summaries...")
                val startTime = System.currentTimeMillis()

                // BƯỚC 1: Load tất cả bookings trước
                val bookingsSnapshot = firestore.collection("bookings").get().await()

                val bookings = bookingsSnapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Booking::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing booking ${doc.id}: ${e.message}")
                        null
                    }
                }

                Log.d(TAG, "Loaded ${bookings.size} bookings")

                // BƯỚC 2: Collect tất cả unique IDs
                val userIds = bookings.map { it.userId }.distinct()
                val hotelIds = bookings.map { it.hotelId }.distinct()
                val roomIds = bookings.map { it.roomId }.distinct()

                Log.d(
                    TAG,
                    "Unique IDs - Users: ${userIds.size}, Hotels: ${hotelIds.size}, Rooms: ${roomIds.size}"
                )

                // BƯỚC 3: Load ALL data SONG SONG với coroutineScope
                coroutineScope {
                    val userLoad =
                        async { loadNamesByField("users", userIds, userNameCache, "fullName") }
                    val hotelLoad =
                        async { loadNamesByField("hotels", hotelIds, hotelNameCache, "name") }
                    val roomLoad =
                        async { loadNamesByField("rooms", roomIds, roomNameCache, "name") }


                    userLoad.await()
                    hotelLoad.await()
                    roomLoad.await()
                }

                Log.d(TAG, "Loaded all related data")

                // BƯỚC 4: Map sang BookingSummary với data đã cache
                val summaries = bookings.map { booking ->
                    BookingSummary(
                        id = booking.id,
                        userId = booking.userId,
                        userName = userNameCache[booking.userId],
                        hotelId = booking.hotelId,
                        hotelName = hotelNameCache[booking.hotelId],
                        roomId = booking.roomId,
                        roomName = roomNameCache[booking.roomId],
                        createdAt = booking.createdAt,
                        dateFrom = booking.dateFrom,
                        dateTo = booking.dateTo,
                        totalPrice = booking.totalPrice,
                        status = booking.status
                    )
                }

                val endTime = System.currentTimeMillis()
                Log.d(
                    TAG,
                    "Completed in ${endTime - startTime}ms - Total: ${summaries.size} summaries"
                )

                summaries
            } catch (e: Exception) {
                Log.e(TAG, "Error loading booking summaries: ${e.message}", e)
                emptyList()
            }
        }


    private suspend fun loadNamesByField(
        collectionName: String,
        ids: List<String>,
        cache: MutableMap<String, String>,
        fieldName: String
    ) {
        if (ids.isEmpty()) return
        // Only request ids not already in cache
        val idsToFetch = ids.filterNot { cache.containsKey(it) }
        if (idsToFetch.isEmpty()) return
        // chunk into batches of up to WHERE_IN_MAX
        val chunks = idsToFetch.chunked(WHERE_IN_MAX)

        supervisorScope {
            chunks.map { chunk ->
                async {
                    try {
                        val querySnapshot = firestore.collection(collectionName)
                            .whereIn(FieldPath.documentId(), chunk).get().await()

                        for (doc in querySnapshot.documents) {
                            val id = doc.id
                            val value = doc.getString(fieldName) ?: ""
                            if (value.isNotBlank()) cache[id] = value
                        }

                        // For any ids in the chunk not returned (missing docs), set a default
                        val returnedIds = querySnapshot.documents.map { it.id }.toSet()
                        chunk.forEach { id ->
                            if (!returnedIds.contains(id) && !cache.containsKey(id)) {
                                cache[id] = defaultNameForCollection(collectionName)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(
                            TAG,
                            "Error loading $collectionName for chunk ${chunk.joinToString()} : ${e.message}"
                        )
// fallback: mark chunk ids with default values so upper logic can proceed
                        chunk.forEach { id ->
                            if (!cache.containsKey(id)) {
                                cache[id] = defaultNameForCollection(collectionName)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun defaultNameForCollection(collectionName: String): String {
        return when (collectionName) {
            "users" -> "Unknown User"
            "hotels" -> "Unknown Hotel"
            "rooms" -> "Unknown Room"
            else -> "Unknown"
        }
    }
}