package com.example.chillstay.core.common

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OnboardingManager {
    private const val PREFS_NAME = "chillstay_prefs"
    private const val KEY_WELCOME_SEEN = "welcome_seen"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isFirstLaunch(context: Context): Boolean =
        !prefs(context).getBoolean(KEY_WELCOME_SEEN, false)

    // Suspend để async I/O, tránh StrictMode violation trên main thread
    suspend fun markWelcomeSeen(context: Context) {
        withContext(Dispatchers.IO) {
            prefs(context).edit().putBoolean(KEY_WELCOME_SEEN, true).apply()
        }
    }

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    // Suspend để async I/O, tránh StrictMode violation trên main thread
    suspend fun markOnboardingDone(context: Context) {
        withContext(Dispatchers.IO) {
            prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        }
    }
}