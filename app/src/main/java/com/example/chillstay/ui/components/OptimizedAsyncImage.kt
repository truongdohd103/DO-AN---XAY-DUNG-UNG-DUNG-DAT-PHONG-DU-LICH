package com.example.chillstay.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.chillstay.R

/**
 * Optimized AsyncImage component with better error handling and fallbacks
 */
@Composable
fun OptimizedAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Int = R.drawable.ic_home,
    error: Int = R.drawable.ic_home,
    showLoadingIndicator: Boolean = true
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoaderConfig.create(context) }
    
    // Use original URL, let AsyncImage handle fallbacks
    val finalImageUrl = imageUrl ?: ""
    
    AsyncImage(
        model = finalImageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        imageLoader = imageLoader,
        placeholder = painterResource(id = placeholder),
        error = painterResource(id = error)
    )
}
