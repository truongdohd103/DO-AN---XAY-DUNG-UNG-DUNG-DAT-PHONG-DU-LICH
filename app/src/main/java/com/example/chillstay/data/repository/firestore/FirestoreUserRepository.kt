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
