package com.example.chillstay.ui.hoteldetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.chillstay.R
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.max
import kotlin.math.min

@Composable
fun HotelImageSection(imageUrls: List<String>) {
    val modifier = Modifier
    val imageCount = imageUrls.size
    val displayCount = if (imageCount == 0) 1 else min(imageCount, 7)

    val virtualPageCount = max(1000, displayCount * 1000)
    val startPage = virtualPageCount / 2 - (virtualPageCount / 2) % displayCount

    val pagerState = rememberPagerState(
        initialPage = startPage,
        pageCount = { virtualPageCount }
    )

    // active dot state
    val currentIndex = remember { mutableIntStateOf(0) }

    // full screen viewer state
    val isViewerOpen = remember { mutableStateOf(false) }
    val viewerStartIndex = remember { mutableIntStateOf(0) }

    // reset pager when images change
    LaunchedEffect(imageCount) {
        if (imageCount > 0) {
            runCatching { pagerState.scrollToPage(startPage) }
            currentIndex.intValue = 0
        } else {
            currentIndex.intValue = 0
        }
    }

    // map currentPage -> index 0..displayCount-1
    LaunchedEffect(pagerState, startPage, displayCount) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val mapped = if (imageCount == 0) 0
                else {
                    if (page - startPage >= 0) (page - startPage) % displayCount
                    else (virtualPageCount + page - startPage) % displayCount
                }
                currentIndex.intValue = mapped
            }
    }

    Box(modifier = modifier.fillMaxWidth().height(250.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val idx = if (imageCount == 0) 0
            else {
                if (page - startPage >= 0) (page - startPage) % displayCount
                else (virtualPageCount + page - startPage) % displayCount
            }

            // make image clickable: mở full screen viewer, truyền index thực
            AsyncImage(
                model = imageUrls.getOrNull(idx) ?: "https://placehold.co/414x250",
                contentDescription = "Image $idx",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        // open viewer at the tapped index (map to actual index in full list)
                        viewerStartIndex.intValue = idx
                        isViewerOpen.value = true
                    },
                contentScale = ContentScale.Crop
            )
        }

        // dots indicator
        DotsIndicator(
            pageCount = displayCount,
            currentPage = currentIndex.intValue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )

        // image count badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_photo_camera),
                    contentDescription = "Photos",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(text = imageUrls.size.toString(), color = Color.White, fontSize = 14.sp)
            }
        }
    }

    // Full screen overlay viewer (translucent black background so user still sees page behind a bit)
    if (isViewerOpen.value) {
        FullScreenImageViewer(
            imageUrls = imageUrls,
            virtualPageCount = virtualPageCount,
            startIndex = currentIndex.intValue,
            onClose = { isViewerOpen.value = false }
        )
    }
}

@Composable
private fun FullScreenImageViewer(
    imageUrls : List<String>,
    virtualPageCount: Int,
    startIndex: Int,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(enabled = true) { onClose() }

        val pageCount = virtualPageCount.coerceAtLeast(1)
        val pagerState = rememberPagerState(
            initialPage = virtualPageCount / 2 + startIndex,
            pageCount = { pageCount })

        Box(
            modifier = Modifier
                .fillMaxSize()
                // semi-transparent black so underlying content is still slightly visible
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable { /* consume background clicks so pager doesn't receive them */ }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val idx = page % imageUrls.size
                AsyncImage(
                    model = imageUrls.getOrNull(idx) ?: "https://placehold.co/1080x1920",
                    contentDescription = "Full image $idx",
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // close button top-right
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 20.dp, end = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Close"
                )
            }
        }
    }
}

@Composable
private fun DotsIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(pageCount) { i ->
            val isActive = i == currentPage
            Box(
                modifier = Modifier
                    .size(if (isActive) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color.White else Color.White.copy(alpha = 0.5f))
            )
        }
    }
}
