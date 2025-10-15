package com.example.chillstay.data.repository

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Create user document in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    password = password,
                    fullName = "",
                    gender = "",
                    photoUrl = "",
                    dateOfBirth = LocalDate.now()
                )
                
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                
                Result.Success(user)
            } else {
                Result.Error(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Get user data from Firestore
                val document = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val user = document.toObject(User::class.java)?.copy(id = document.id)
                    if (user != null) {
                        Result.Success(user)
                    } else {
                        Result.Error(Exception("Failed to parse user data"))
                    }
                } else {
                    Result.Error(Exception("User profile not found"))
                }
            } else {
                Result.Error(Exception("Failed to sign in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun signOut(): Result<Boolean> {
        return try {
            auth.signOut()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun getCurrentUser(): Result<User?> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val document = firestore.collection("users")
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                if (document.exists()) {
                    val user = document.toObject(User::class.java)?.copy(id = document.id)
                    Result.Success(user)
                } else {
                    Result.Success(null)
                }
            } else {
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun updatePassword(newPassword: String): Result<Boolean> {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                firebaseUser.updatePassword(newPassword).await()
                Result.Success(true)
            } else {
                Result.Error(Exception("No user signed in"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String): Result<Boolean> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
