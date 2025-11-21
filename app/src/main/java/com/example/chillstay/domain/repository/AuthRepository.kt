package com.example.chillstay.domain.repository

/**
 * Authentication gateway that exposes FirebaseAuth operations to the domain layer
 * without leaking any Android/Firebase specific APIs.
 */
interface AuthRepository {
    /**
     * Signs a user in with the provided email/password combination and returns the Firebase UID.
     */
    suspend fun signIn(email: String, password: String): String

    /**
     * Creates a new FirebaseAuth user and returns the generated UID.
     */
    suspend fun signUp(email: String, password: String): String

    /**
     * Signs the current FirebaseAuth user out.
     */
    suspend fun signOut()

    /**
     * Returns the UID of the currently signed-in user, or null if no user is authenticated.
     */
    fun getCurrentUserId(): String?
}


