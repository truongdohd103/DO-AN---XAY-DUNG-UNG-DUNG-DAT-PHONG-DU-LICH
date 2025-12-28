package com.example.chillstay.data.repository.firestore

import android.util.Log
import com.example.chillstay.domain.model.CustomerStats
import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.model.UserRole
import com.example.chillstay.domain.repository.UserRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    override suspend fun getAllUsers(): List<User> {
        return try {
            val snapshot = firestore.collection("users")
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                mapUser(document.data, document.id)
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            val document = firestore.collection("users")
                .document(id)
                .get()
                .await()

            if (document.exists()) {
                mapUser(document.data, document.id)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun createUser(user: User): User {
        return try {
            val userMap = userToMap(user)
            if (user.id.isBlank()) {
                val documentRef = firestore.collection("users").add(userMap).await()
                user.copy(id = documentRef.id)
            } else {
                firestore.collection("users")
                    .document(user.id)
                    .set(userMap)
                    .await()
                user
            }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun updateUser(user: User): User {
        return try {
            val userMap = userToMap(user)
            firestore.collection("users")
                .document(user.id)
                .set(userMap)
                .await()
            user
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun deleteUser(id: String) {
        try {
            firestore.collection("users")
                .document(id)
                .delete()
                .await()
        } catch (_: Exception) {
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
                mapUser(document.data, document.id)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun mapUser(data: Map<String, Any>?, documentId: String): User? {
        if (data == null) return null
        val email = (data["email"] as? String)
            ?: (data["e-mail"] as? String)
            ?: return null

        val dob = (data["dateOfBirth"] as? String)?.let { dateString ->
            try {
                LocalDate.parse(dateString)
            } catch (_: Exception) {
                DEFAULT_DOB
            }
        } ?: DEFAULT_DOB

        // Map role from Firestore, default to USER if not found
        val roleString = data["role"] as? String ?: "USER"
        val role = try {
            UserRole.valueOf(roleString.uppercase())
        } catch (_: Exception) {
            UserRole.USER
        }

        return User(
            id = documentId,
            email = email,
            password = data["password"] as? String ?: "",
            fullName = data["fullName"] as? String ?: "",
            gender = data["gender"] as? String ?: "",
            photoUrl = data["photoUrl"] as? String ?: "",
            phoneNumber = data["phoneNumber"] as? String ?: "",
            dateOfBirth = dob,
            isActive = data["isActive"] as? Boolean ?: true,
            role = role,
            memberSince = data["memberSince"] as? Timestamp ?: Timestamp.now()
        )
    }

    override suspend fun getCustomerStats(userId: String): CustomerStats {
        return try {
            Log.d("FirestoreUserRepository", "Getting stats for user $userId")

            // Get user to check memberSince
            val user = getUserById(userId)
            val memberSince = user?.memberSince?.toDate()?.let { date ->
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
                formatter.format(date)
            } ?: "Unknown"

            // Get total bookings
            val bookingsSnapshot = firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val totalBookings = bookingsSnapshot.documents.size

            // Calculate total spent
            val totalSpent = bookingsSnapshot.documents.sumOf { doc ->
                doc.getDouble("totalPrice") ?: 0.0
            }

            // Get total reviews
            val reviewsSnapshot = firestore.collection("reviews")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            val totalReviews = reviewsSnapshot.documents.size

            CustomerStats(
                totalBookings = totalBookings,
                totalSpent = totalSpent,
                totalReviews = totalReviews,
                memberSince = memberSince
            )
        } catch (e: Exception) {
            Log.e("FirestoreUserRepository", "Error getting customer stats: ${e.message}")
            CustomerStats()
        }
    }

    private fun userToMap(user: User): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        map["email"] = user.email
        map["password"] = user.password
        map["fullName"] = user.fullName
        map["gender"] = user.gender
        map["photoUrl"] = user.photoUrl
        map["dateOfBirth"] = user.dateOfBirth.toString()
        map["role"] = user.role.name
        map["phoneNumber"] = user.phoneNumber
        map["isActive"] = user.isActive
        user.memberSince?.let { map["memberSince"] = it }
        return map
    }

    companion object {
        private val DEFAULT_DOB: LocalDate = LocalDate.of(2000, 1, 1)
    }
}
