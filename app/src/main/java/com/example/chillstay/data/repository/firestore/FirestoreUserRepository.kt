package com.example.chillstay.data.repository.firestore

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override suspend fun getUser(id: String): User? {
        return try {
            android.util.Log.d("FirestoreUserRepository", "Getting user: $id")
            
            val document = firestore.collection("users")
                .document(id)
                .get()
                .await()
            
            if (document.exists()) {
                val data = document.data
                android.util.Log.d("FirestoreUserRepository", "User document data: $data")
                
                // Map thủ công để đảm bảo đúng field names (đặc biệt là "e-mail" → email)
                val user = User(
                    id = document.id,
                    email = data?.get("e-mail") as? String ?: "",  // ✅ Map "e-mail" → email
                    password = data?.get("password") as? String ?: "",
                    fullName = data?.get("fullName") as? String ?: "",
                    gender = data?.get("gender") as? String ?: "",
                    photoUrl = data?.get("photoUrl") as? String ?: "",
                    dateOfBirth = (data?.get("dateOfBirth") as? String)?.let { dateStr ->
                        try {
                            java.time.LocalDate.parse(dateStr)
                        } catch (e: Exception) {
                            android.util.Log.w("FirestoreUserRepository", "Failed to parse dateOfBirth: $dateStr, using default")
                            java.time.LocalDate.of(2000, 1, 1)
                        }
                    } ?: java.time.LocalDate.of(2000, 1, 1)
                )
                
                android.util.Log.d("FirestoreUserRepository", "Parsed user: id=${user.id}, fullName=${user.fullName}, email=${user.email}")
                user
            } else {
                android.util.Log.w("FirestoreUserRepository", "User document not found: $id")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreUserRepository", "Error getting user $id: ${e.message}", e)
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
