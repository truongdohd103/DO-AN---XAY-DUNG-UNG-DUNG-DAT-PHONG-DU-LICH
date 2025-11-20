package com.example.chillstay.ui.components

import android.text.TextUtils
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.toColorInt
import androidx.core.content.res.ResourcesCompat


@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    textSizeSp: Float = 16f,
    textColorHex: String = "#757575",
    @FontRes fontResId: Int? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                // marquee config
                setSingleLine(true)
                ellipsize = TextUtils.TruncateAt.MARQUEE
                isSelected = true
                marqueeRepeatLimit = -1
                setHorizontallyScrolling(true)

                // size / color / font
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                try { setTextColor(textColorHex.toColorInt()) } catch (_: Exception) {}
                fontResId?.let { res ->
                    ResourcesCompat.getFont(ctx, res)?.let { typeface = it }
                }
            }
        },
        update = { tv ->
            tv.text = text
            tv.isSelected = true
        }
    )
}
