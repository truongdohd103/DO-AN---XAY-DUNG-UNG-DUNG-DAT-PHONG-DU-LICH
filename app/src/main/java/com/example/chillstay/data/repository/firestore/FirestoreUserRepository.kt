package com.example.chillstay.data.repository.firestore

import com.example.chillstay.domain.model.User
import com.example.chillstay.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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
            if (user.id.isBlank()) {
                val documentRef = firestore.collection("users").add(user).await()
                user.copy(id = documentRef.id)
            } else {
                firestore.collection("users")
                    .document(user.id)
                    .set(user)
                    .await()
                user
            }
        } catch (e: Exception) {
            throw e
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
            throw e
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

        return User(
            id = documentId,
            email = email,
            password = data["password"] as? String ?: "",
            fullName = data["fullName"] as? String ?: "",
            gender = data["gender"] as? String ?: "",
            photoUrl = data["photoUrl"] as? String ?: "",
            dateOfBirth = dob,
            role = data["role"] as? String ?: "user"
        )
    }

    companion object {
        private val DEFAULT_DOB: LocalDate = LocalDate.of(2000, 1, 1)
    }
}
