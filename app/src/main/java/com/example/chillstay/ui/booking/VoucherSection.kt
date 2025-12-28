package com.example.chillstay.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chillstay.domain.model.Voucher

import androidx.compose.ui.text.font.FontStyle
import com.example.chillstay.domain.model.VoucherType

@Composable
fun VoucherSection(
    voucherCode: String,
    onVoucherCodeChange: (String) -> Unit,
    onApplyVoucher: (String) -> Unit,
    isApplying: Boolean,
    voucherMessage: String?,
    appliedVouchers: List<Voucher>,
    availableVouchers: List<Voucher>,
    onRemoveVoucher: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Promo Code",
                color = Color(0xFF212121),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (appliedVouchers.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = voucherCode,
                        onValueChange = onVoucherCodeChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter voucher code") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1AB6B6),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = { onApplyVoucher(voucherCode) },
                        enabled = voucherCode.isNotBlank() && !isApplying,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1AB6B6)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        if (isApplying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Apply")
                        }
                    }
                }
            } else {
                // Show applied voucher
                appliedVouchers.forEach { voucher ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE0F7FA), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = voucher.code,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006064)
                            )
                            Text(
                                text = "Applied - ${if (voucher.type == VoucherType.PERCENTAGE) "${voucher.value.toInt()}% OFF" else "$${voucher.value} OFF"}",
                                fontSize = 12.sp,
                                color = Color(0xFF006064)
                            )
                        }
                        IconButton(onClick = { onRemoveVoucher(voucher.id) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = Color(0xFF006064)
                            )
                        }
                    }
                }
            }

            if (voucherMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voucherMessage,
                    color = if (voucherMessage.startsWith("Voucher applied")) Color(0xFF2E7D32) else Color.Red,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Available Vouchers",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (availableVouchers.isEmpty()) {
                Text(
                    text = "No vouchers available.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = FontStyle.Italic
                )
            } else {
                availableVouchers.forEach { voucher ->
                    val isApplied = appliedVouchers.any { it.id == voucher.id }
                    VoucherItem(
                        voucher = voucher,
                        isApplied = isApplied,
                        onApply = {
                            if (!isApplied) {
                                onVoucherCodeChange(voucher.code)
                                onApplyVoucher(voucher.code)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun VoucherItem(
    voucher: Voucher,
    isApplied: Boolean,
    onApply: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = voucher.code,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF212121)
            )
            Text(
                text = voucher.title.ifBlank { voucher.description },
                fontSize = 12.sp,
                color = Color(0xFF757575),
                maxLines = 1
            )
            Text(
                text = if (voucher.type == VoucherType.PERCENTAGE) "${voucher.value.toInt()}% OFF" else "$${voucher.value} OFF",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1AB6B6)
            )
        }
        
        Button(
            onClick = onApply,
            enabled = !isApplied,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isApplied) Color(0xFFE0E0E0) else Color(0xFF1AB6B6),
                contentColor = if (isApplied) Color(0xFF757575) else Color.White
            ),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = if (isApplied) "Applied" else "Apply",
                fontSize = 12.sp
            )
        }
    }
}
