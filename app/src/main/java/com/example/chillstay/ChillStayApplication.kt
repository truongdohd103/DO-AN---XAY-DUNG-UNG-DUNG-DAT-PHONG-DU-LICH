package com.example.chillstay

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.chillstay.di.repositoryModule
import com.example.chillstay.di.useCaseModule
import com.example.chillstay.di.viewModelModule
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
        // App Check Debug provider for development - DISABLED FOR TESTING
        // TODO: Re-enable App Check in production
        /*
        try {
            val appCheck = FirebaseAppCheck.getInstance()
            appCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
        } catch (_: Exception) { }
        */
        
        // Initialize Koin
        startKoin {
            androidContext(this@ChillStayApplication)
            modules(repositoryModule, useCaseModule, viewModelModule)
        }
    }
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .build()
    }
}


