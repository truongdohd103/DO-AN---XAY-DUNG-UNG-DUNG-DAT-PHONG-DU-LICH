package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.ActivityType
import com.example.chillstay.domain.model.Booking
import com.example.chillstay.domain.model.BookingStatus
import com.example.chillstay.domain.model.CustomerActivity
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ActivityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreActivityRepository @Inject constructor(private val firestore: FirebaseFirestore) : ActivityRepository {

    // Cache for hotel names to avoid repeated queries
    private val hotelNameCache = mutableMapOf<String, String>()

    override suspend fun getCustomerActivities(userId: String, type: String?): List<CustomerActivity> {
        return try {
            Log.d("FirestoreActivityRepository", "Getting activities for user $userId, type: $type")

            // Load bookings and reviews in parallel using coroutineScope and async
            val activities = coroutineScope {
                val bookingsDeferred = async {
                    if (type == null || type == "BOOKING") {
                        getBookingActivities(userId)
                    } else {
                        emptyList()
                    }
                }

                val reviewsDeferred = async {
                    if (type == null || type == "REVIEW") {
                        getReviewActivities(userId)
                    } else {
                        emptyList()
                    }
                }

                // Wait for both to complete
                val bookings = bookingsDeferred.await()
                val reviews = reviewsDeferred.await()

                bookings + reviews
            }

            // Sort by createdAt descending
            val sorted = activities.sortedByDescending { it.createdAt }

            Log.d("FirestoreActivityRepository", "Found ${sorted.size} activities")
            sorted
        } catch (e: Exception) {
            Log.e("FirestoreActivityRepository", "Error getting activities: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getBookingActivities(userId: String): List<CustomerActivity> {
        return try {
            val snapshot = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Collect all unique hotel IDs first
            val hotelIds = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.hotelId
            }.distinct()

            // Fetch all hotel names in parallel
            coroutineScope {
                hotelIds.map { hotelId ->
                    async {
                        if (!hotelNameCache.containsKey(hotelId)) {
                            try {
                                val hotelDoc = firestore.collection("hotels")
                                    .document(hotelId)
                                    .get()
                                    .await()
                                hotelNameCache[hotelId] = hotelDoc.getString("name") ?: "Unknown Hotel"
                            } catch (_: Exception) {
                                hotelNameCache[hotelId] = "Unknown Hotel"
                            }
                        }
                    }
                }.forEach { it.await() }
            }

            // Now create activities with cached hotel names
            snapshot.documents.mapNotNull { doc ->
                try {
                    val booking = doc.toObject(Booking::class.java)?.copy(id = doc.id)
                    booking?.let { createBookingActivity(it) }
                } catch (e: Exception) {
                    Log.e("FirestoreActivityRepository", "Error parsing booking: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreActivityRepository", "Error getting bookings: ${e.message}")
            emptyList()
        }
    }

    private suspend fun getReviewActivities(userId: String): List<CustomerActivity> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Collect all unique hotel IDs first
            val hotelIds = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Review::class.java)?.hotelId
            }.distinct()

            // Fetch all hotel names in parallel
            coroutineScope {
                hotelIds.map { hotelId ->
                    async {
                        if (!hotelNameCache.containsKey(hotelId)) {
                            try {
                                val hotelDoc = firestore.collection("hotels")
                                    .document(hotelId)
                                    .get()
                                    .await()
                                hotelNameCache[hotelId] = hotelDoc.getString("name") ?: "Unknown Hotel"
                            } catch (_: Exception) {
                                hotelNameCache[hotelId] = "Unknown Hotel"
                            }
                        }
                    }
                }.forEach { it.await() }
            }

            // Now create activities with cached hotel names
            snapshot.documents.mapNotNull { doc ->
                try {
                    val review = doc.toObject(Review::class.java)?.copy(id = doc.id)
                    review?.let { createReviewActivity(it) }
                } catch (e: Exception) {
                    Log.e("FirestoreActivityRepository", "Error parsing review: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreActivityRepository", "Error getting reviews: ${e.message}")
            emptyList()
        }
    }

    private fun createBookingActivity(booking: Booking): CustomerActivity {
        // Get hotel name from cache
        val hotelName = hotelNameCache[booking.hotelId] ?: "Unknown Hotel"

        val title = when (booking.status) {
            BookingStatus.COMPLETED -> "Booking Completed"
            BookingStatus.CANCELLED -> "Booking Cancelled"
            BookingStatus.CONFIRMED -> "New Booking"
            else -> "Booking ${booking.status.name.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
            }}"
        }

        val description = when (booking.status) {
            BookingStatus.COMPLETED -> "Completed stay at $hotelName"
            BookingStatus.CANCELLED -> "Cancelled booking at $hotelName"
            BookingStatus.CONFIRMED -> "Booked $hotelName for ${booking.dateFrom} - ${booking.dateTo}"
            else -> "Status: ${booking.status.name}"
        }

        return CustomerActivity(
            id = booking.id,
            userId = booking.userId,
            type = ActivityType.BOOKING,
            title = title,
            description = description,
            relatedId = booking.id,
            createdAt = booking.createdAt
        )
    }

    private fun createReviewActivity(review: Review): CustomerActivity {
        // Get hotel name from cache
        val hotelName = hotelNameCache[review.hotelId] ?: "Unknown Hotel"

        return CustomerActivity(
            id = review.id,
            userId = review.userId,
            type = ActivityType.REVIEW,
            title = "Review Posted",
            description = "Rated ${review.rating} stars for $hotelName",
            relatedId = review.id,
            createdAt = review.createdAt
        )
    }
}