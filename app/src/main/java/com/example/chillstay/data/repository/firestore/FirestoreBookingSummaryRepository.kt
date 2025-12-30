package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingSummary
import com.example.chillstay.domain.repository.BookingSummaryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
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

    override suspend fun getAllBookingSummaries(): List<BookingSummary> {
        return try {
            Log.d("BookingSummaryRepo", "Starting to load booking summaries...")
            val startTime = System.currentTimeMillis()

            // BƯỚC 1: Load tất cả bookings trước
            val bookingsSnapshot = firestore.collection("bookings")
                .get()
                .await()

            val bookings = bookingsSnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Booking::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    Log.e("BookingSummaryRepo", "Error parsing booking ${doc.id}: ${e.message}")
                    null
                }
            }

            Log.d("BookingSummaryRepo", "Loaded ${bookings.size} bookings")

            // BƯỚC 2: Collect tất cả unique IDs
            val userIds = bookings.map { it.userId }.distinct()
            val hotelIds = bookings.map { it.hotelId }.distinct()
            val roomIds = bookings.map { it.roomId }.distinct()

            Log.d("BookingSummaryRepo", "Unique IDs - Users: ${userIds.size}, Hotels: ${hotelIds.size}, Rooms: ${roomIds.size}")

            // BƯỚC 3: Load ALL data SONG SONG với coroutineScope
            coroutineScope {
                // Load users parallel
                val usersDeferred = async {
                    loadUserNames(userIds)
                }

                // Load hotels parallel
                val hotelsDeferred = async {
                    loadHotelNames(hotelIds)
                }

                // Load rooms parallel
                val roomsDeferred = async {
                    loadRoomNames(roomIds)
                }

                // Chờ tất cả hoàn thành
                usersDeferred.await()
                hotelsDeferred.await()
                roomsDeferred.await()
            }

            Log.d("BookingSummaryRepo", "Loaded all related data")

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
            Log.d("BookingSummaryRepo", "Completed in ${endTime - startTime}ms - Total: ${summaries.size} summaries")

            summaries
        } catch (e: Exception) {
            Log.e("BookingSummaryRepo", "Error loading booking summaries: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Load user names SONG SONG
     */
    private suspend fun loadUserNames(userIds: List<String>) = coroutineScope {
        userIds.map { userId ->
            async {
                // Skip nếu đã có trong cache
                if (!userNameCache.containsKey(userId)) {
                    try {
                        val userDoc = firestore.collection("users")
                            .document(userId)
                            .get()
                            .await()

                        val fullName = userDoc.getString("fullName") ?: "Unknown User"
                        userNameCache[userId] = fullName
                    } catch (e: Exception) {
                        Log.e("BookingSummaryRepo", "Error loading user $userId: ${e.message}")
                        userNameCache[userId] = "Unknown User"
                    }
                }
            }
        }.forEach { it.await() }
    }

    /**
     * Load hotel names SONG SONG
     */
    private suspend fun loadHotelNames(hotelIds: List<String>) = coroutineScope {
        hotelIds.map { hotelId ->
            async {
                // Skip nếu đã có trong cache
                if (!hotelNameCache.containsKey(hotelId)) {
                    try {
                        val hotelDoc = firestore.collection("hotels")
                            .document(hotelId)
                            .get()
                            .await()

                        val name = hotelDoc.getString("name") ?: "Unknown Hotel"
                        hotelNameCache[hotelId] = name
                    } catch (e: Exception) {
                        Log.e("BookingSummaryRepo", "Error loading hotel $hotelId: ${e.message}")
                        hotelNameCache[hotelId] = "Unknown Hotel"
                    }
                }
            }
        }.forEach { it.await() }
    }

    /**
     * Load room names SONG SONG
     */
    private suspend fun loadRoomNames(roomIds: List<String>) = coroutineScope {
        roomIds.map { roomId ->
            async {
                // Skip nếu đã có trong cache
                if (!roomNameCache.containsKey(roomId)) {
                    try {
                        val roomDoc = firestore.collection("rooms")
                            .document(roomId)
                            .get()
                            .await()

                        val name = roomDoc.getString("name") ?: "Unknown Room"
                        roomNameCache[roomId] = name
                    } catch (e: Exception) {
                        Log.e("BookingSummaryRepo", "Error loading room $roomId: ${e.message}")
                        roomNameCache[roomId] = "Unknown Room"
                    }
                }
            }
        }.forEach { it.await() }
    }

    fun clearCache() {
        userNameCache.clear()
        hotelNameCache.clear()
        roomNameCache.clear()
    }
}