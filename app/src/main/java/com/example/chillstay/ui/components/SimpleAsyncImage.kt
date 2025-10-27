package com.example.chillstay.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.chillstay.R

/**
 * Simple AsyncImage component similar to BookingApp (Helia)
 * Uses global ImageLoader from Application
 */
@Composable
fun SimpleAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Int = R.drawable.ic_home,
    error: Int = R.drawable.ic_home
) {
    // Always use AsyncImage with proper error handling
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = painterResource(id = placeholder),
        error = painterResource(id = error)
    )
}
