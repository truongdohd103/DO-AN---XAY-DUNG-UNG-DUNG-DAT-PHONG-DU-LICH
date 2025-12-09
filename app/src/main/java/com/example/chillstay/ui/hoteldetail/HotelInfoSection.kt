package com.example.chillstay.ui.hoteldetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.chillstay.R
import com.example.chillstay.ui.components.MarqueeText

private const val TAG = "HotelInfoSection"

@SuppressLint("DefaultLocale", "QueryPermissionsNeeded")
@Composable
fun HotelInfoSection(
    name: String,
    address: String,
    rating: Double
) {
    val context = LocalContext.current

    // default action: mở bản đồ ngoài bằng address (geo URI)
    val defaultOnMapClick = remember(address, context) {
        {
            openMapByAddress(context, address)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 21.dp)
    ) {
        Text(
            text = name,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF000000)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_map),
                contentDescription = "Open map",
                modifier = Modifier
                    .size(32.dp)
                    .clickable(onClick = defaultOnMapClick),
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.width(8.dp))
            MarqueeText(text = address, modifier = Modifier.fillMaxWidth(), 16f, "#757575")
        }

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val fullStars = rating.toInt().coerceIn(0, 5)
            val emptyStars = (5 - fullStars)
            repeat(fullStars) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating Star",
                    tint = Color(0xFFFBC40D),
                    modifier = Modifier.size(12.dp)
                )
            }
            repeat(emptyStars) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Rating Star Outline",
                    tint = Color(0xFFC0C0C0),
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = String.format("%.1f", rating),
                color = Color(0xFF1AB6B6),
                fontSize = 13.67.sp
            )
        }
    }
}

@SuppressLint("QueryPermissionsNeeded")
private fun openMapByAddress(context: Context, address: String) {
    try {
        val encoded = Uri.encode(address)
        val geoUri = "geo:0,0?q=$encoded".toUri()

        // ưu tiên Google Maps app
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            `package` = "com.google.android.apps.maps"
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
            return
        }

        // fallback any map app
        val anyIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (anyIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(anyIntent)
            return
        }

        // fallback -> browser google maps
        val webUri = "https://www.google.com/maps/search/?api=1&query=$encoded".toUri()
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Log.e(TAG, "Cannot open maps: $e")
    }
}

