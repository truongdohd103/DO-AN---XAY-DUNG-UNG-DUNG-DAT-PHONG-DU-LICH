package com.example.chillstay.data.repository.firestore

import com.example.chillstay.core.common.Result
import com.example.chillstay.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): String {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return authResult.user?.uid ?: throw IllegalStateException("Unable to resolve user id after sign in")
    }

    override suspend fun signUp(email: String, password: String): String {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return authResult.user?.uid ?: throw IllegalStateException("Unable to resolve user id after sign up")
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun observeCurrentUserId(): Flow<Result<String?>> = callbackFlow {
        // Emit giá trị ban đầu
        trySend(Result.success(firebaseAuth.currentUser?.uid))
        
        // Lắng nghe thay đổi auth state
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val userId = auth.currentUser?.uid
            trySend(Result.success(userId))
        }
        
        firebaseAuth.addAuthStateListener(listener)
        
        // Cleanup khi flow bị hủy
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }
}


