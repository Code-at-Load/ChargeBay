package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// Apple StandBy Bubbly Typography
val TitanOneFontFamily = FontFamily(
    Font(R.font.titan_one, FontWeight.Normal)
)

val FredokaFontFamily = FontFamily(
    Font(R.font.fredoka, FontWeight.Normal)
)

val RubikBubblesFontFamily = FontFamily(
    Font(R.font.rubik_bubbles, FontWeight.Normal)
)

val PlayfairDisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal)
)

val DmSerifDisplayFontFamily = FontFamily(
    Font(R.font.dm_serif_display, FontWeight.Normal)
)

// Primary Bubbly Clock Font (matches user image 1 top right and image 2)
val StandByBubblyFont = TitanOneFontFamily

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
