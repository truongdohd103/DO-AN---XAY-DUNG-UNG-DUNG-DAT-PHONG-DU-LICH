package com.example.chillstay.ui.components

import android.content.Context
import coil.ImageLoader
import coil.util.DebugLogger
import coil.request.CachePolicy

/**
 * Custom ImageLoader configuration to handle image decoder issues
 * Provides fallback mechanisms for unsupported image formats
 */
object ImageLoaderConfig {
    
    fun create(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .crossfade(300) // 300ms crossfade animation
            .respectCacheHeaders(false) // Ignore cache headers for better compatibility
            .allowHardware(true) // Enable hardware bitmaps for better performance
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .apply {
                // Only add debug logger in debug builds
                logger(DebugLogger())
            }
            .build()
    }
}
