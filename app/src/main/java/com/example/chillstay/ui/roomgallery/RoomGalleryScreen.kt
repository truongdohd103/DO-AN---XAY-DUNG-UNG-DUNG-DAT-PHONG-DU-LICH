package com.example.chillstay.ui.roomgallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject
import com.example.chillstay.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomGalleryScreen(
    hotelId: String = "",
    roomId: String = "",
    onBackClick: () -> Unit = {}
) {
    val viewModel: RoomGalleryViewModel = koinInject()
    val uiState = viewModel.state.collectAsStateWithLifecycle().value
    androidx.compose.runtime.LaunchedEffect(hotelId, roomId) {
        if (hotelId.isNotEmpty() && roomId.isNotEmpty()) {
            viewModel.onEvent(RoomGalleryIntent.LoadGallery(hotelId, roomId))
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Gallery (${uiState.totalCount})",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CategoryItem(
                    title = "Exterior\nview",
                    imageUrl = uiState.exteriorView.firstOrNull() ?: "",
                    selected = uiState.selectedCategory == RoomGalleryCategory.ExteriorView,
                    onClick = { viewModel.onEvent(RoomGalleryIntent.SelectCategory(RoomGalleryCategory.ExteriorView)) }
                )
                CategoryItem(
                    title = "Facilities",
                    imageUrl = uiState.facilities.firstOrNull() ?: "",
                    selected = uiState.selectedCategory == RoomGalleryCategory.Facilities,
                    onClick = { viewModel.onEvent(RoomGalleryIntent.SelectCategory(RoomGalleryCategory.Facilities)) }
                )
                CategoryItem(
                    title = "Dining",
                    imageUrl = uiState.dining.firstOrNull() ?: "",
                    selected = uiState.selectedCategory == RoomGalleryCategory.Dining,
                    onClick = { viewModel.onEvent(RoomGalleryIntent.SelectCategory(RoomGalleryCategory.Dining)) }
                )
                CategoryItem(
                    title = "This room",
                    imageUrl = uiState.thisRoom.firstOrNull() ?: "",
                    selected = uiState.selectedCategory == RoomGalleryCategory.ThisRoom,
                    onClick = { viewModel.onEvent(RoomGalleryIntent.SelectCategory(RoomGalleryCategory.ThisRoom)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = when (uiState.selectedCategory) {
                    RoomGalleryCategory.ExteriorView -> "Exterior view"
                    RoomGalleryCategory.Facilities -> "Facilities"
                    RoomGalleryCategory.Dining -> "Dining"
                    RoomGalleryCategory.ThisRoom -> "This room"
                },
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 26.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .height(280.dp)
                    .fillMaxWidth()
            ) {
                AsyncImage(
                    model = (uiState.currentImages.firstOrNull() ?: ""),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = (uiState.currentImages.getOrNull(1) ?: ""),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(11.dp)
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Newly added photo",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .height(200.dp)
                ) {
                    AsyncImage(
                        model = (uiState.currentImages.getOrNull(2) ?: ""),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CategoryItem(
    title: String,
    imageUrl: String,
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .then(
                    if (selected) Modifier.border(1.dp, Color(0xFF1AB6B6), RoundedCornerShape(10.dp)) else Modifier
                )
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = title,
            color = if (selected) Color(0xFF1AB6B6) else Color(0xFF212121),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
