package com.example.chillstay

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.chillstay.di.chatModule
import com.example.chillstay.di.repositoryModule
import com.example.chillstay.di.useCaseModule
import com.example.chillstay.di.viewModelModule
import com.example.chillstay.ui.components.ImageLoaderConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChillStayApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Firebase App Check for debug builds only
        if (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            try {
                val appCheck = FirebaseAppCheck.getInstance()
                appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            } catch (_: Exception) {
                // Silently ignore App Check setup errors in debug
            }
        }
        
        // Initialize Koin
        startKoin {
            androidContext(this@ChillStayApplication)
            modules(repositoryModule, useCaseModule, viewModelModule, chatModule)
        }
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoaderConfig.create(this)
    }
}


