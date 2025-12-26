package com.example.chillstay.ui.admin.voucher.voucher_manage

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.chillstay.domain.model.Voucher
import com.example.chillstay.domain.model.VoucherStatus
import com.example.chillstay.domain.model.timeLeftText
import com.example.chillstay.domain.model.infoText
import com.example.chillstay.domain.model.discountText
import com.example.chillstay.domain.model.gradientColors
import com.example.chillstay.domain.model.decorativeEmojis
import org.koin.androidx.compose.koinViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoucherManageScreen(
    onBackClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    viewModel: VoucherManageViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is VoucherManageEffect.NavigateToEdit -> onEditClick(effect.voucherId)
                VoucherManageEffect.NavigateToCreate -> onCreateClick()
                is VoucherManageEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is VoucherManageEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                VoucherManageEffect.NavigateBack -> onBackClick()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Voucher",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(VoucherManageIntent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1AB6B6),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.White,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(VoucherManageIntent.CreateNewVoucher) },
                containerColor = Color(0xFF1AB6B6),
                contentColor = Color.White,
                modifier = Modifier.shadow(6.dp, CircleShape)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create New Code")
            }
        }
    ) { innerPadding ->
        // FIX 1: Chỉ dùng innerPadding 1 lần
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = {
                        viewModel.onEvent(VoucherManageIntent.UpdateSearchQuery(it))
                    }
                )

                // Statistics Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.activeCount,
                        label = "Active Codes",
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFA855F7),
                                Color(0xFF9333EA)
                            )
                        )
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        count = uiState.inactiveCount,
                        label = "Inactive",
                        gradient = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFF97316),
                                Color(0xFFEA580C)
                            )
                        )
                    )
                }

                // Section Title
                Text(
                    text = "All Discount Codes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(top = 4.dp)
                )

                // FIX 2: Hiển thị paginatedVouchers thay vì filteredVouchers
                uiState.paginatedVouchers.forEach { voucher ->
                    VoucherCard(
                        voucher = voucher,
                        onEditClick = {
                            viewModel.onEvent(VoucherManageIntent.EditVoucher(voucher.id))
                        },
                        onInvalidClick = {
                            viewModel.onEvent(VoucherManageIntent.ToggleVoucherStatus(voucher.id))
                        },
                        onDeleteClick = { showDeleteDialog = voucher.id }
                    )
                }

                // Empty State
                if (uiState.filteredVouchers.isEmpty() && !uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank())
                                "No vouchers found"
                            else
                                "No matching vouchers",
                            fontSize = 16.sp,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }

                // FIX 2: Thêm Pagination Controls
                if (uiState.filteredVouchers.isNotEmpty()) {
                    PaginationControls(
                        currentPage = uiState.currentPage,
                        totalPages = uiState.totalPages,
                        onPageChange = { viewModel.onEvent(VoucherManageIntent.GoToPage(it)) },
                        onPreviousPage = { viewModel.onEvent(VoucherManageIntent.PreviousPage) },
                        onNextPage = { viewModel.onEvent(VoucherManageIntent.NextPage) },
                        tealColor = Color(0xFF1AB6B6)
                    )
                }
            }

            // Loading Overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF1AB6B6))
                }
            }
        }
    }

    // Delete Confirmation Dialog
    showDeleteDialog?.let { voucherId ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Voucher") },
            text = { Text("Are you sure you want to delete this voucher? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(VoucherManageIntent.DeleteVoucher(voucherId))
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFEF4444)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF828282),
                modifier = Modifier.size(16.dp)
            )
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (query.isEmpty()) Color(0xFF757575) else Color.Black
                ),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun StatCard(
    count: Int,
    label: String,
    gradient: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .background(gradient, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun VoucherCard(
    voucher: Voucher,
    onEditClick: () -> Unit,
    onInvalidClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isActive = voucher.status == VoucherStatus.ACTIVE

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // FIX 3: Header với hình ảnh hoặc gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Nếu có imageUrl thì hiển thị ảnh, không thì dùng gradient
                if (voucher.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = voucher.imageUrl,
                        contentDescription = voucher.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Gradient background như cũ
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = voucher.gradientColors().map { parseColor(it) }
                                )
                            )
                    )

                    // Decorative emojis
                    Box(modifier = Modifier.fillMaxSize()) {
                        voucher.decorativeEmojis().forEachIndexed { index, emoji ->
                            Text(
                                text = emoji,
                                fontSize = when (index % 3) {
                                    0 -> 10.sp
                                    1 -> 14.sp
                                    else -> 20.sp
                                },
                                color = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(
                                        when (index % 4) {
                                            0 -> Alignment.TopStart
                                            1 -> Alignment.TopEnd
                                            2 -> Alignment.BottomStart
                                            else -> Alignment.BottomEnd
                                        }
                                    )
                                    .padding(
                                        when (index % 4) {
                                            0 -> PaddingValues(start = 20.dp, top = 20.dp)
                                            1 -> PaddingValues(end = 30.dp, top = 30.dp)
                                            2 -> PaddingValues(start = 30.dp, bottom = 20.dp)
                                            else -> PaddingValues(end = 20.dp, bottom = 20.dp)
                                        }
                                    )
                            )
                        }
                    }
                }

                // Discount badge (luôn hiển thị)
                Box(
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.95f),
                            RoundedCornerShape(12.dp)
                        )
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = voucher.discountText(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            textAlign = TextAlign.Center,
                            lineHeight = 32.sp
                        )
                        Text(
                            text = "OFF",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = voucher.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    lineHeight = 20.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = voucher.infoText(),
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = voucher.timeLeftText(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1AB6B6)
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionButton(
                    text = "Edit",
                    backgroundColor = Color(0xFF44A2EF),
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = if (isActive) "Invalid" else "Activate",
                    backgroundColor = if (isActive) Color(0xFFF59E0B) else Color(0xFF10B981),
                    onClick = onInvalidClick,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = "Delete",
                    backgroundColor = Color(0xFFEF4444),
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

// FIX 2: Thêm Pagination Controls giống AccommodationManageScreen
@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    tealColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onPreviousPage,
            enabled = currentPage > 1,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Previous")
        }

        Text(
            text = "Page $currentPage of $totalPages",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Button(
            onClick = onNextPage,
            enabled = currentPage < totalPages,
            colors = ButtonDefaults.buttonColors(
                containerColor = tealColor
            )
        ) {
            Text("Next")
        }
    }
}

// Helper function to parse color string
@SuppressLint("UseKtx")
private fun parseColor(colorString: String): Color {
    return try {
        Color(colorString.toColorInt())
    } catch (e: Exception) {
        Color.Gray
    }
}