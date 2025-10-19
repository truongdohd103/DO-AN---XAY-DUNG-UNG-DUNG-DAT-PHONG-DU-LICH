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
import kotlinx.coroutines.tasks.await
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
            val documentRef = firestore.collection("bookmarks").add(bookmark).await()
            bookmark.copy(id = documentRef.id)
        } catch (e: Exception) {
            bookmark
        }
    }

    override suspend fun removeBookmark(userId: String, hotelId: String): Boolean {
        return try {
            val snapshot = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                return false
            }
            
            val document = snapshot.documents.first()
            firestore.collection("bookmarks").document(document.id).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserBookmarks(userId: String): List<Bookmark> {
        return try {
            val snapshot = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Bookmark::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
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
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Voucher::class.java)?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getVoucherById(id: String): Voucher? {
        return try {
            val document = firestore.collection("vouchers")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                document.toObject(Voucher::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getVoucherByCode(code: String): Voucher? {
        return try {
            val snapshot = firestore.collection("vouchers")
                .whereEqualTo("code", code)
                .whereEqualTo("status", "ACTIVE")
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val document = snapshot.documents.first()
                document.toObject(Voucher::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createVoucher(voucher: Voucher): Voucher {
        return try {
            val documentRef = firestore.collection("vouchers").add(voucher).await()
            voucher.copy(id = documentRef.id)
        } catch (e: Exception) {
            voucher
        }
    }

    override suspend fun updateVoucher(voucher: Voucher): Voucher {
        return try {
            firestore.collection("vouchers")
                .document(voucher.id)
                .set(voucher)
                .await()
            voucher
        } catch (e: Exception) {
            voucher
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