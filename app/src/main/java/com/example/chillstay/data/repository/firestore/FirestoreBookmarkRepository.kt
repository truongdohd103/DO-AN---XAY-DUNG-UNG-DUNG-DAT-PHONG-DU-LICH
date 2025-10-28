package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Bookmark
import com.example.chillstay.domain.repository.BookmarkRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreBookmarkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : BookmarkRepository {

    override suspend fun addBookmark(bookmark: Bookmark): Bookmark {
        return try {
            Log.d("FirestoreBookmarkRepository", "Adding bookmark: userId=${bookmark.userId}, hotelId=${bookmark.hotelId}")
            val documentRef = firestore.collection("bookmarks").add(bookmark).await()
            Log.d("FirestoreBookmarkRepository", "Successfully added bookmark with ID: ${documentRef.id}")
            bookmark.copy(id = documentRef.id)
        } catch (e: Exception) {
            Log.e("FirestoreBookmarkRepository", "Error adding bookmark: ${e.message}")
            bookmark
        }
    }

    override suspend fun removeBookmark(userId: String, hotelId: String): Boolean {
        return try {
            Log.d("FirestoreBookmarkRepository", "Removing bookmark: userId=$userId, hotelId=$hotelId")
            val snapshot = firestore.collection("bookmarks")
                .whereEqualTo("userId", userId)
                .whereEqualTo("hotelId", hotelId)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                Log.d("FirestoreBookmarkRepository", "No bookmark found to remove")
                return false
            }
            
            val document = snapshot.documents.first()
            firestore.collection("bookmarks").document(document.id).delete().await()
            Log.d("FirestoreBookmarkRepository", "Successfully removed bookmark with ID: ${document.id}")
            true
        } catch (e: Exception) {
            Log.e("FirestoreBookmarkRepository", "Error removing bookmark: ${e.message}")
            false
        }
    }

    override suspend fun getUserBookmarks(userId: String): List<Bookmark> {
        return try {
            Log.d("FirestoreBookmarkRepository", "Getting bookmarks for user: $userId")
            
            // Add timeout to prevent hanging
            val snapshot = withTimeout(10000) { // 10 second timeout
                firestore.collection("bookmarks")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
            }
            
            Log.d("FirestoreBookmarkRepository", "Found ${snapshot.documents.size} bookmark documents")
            
            // Sort in memory instead of using orderBy to avoid index requirement
            val bookmarks = snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(Bookmark::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    Log.w("FirestoreBookmarkRepository", "Failed to parse bookmark document ${document.id}: ${e.message}")
                    null
                }
            }.sortedByDescending { it.createdAt }
            
            Log.d("FirestoreBookmarkRepository", "Returning ${bookmarks.size} bookmarks")
            bookmarks
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e("FirestoreBookmarkRepository", "Timeout getting bookmarks: ${e.message}")
            emptyList()
        } catch (e: Exception) {
            Log.e("FirestoreBookmarkRepository", "Error getting bookmarks: ${e.message}", e)
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
