package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.Notification
import com.example.chillstay.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Filter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val collection = firestore.collection("notifications")

    override suspend fun getUserNotifications(
        userId: String,
        isRead: Boolean?,
        limit: Int?
    ): List<Notification> {
        return try {
            // Match userId equal to the specific user OR equal to "ALL"
            var query: Query = collection.where(
                Filter.or(
                    Filter.equalTo("userId", userId),
                    Filter.equalTo("userId", "ALL")
                )
            )
            
            if (isRead != null) {
                query = query.whereEqualTo("isRead", isRead)
            }
            
            query = query.orderBy("createdAt", Query.Direction.DESCENDING)
            
            if (limit != null) {
                query = query.limit(limit.toLong())
            }

            query.get().await().toObjects(Notification::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error getting notifications", e)
            emptyList()
        }
    }

    override suspend fun createNotification(notification: Notification): Notification {
        return try {
            val docRef = collection.document()
            val newNotification = notification.copy(id = docRef.id)
            docRef.set(newNotification).await()
            newNotification
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error creating notification", e)
            throw e
        }
    }

    override suspend fun markAsRead(notificationId: String): Boolean {
        return try {
            collection.document(notificationId).update("isRead", true).await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error marking notification as read", e)
            false
        }
    }

    override suspend fun markAllAsRead(userId: String): Boolean {
        return try {
            val batch = firestore.batch()
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            
            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error marking all as read", e)
            false
        }
    }

    override suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            collection.document(notificationId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error deleting notification", e)
            false
        }
    }

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            val snapshot = collection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .count()
                .get(com.google.firebase.firestore.AggregateSource.SERVER)
                .await()
            
            snapshot.count.toInt()
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error getting unread count", e)
            0
        }
    }

    override suspend fun getAllNotifications(): List<Notification> {
        return try {
            collection.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Notification::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreNotification", "Error getting all notifications", e)
            emptyList()
        }
    }
}
