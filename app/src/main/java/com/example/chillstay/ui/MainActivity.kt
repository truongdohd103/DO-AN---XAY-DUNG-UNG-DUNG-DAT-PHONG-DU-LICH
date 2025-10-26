package com.example.chillstay.ui

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.rememberNavController
import com.example.chillstay.ui.home.HomeViewModel
import com.example.chillstay.ui.navigation.AppNavHost
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.util.Log

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModel()
    
    // Background scope for Firebase initialization
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable StrictMode to detect main thread violations (only in debug builds)
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
        }
        
        // Initialize Firebase asynchronously to avoid blocking main thread
        initializeFirebaseAsync()
        
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val navController = rememberNavController()
                AppNavHost(navController = navController, homeViewModel = homeViewModel)
            }
        }
    }
    
    /**
     * Initialize Firebase asynchronously to prevent main thread blocking
     * This includes FirebaseApp initialization and FirebaseAuth setup
     */
    private fun initializeFirebaseAsync() {
        backgroundScope.launch {
            try {
                Log.d("MainActivity", "Initializing Firebase asynchronously...")
                
                // Ensure FirebaseApp is initialized
                if (FirebaseApp.getApps(this@MainActivity).isEmpty()) {
                    FirebaseApp.initializeApp(this@MainActivity)
                }
                
                // Initialize FirebaseAuth
                FirebaseAuth.getInstance()
                
                Log.d("MainActivity", "Firebase initialized successfully")
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing Firebase: ${e.message}", e)
                // Handle SecurityException (wrong SHA fingerprint) gracefully
                if (e is SecurityException) {
                    Log.e("MainActivity", "SecurityException - Check SHA fingerprint in Firebase Console")
                }
            }
        }
    }
}

class MainViewModel : ViewModel()

@Composable
fun MainScreen() {}


