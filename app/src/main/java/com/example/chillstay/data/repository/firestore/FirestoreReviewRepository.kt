package com.example.chillstay.data.repository.firestore

import com.example.chillstay.domain.model.Review
import com.example.chillstay.domain.repository.ReviewRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
