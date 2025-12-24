package com.example.chillstay.ui.admin.accommodation.room_manage

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.R
import com.example.chillstay.domain.model.Room
import com.example.chillstay.domain.model.RoomStatus
import com.example.chillstay.ui.admin.accommodation.accommodation_manage.AccommodationManageIntent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomManageScreen(
    hotelId: String,
    onBackClick: () -> Unit = {},
    onCreateRoomClick: () -> Unit = {},
    onDeleteRoomClick: (Room) -> Unit = {},
    onEditRoomClick: (Room) -> Unit = {},
    viewModel: RoomManageViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(hotelId) {
        viewModel.onEvent(RoomManageIntent.LoadRooms(hotelId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RoomManageEffect.NavigateBack -> onBackClick()
                RoomManageEffect.NavigateToCreateRoom -> onCreateRoomClick()
                is RoomManageEffect.NavigateToEditRoom -> onEditRoomClick(effect.room)
                is RoomManageEffect.ShowDisableSuccess -> { /* Handle disable success */ }
                is RoomManageEffect.ShowDeleteSuccess -> onDeleteRoomClick(effect.room)
                is RoomManageEffect.ShowError -> {
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Room",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(RoomManageIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6)
                )
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = uiState.error ?: "Error",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(
                                onClick = { hotelId.let { viewModel.onEvent(RoomManageIntent.LoadRooms(it)) } }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // Statistics Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                number = uiState.rooms.size.toString(),
                                label = "Total Room",
                                gradient = listOf(Color(0xFF3B82F6), Color(0xFF2563EB)),
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                number = uiState.rooms.filter { it.status == RoomStatus.ACTIVE }.size.toString(),
                                label = "Active Room",
                                gradient = listOf(Color(0xFF92F63B), Color(0xFF4CAF50)),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Room List Title
                        Text(
                            text = "Room List",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                        )

                        // Room Cards
                        uiState.rooms.forEach { room ->
                            RoomCard(
                                room = room,
                                onEdit = { viewModel.onEvent(RoomManageIntent.EditRoom(room)) },
                                onDisable = { viewModel.onEvent(RoomManageIntent.DisableRoom(room)) },
                                onDelete = { viewModel.onEvent(RoomManageIntent.DeleteRoom(room)) }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.onEvent(RoomManageIntent.CreateNewRoom) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF1AB6B6)
        ) {
            Text(
                text = "+",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(90.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = gradient,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = number,
                style = TextStyle(
                    fontSize = 27.3.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 12.7.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f)
                )
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun RoomCard(
    room: Room,
    onEdit: () -> Unit,
    onDisable: () -> Unit,
    onDelete: () -> Unit
) {
    val roomSize = room.area
    val roomSizeText = if (roomSize > 0) {
        "$roomSize m²/${(roomSize * 10.764).toInt()} ft²"
    } else {
        "N/A"
    }
    val maxAdults = room.capacity
    val bedsText = if (maxAdults > 0) {
        "$maxAdults ${if (maxAdults > 1) "beds" else "bed"}"
    } else {
        "N/A"
    }

    val galleryCount = room.gallery?.totalCount ?: 0
    val price = room.price
    val originalPrice = price * (1.0 + room.discount / 100)
    val discountPercent = 25

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Room Name
            Text(
                text = room.name.ifEmpty { "Room" },
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
            )

            // Room Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (room.gallery?.thisRoom[0]?.isEmpty() == false) {
                    AsyncImage(
                        model = room.gallery.thisRoom[0],
                        contentDescription = room.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                    )
                }

//                // "Our last 4!" Badge - only show if availableCount <= 4
//                if (room.availableCount <= 4 && room.availableCount > 0) {
//                    Box(
//                        modifier = Modifier
//                            .padding(12.dp)
//                            .background(
//                                color = Color(0xFFFF5722),
//                                shape = RoundedCornerShape(4.dp)
//                            )
//                            .padding(horizontal = 8.dp, vertical = 4.dp)
//                            .align(Alignment.TopStart)
//                    ) {
//                        Text(
//                            text = "Our last ${room.availableCount}!",
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = Color.White
//                            )
//                        )
//                    }
//                }

                // Photo Count Badge
                if (galleryCount > 0) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .align(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_photo_camera),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = galleryCount.toString(),
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        )
                    }
                }

                // Heart Icon (favorite indicator)
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .size(38.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFE0E0E0),
                            shape = CircleShape
                        )
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "♡",
                        style = TextStyle(
                            fontSize = 18.sp,
                            color = Color(0xFFFF5722)
                        )
                    )
                }
            }

            // Room Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = roomSizeText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                )
                Text(
                    text = "Max $maxAdults adults",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                )
                Text(
                    text = bedsText,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                )
            }

            // Amenities - using facilities from room
            if (room.feature.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    room.feature.chunked(2).forEach { chunk ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            chunk.forEach { facility ->
                                Text(
                                    text = facility,
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color(0xFF757575)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Price Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "Breakfast ($25 / person)",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1AB65C)
                            )
                        )
                        Text(
                            text = "$${String.format("%.0f", originalPrice)}",
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Non-refundable (Low price!)",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = Color(0xFF212121)
                            )
                        )
                        Text(
                            text = "-$discountPercent%",
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Pay at hotel",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1AB65C)
                            )
                        )
                        Text(
                            text = "$${String.format("%.0f", price)}",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722)
                            )
                        )
                    }
                    Text(
                        text = "See details",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1AB6B6)
                        )
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Edit",
                    backgroundColor = Color(0xFF45A3F0),
                    modifier = Modifier.weight(1f),
                    onClick = onEdit
                )

                ActionButton(
                    text = "Disable",
                    backgroundColor = Color(0xFFF59E0A),
                    modifier = Modifier.weight(1f),
                    onClick = onDisable
                )

                ActionButton(
                    text = "Delete",
                    backgroundColor = Color(0xFFF04545),
                    modifier = Modifier.weight(1f),
                    onClick = onDelete
                )
            }

        }
    }
}

@Composable
fun ActionButton(
    text: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

