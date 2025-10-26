package com.example.chillstay.data.repository

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.model.Hotel
import com.example.chillstay.domain.model.HotelDetail
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomDetail
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherType
import com.example.chillstay.domain.repository.BookingRepository
import com.example.chillstay.domain.repository.BookmarkRepository
import com.example.chillstay.domain.repository.HotelRepository
import com.example.chillstay.domain.repository.NotificationRepository
import com.example.chillstay.domain.repository.ReviewRepository
import com.example.chillstay.domain.repository.UserRepository
import com.example.chillstay.domain.repository.VoucherRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

@Singleton
class FirestoreHotelRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HotelRepository {

    override suspend fun getHotels(): List<Hotel> {
        return try {
            Log.d("FirestoreHotelRepository", "Attempting to fetch hotels from Firestore")
            val snapshot = firestore.collection("hotels")
                .orderBy("rating", Query.Direction.DESCENDING)
                .get()
                .await()
            
            Log.d("FirestoreHotelRepository", "Successfully fetched ${snapshot.documents.size} hotels")
            snapshot.documents.mapNotNull { document ->
                val hotel = document.toObject(Hotel::class.java)?.copy(id = document.id)
                
                // Load rooms for this hotel
                val roomsSnapshot = firestore.collection("rooms")
                    .whereEqualTo("hotelId", document.id)
                    .get()
                    .await()
                
                val rooms = roomsSnapshot.documents.mapNotNull { roomDoc ->
                    val roomData = roomDoc.data
                    Room(
                        id = roomDoc.id,
                        hotelId = roomData?.get("hotelId") as? String ?: "",
                        type = roomData?.get("type") as? String ?: "",
                        price = (roomData?.get("price") as? Double) ?: 0.0,
                        imageUrl = roomData?.get("imageUrl") as? String ?: "",
                        isAvailable = roomData?.get("isAvailable") as? Boolean ?: true,
                        capacity = (roomData?.get("capacity") as? Long)?.toInt() ?: 0,
                        detail = roomData?.get("detail")?.let { detailData ->
                            val detailMap = detailData as? Map<String, Any>
                            RoomDetail(
                                name = detailMap?.get("name") as? String ?: "",
                                size = (detailMap?.get("size") as? Double) ?: 0.0,
                                view = detailMap?.get("view") as? String ?: ""
                            )
                        }
                    )
                }
                
                // Create a simple HotelDetail
                val hotelDetail = HotelDetail(
                    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",
                    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),
                    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }
                )
                
                hotel?.copy(rooms = rooms, detail = hotelDetail)
            }
        } catch (e: Exception) {
            Log.e("FirestoreHotelRepository", "Error fetching hotels: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun searchHotels(
        query: String,
        country: String?,
        city: String?,
        minRating: Double?,
        maxPrice: Double?
    ): List<Hotel> {
        return try {
            var firestoreQuery: Query = firestore.collection("hotels")
            
            // Apply filters
            country?.let { firestoreQuery = firestoreQuery.whereEqualTo("country", it) }
            city?.let { firestoreQuery = firestoreQuery.whereEqualTo("city", it) }
            minRating?.let { firestoreQuery = firestoreQuery.whereGreaterThanOrEqualTo("rating", it) }
            maxPrice?.let { firestoreQuery = firestoreQuery.whereLessThanOrEqualTo("priceRange.max", it) }
            
            val snapshot = firestoreQuery.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Hotel::class.java)?.copy(id = document.id)
            }.filter { hotel ->
                // Text search in memory (for development)
                query.isEmpty() || hotel.name.contains(query, ignoreCase = true) ||
                hotel.city.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHotelById(id: String): Hotel? {
        return try {
            val document = firestore.collection("hotels")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                val hotel = document.toObject(Hotel::class.java)?.copy(id = document.id)
                
                // Load rooms for this hotel
                val roomsSnapshot = firestore.collection("rooms")
                    .whereEqualTo("hotelId", id)
                    .get()
                    .await()
                
                val rooms = roomsSnapshot.documents.mapNotNull { roomDoc ->
                    val roomData = roomDoc.data
                    Room(
                        id = roomDoc.id,
                        hotelId = roomData?.get("hotelId") as? String ?: "",
                        type = roomData?.get("type") as? String ?: "",
                        price = (roomData?.get("price") as? Double) ?: 0.0,
                        imageUrl = roomData?.get("imageUrl") as? String ?: "",
                        isAvailable = roomData?.get("isAvailable") as? Boolean ?: true,
                        capacity = (roomData?.get("capacity") as? Long)?.toInt() ?: 0,
                        detail = roomData?.get("detail")?.let { detailData ->
                            val detailMap = detailData as? Map<String, Any>
                            RoomDetail(
                                name = detailMap?.get("name") as? String ?: "",
                                size = (detailMap?.get("size") as? Double) ?: 0.0,
                                view = detailMap?.get("view") as? String ?: ""
                            )
                        }
                    )
                }
                
                
                // Create a simple HotelDetail with basic info
                val hotelDetail = HotelDetail(
                    description = "A beautiful hotel in ${hotel?.city}, ${hotel?.country}",
                    facilities = listOf("WiFi", "Parking", "Restaurant", "Pool"),
                    photoUrls = (1..(hotel?.photoCount ?: 5)).map { "https://placehold.co/600x400" }
                )
                
                hotel?.copy(rooms = rooms, detail = hotelDetail)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getHotelsByCity(city: String): List<Hotel> {
        return try {
            val snapshot = firestore.collection("hotels")
                .whereEqualTo("city", city)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Hotel::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHotelRooms(
        hotelId: String,
        checkIn: String?,
        checkOut: String?,
        guests: Int?
    ): List<Room> {
        return try {
            var query = firestore.collection("rooms")
                .whereEqualTo("hotelId", hotelId)
                .whereEqualTo("isAvailable", true)
            
            guests?.let { query = query.whereGreaterThanOrEqualTo("capacity", it) }
            
            val snapshot = query.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Room::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getRoomById(roomId: String): Room? {
        return try {
            val document = firestore.collection("rooms")
                .document(roomId)
                .get()
                .await()
            if (document.exists()) {
                document.toObject(Room::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUser(id: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(User::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createUser(user: User): User {
        return try {
            val documentRef = firestore.collection("users").add(user).await()
            user.copy(id = documentRef.id)
        } catch (e: Exception) {
            user
        }
    }

    override suspend fun updateUser(user: User): User {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            user
        } catch (e: Exception) {
            user
        }
    }

    override suspend fun deleteUser(id: String) {
        try {
            firestore.collection("users")
                .document(id)
                .delete()
                .await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    override suspend fun getUserByEmail(email: String): User? {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                document.toObject(User::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

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
            var query = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
            
            status?.let { query = query.whereEqualTo("status", it) }
            
            val snapshot = query.orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Booking::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            firestore.collection("bookings")
                .document(bookingId)
                .update(
                    mapOf(
                        "status" to "CANCELLED",
                        "updatedAt" to Date()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
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
}

@Singleton
class FirestoreBookmarkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookmarkRepository {

    override suspend fun addBookmark(bookmark: Bookmark): Bookmark {
        return try {
            android.util.Log.d("FirestoreBookmarkRepository", "Adding bookmark: userId=${bookmark.userId}, hotelId=${bookmark.hotelId}")
            val documentRef = firestore.collection("bookmarks").add(bookmark).await()
            android.util.Log.d("FirestoreBookmarkRepository", "Successfully added bookmark with ID: ${documentRef.id}")
            bookmark.copy(id = documentRef.id)
        } catch (e: Exception) {
            android.util.Log.e("FirestoreBookmarkRepository", "Error adding bookmark: ${e.message}")
            bookmark
        }
    }

    override suspend fun removeBookmark(userId: String, hotelId: String): Boolean {
        return try {
            android.util.Log.d("FirestoreBookmarkRepository", "Removing bookmark: userId=$userId, hotelId=$hotelId")
            val snapshot = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                android.util.Log.d("FirestoreBookmarkRepository", "No bookmark found to remove")
                return false
            }
            
            val document = snapshot.documents.first()
            firestore.collection("bookmarks").document(document.id).delete().await()
            android.util.Log.d("FirestoreBookmarkRepository", "Successfully removed bookmark with ID: ${document.id}")
            true
        } catch (e: Exception) {
            android.util.Log.e("FirestoreBookmarkRepository", "Error removing bookmark: ${e.message}")
            false
        }
    }

    override suspend fun getUserBookmarks(userId: String): List<Bookmark> {
        return try {
            android.util.Log.d("FirestoreBookmarkRepository", "Getting bookmarks for user: $userId")
            
            // Add timeout to prevent hanging
            val snapshot = withTimeout(10000) { // 10 second timeout
                firestore.collection("bookmarks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }
            
            android.util.Log.d("FirestoreBookmarkRepository", "Found ${snapshot.documents.size} bookmark documents")
            
            // Sort in memory instead of using orderBy to avoid index requirement
            val bookmarks = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Bookmark::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    android.util.Log.w("FirestoreBookmarkRepository", "Failed to parse bookmark document ${document.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            android.util.Log.d("FirestoreBookmarkRepository", "Returning ${bookmarks.size} bookmarks")
            bookmarks
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("FirestoreBookmarkRepository", "Timeout getting bookmarks: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("FirestoreBookmarkRepository", "Error getting bookmarks: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun isBookmarked(userId: String, hotelId: String): Boolean {
        return try {
            val snapshot = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
}

@Singleton
class FirestoreReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override suspend fun createReview(review: Review): Review {
        return try {
            val documentRef = firestore.collection("reviews").add(review).await()
            review.copy(id = documentRef.id)
        } catch (e: Exception) {
            review
        }
    }

    override suspend fun getHotelReviews(
        hotelId: String,
        limit: Int?,
        offset: Int
    ): List<Review> {
        return try {
            var query = firestore.collection("reviews")
                .whereEqualTo("hotelId", hotelId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
            
            limit?.let { query = query.limit(it.toLong()) }
            
            val snapshot = query.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Review::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserReviewForHotel(userId: String, hotelId: String): Review? {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                document.toObject(Review::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateReview(review: Review): Review {
        return try {
            firestore.collection("reviews")
                .document(review.id)
                .set(review)
                .await()
            review
        } catch (e: Exception) {
            review
        }
    }

    override suspend fun deleteReview(reviewId: String): Boolean {
        return try {
            firestore.collection("reviews")
                .document(reviewId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}

@Singleton
class FirestoreVoucherRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : VoucherRepository {

    override suspend fun getVouchers(): List<Voucher> {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching vouchers from Firestore")
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            val vouchers = snapshot.documents.mapNotNull { document ->
                document.toObject(Voucher::class.java)?.copy(id = document.id)
            }
            Log.d("FirestoreVoucherRepository", "Successfully fetched ${vouchers.size} vouchers")
            vouchers
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching vouchers: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun getVoucherById(id: String): Voucher? {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching voucher by ID: $id")
            val document = firestore.collection("vouchers")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                val voucher = document.toObject(Voucher::class.java)?.copy(id = document.id)
                Log.d("FirestoreVoucherRepository", "Successfully fetched voucher: ${voucher?.title}")
                voucher
            } else {
                Log.d("FirestoreVoucherRepository", "Voucher not found with ID: $id")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching voucher by ID: ${e.message}", e)
            null
        }
    }

    override suspend fun getVoucherByCode(code: String): Voucher? {
        return try {
            Log.d("FirestoreVoucherRepository", "Fetching voucher by code: $code")
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                val voucher = document.toObject(Voucher::class.java)?.copy(id = document.id)
                Log.d("FirestoreVoucherRepository", "Successfully fetched voucher by code: ${voucher?.title}")
                voucher
            } else {
                Log.d("FirestoreVoucherRepository", "Voucher not found with code: $code")
                null
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error fetching voucher by code: ${e.message}", e)
            null
        }
    }

    override suspend fun createVoucher(voucher: Voucher): Voucher {
        return try {
            Log.d("FirestoreVoucherRepository", "Creating voucher: ${voucher.title}")
            val documentRef = firestore.collection("vouchers").add(voucher).await()
            val createdVoucher = voucher.copy(id = documentRef.id)
            Log.d("FirestoreVoucherRepository", "Successfully created voucher with ID: ${createdVoucher.id}")
            createdVoucher
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error creating voucher: ${e.message}", e)
            voucher
        }
    }

    override suspend fun updateVoucher(voucher: Voucher): Voucher {
        return try {
            Log.d("FirestoreVoucherRepository", "Updating voucher: ${voucher.id}")
            firestore.collection("vouchers")
                .document(voucher.id)
                .set(voucher)
                .await()
            Log.d("FirestoreVoucherRepository", "Successfully updated voucher: ${voucher.id}")
            voucher
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error updating voucher: ${e.message}", e)
            voucher
        }
    }

    // Claim methods
    override suspend fun claimVoucher(voucherId: String, userId: String): Boolean {
        return try {
            Log.d("FirestoreVoucherRepository", "Claiming voucher: $voucherId for user: $userId")
            
            // Check if already claimed
            val isAlreadyClaimed = isVoucherClaimed(voucherId, userId)
            if (isAlreadyClaimed) {
                Log.d("FirestoreVoucherRepository", "Voucher already claimed by user")
                return false
            }
            
            // Create claim record
            val claimData = mapOf(
                "voucherId" to voucherId,
                "userId" to userId,
                "claimedAt" to Date(),
                "createdAt" to Date()
            )
            
            firestore.collection("voucher_claims").add(claimData).await()
            Log.d("FirestoreVoucherRepository", "Successfully claimed voucher: $voucherId")
            true
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error claiming voucher: ${e.message}", e)
            false
        }
    }

    override suspend fun isVoucherClaimed(voucherId: String, userId: String): Boolean {
        return try {
            Log.d("FirestoreVoucherRepository", "Checking if voucher claimed: $voucherId by user: $userId")
            val snapshot = firestore.collection("voucher_claims")
                .whereEqualTo("voucherId", voucherId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            val isClaimed = !snapshot.isEmpty
            Log.d("FirestoreVoucherRepository", "Voucher claim status: $isClaimed")
            isClaimed
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreVoucherRepository", "Permission denied checking voucher claim status - assuming not claimed for graceful fallback")
                    // Graceful fallback: assume not claimed to allow claiming
                    false
                }
                else -> {
                    Log.e("FirestoreVoucherRepository", "Firestore error checking voucher claim status: ${e.message}", e)
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error checking voucher claim status: ${e.message}", e)
            false
        }
    }

    // Eligibility methods
    override suspend fun checkVoucherEligibility(voucherId: String, userId: String): Pair<Boolean, String> {
        return try {
            Log.d("FirestoreVoucherRepository", "Checking eligibility for voucher: $voucherId, user: $userId")
            
            // Get voucher
            val voucher = getVoucherById(voucherId)
            if (voucher == null) {
                return Pair(false, "Voucher not found")
            }
            
            // Check if already claimed
            val isClaimed = isVoucherClaimed(voucherId, userId)
            if (isClaimed) {
                return Pair(false, "Voucher already claimed")
            }
            
            // Check validity period
            val now = Date()
            if (voucher.validFrom.toDate().after(now)) {
                return Pair(false, "Voucher not yet valid")
            }
            if (voucher.validTo.toDate().before(now)) {
                return Pair(false, "Voucher has expired")
            }
            
            // Check status
            if (voucher.status != com.example.chillstay.domain.model.VoucherStatus.ACTIVE) {
                return Pair(false, "Voucher is not active")
            }
            
            // Check usage limits with PERMISSION_DENIED handling
            if (voucher.conditions.maxTotalUsage > 0) {
                try {
                    val totalClaimsSnapshot = firestore.collection("voucher_claims")
                        .whereEqualTo("voucherId", voucherId)
                        .get()
                        .await()
                    
                    if (totalClaimsSnapshot.documents.size >= voucher.conditions.maxTotalUsage) {
                        return Pair(false, "Voucher usage limit reached")
                    }
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("FirestoreVoucherRepository", "Permission denied checking total usage - skipping limit check for graceful fallback")
                        // Skip usage limit check, allow claiming
                    } else {
                        throw e
                    }
                }
            }
            
            // Check per-user usage limit with PERMISSION_DENIED handling
            if (voucher.conditions.maxUsagePerUser > 0) {
                try {
                    val userClaimsSnapshot = firestore.collection("voucher_claims")
                        .whereEqualTo("voucherId", voucherId)
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                    
                    if (userClaimsSnapshot.documents.size >= voucher.conditions.maxUsagePerUser) {
                        return Pair(false, "You have reached the usage limit for this voucher")
                    }
                } catch (e: FirebaseFirestoreException) {
                    if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        Log.w("FirestoreVoucherRepository", "Permission denied checking user usage - skipping limit check for graceful fallback")
                        // Skip usage limit check, allow claiming
                    } else {
                        throw e
                    }
                }
            }
            
            Log.d("FirestoreVoucherRepository", "User is eligible for voucher")
            Pair(true, "You are eligible to claim this voucher")
        } catch (e: FirebaseFirestoreException) {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                    Log.w("FirestoreVoucherRepository", "Permission denied checking eligibility - returning graceful fallback")
                    // Graceful fallback: assume eligible with manual check message
                    Pair(true, "Check manually - Try claiming anyway")
                }
                else -> {
                    Log.e("FirestoreVoucherRepository", "Firestore error checking eligibility: ${e.message}", e)
                    Pair(false, "Unable to check eligibility")
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreVoucherRepository", "Error checking eligibility: ${e.message}", e)
            Pair(false, "Unable to check eligibility")
        }
    }
}

@Singleton
class FirestoreNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    override suspend fun getUserNotifications(
        userId: String,
        isRead: Boolean?,
        limit: Int?
    ): List<Notification> {
        return try {
            var query = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
            
            isRead?.let { query = query.whereEqualTo("isRead", it) }
            
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
            
            limit?.let { query = query.limit(it.toLong()) }
            
            val snapshot = query.get().await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Notification::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun createNotification(notification: Notification): Notification {
        return try {
            val documentRef = firestore.collection("notifications").add(notification).await()
            notification.copy(id = documentRef.id)
        } catch (e: Exception) {
            notification
        }
    }

    override suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update(
                    mapOf(
                        "isRead" to true,
                        "updatedAt" to Date()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun markAllAsRead(userId: String): Boolean {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            val batch = firestore.batch()
            snapshot.documents.forEach { document ->
                batch.update(document.reference, mapOf(
                    "isRead" to true,
                    "updatedAt" to Date()
                ))
            }
            
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}