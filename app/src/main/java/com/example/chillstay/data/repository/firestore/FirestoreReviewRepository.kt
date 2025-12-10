package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreReviewRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    override suspend fun createReview(review: Review): Review {
        return try {
            val documentRef = firestore.collection("reviews").add(review).await()
            review.copy(id = documentRef.id)
        } catch (_: Exception) {
            review
        }
    }

    override suspend fun getHotelReviews(hotelId: String, offset: Int): List<Review> {
        return try {
            Log.d("FirestoreReviewRepository", "Getting reviews for hotelId: $hotelId")
            
            val query = firestore.collection("reviews")
                .whereEqualTo("hotelId", hotelId)
            val snapshot = query.get().await()
            val reviews = snapshot.documents.mapNotNull { document ->
                try {
                    val review = document.toObject(Review::class.java)?.copy(id = document.id)
                    if (review != null) {
                        Log.d("FirestoreReviewRepository", "Parsed review: id=${review.id}, userId=${review.userId}, rating=${review.rating}, comment=${review.comment.take(50)}...")
                    }
                    review
                } catch (e: Exception) {
                    Log.e("FirestoreReviewRepository", "Error parsing review document ${document.id}: ${e.message}", e)
                    null
                }
            }
            
            Log.d("FirestoreReviewRepository", "Returning ${reviews.size} reviews")
            reviews
        } catch (e: Exception) {
            Log.e("FirestoreReviewRepository", "Error getting hotel reviews: ${e.message}", e)
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
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getUserReviews(userId: String): List<Review> {
        return try {
            val snapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Review::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.e("FirestoreReviewRepository", "Error parsing user review ${document.id}: ${e.message}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreReviewRepository", "Error getting user reviews: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun updateReview(review: Review): Review {
        return try {
            firestore.collection("reviews")
                .document(review.id)
                .set(review)
                .await()
            review
        } catch (_: Exception) {
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
        } catch (_: Exception) {
            false
        }
    }
}
