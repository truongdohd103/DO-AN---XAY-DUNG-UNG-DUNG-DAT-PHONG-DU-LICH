package com.example.chillstay

import android.app.Application
import com.example.chillstay.di.repositoryModule
import com.example.chillstay.di.useCaseModule
import com.example.chillstay.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChillStayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ChillStayApplication)
            modules(repositoryModule, useCaseModule, viewModelModule)
        }
    }
}


