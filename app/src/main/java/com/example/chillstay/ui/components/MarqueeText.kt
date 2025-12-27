package com.example.chillstay.ui.components

import androidx.annotation.FontRes
import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 16.sp,
    textColor: Color = Color(0xFF757575),
    fontWeight: FontWeight = FontWeight.Normal,
    @FontRes fontResId: Int? = null
) {
    val fontFamily = fontResId?.let { FontFamily(Font(it)) }

    Text(
        text = text,
        modifier = modifier.basicMarquee(iterations = Int.MAX_VALUE),
        maxLines = 1,
        overflow = TextOverflow.Visible,
        style = TextStyle(
            color = textColor,
            fontSize = textSize,
            fontWeight = fontWeight,
            fontFamily = fontFamily
        ),
        softWrap = false
    )
}
