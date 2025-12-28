package com.example.chillstay.core.common

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OnboardingManager {
    private const val PREFS_NAME = "chillstay_prefs"
    private const val KEY_WELCOME_SEEN = "welcome_seen"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_LAST_ROUTE = "last_route"
    private const val KEY_LAST_TAB = "last_tab"
    private const val KEY_IS_ADMIN = "is_admin"

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

    fun isAdmin(context: Context): Boolean =
        prefs(context).getBoolean(KEY_IS_ADMIN, false)

    suspend fun setAdmin(context: Context, isAdmin: Boolean) {
        withContext(Dispatchers.IO) {
            prefs(context).edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply()
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

    fun getLastRoute(context: Context): String? =
        prefs(context).getString(KEY_LAST_ROUTE, null)

    suspend fun setLastRoute(context: Context, route: String) {
        withContext(Dispatchers.IO) {
            prefs(context).edit().putString(KEY_LAST_ROUTE, route).apply()
        }
    }

    fun getLastTab(context: Context): Int =
        prefs(context).getInt(KEY_LAST_TAB, 0)

    suspend fun setLastTab(context: Context, tab: Int) {
        withContext(Dispatchers.IO) {
            prefs(context).edit().putInt(KEY_LAST_TAB, tab).apply()
        }
    }
}
