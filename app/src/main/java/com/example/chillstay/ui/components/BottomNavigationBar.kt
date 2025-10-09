package com.example.chillstay.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.example.chillstay.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar(
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    NavigationBar(
        containerColor = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_home),
                    contentDescription = "Home",
                    tint = if (selectedTab == 0) Color(0xFF1AB6B6) else Color(0xFF757575)
                )
            },
            label = { 
                Text(
                    "Home", 
                    color = if (selectedTab == 0) Color(0xFF212121) else Color(0xFF757575), 
                    fontSize = 12.sp
                ) 
            }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Deal",
                    tint = if (selectedTab == 1) Color(0xFF1AB6B6) else Color(0xFF757575)
                )
            },
            label = { 
                Text(
                    "Deal", 
                    color = if (selectedTab == 1) Color(0xFF212121) else Color(0xFF757575), 
                    fontSize = 12.sp
                ) 
            }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_favorite),
                    contentDescription = "Saved",
                    tint = if (selectedTab == 2) Color(0xFF1AB6B6) else Color(0xFF757575)
                )
            },
            label = { 
                Text(
                    "Saved", 
                    color = if (selectedTab == 2) Color(0xFF212121) else Color(0xFF757575), 
                    fontSize = 12.sp
                ) 
            }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_flight),
                    contentDescription = "My trips",
                    tint = if (selectedTab == 3) Color(0xFF1AB6B6) else Color(0xFF757575)
                )
            },
            label = { 
                Text(
                    "My trips", 
                    color = if (selectedTab == 3) Color(0xFF212121) else Color(0xFF757575), 
                    fontSize = 12.sp
                ) 
            }
        )
        NavigationBarItem(
            selected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Profile",
                    tint = if (selectedTab == 4) Color(0xFF1AB6B6) else Color(0xFF757575)
                )
            },
            label = { 
                Text(
                    "Profile", 
                    color = if (selectedTab == 4) Color(0xFF212121) else Color(0xFF757575), 
                    fontSize = 12.sp
                ) 
            }
        )
    }
}
