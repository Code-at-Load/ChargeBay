package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleNightRed

/**
 * Apple StandBy Condensed Clock matching image_e4692b65.png.
 * Features tall condensed blue numerals on the left, and "THU 31", "25°C" on top right.
 */
@Composable
fun AppleCondensedClock(
    modifier: Modifier = Modifier,
    timeString: String = "14:38",
    dayOfWeek: String = "THU",
    dayOfMonth: Int = 31,
    temperature: Int = 25,
    isNightMode: Boolean = false
) {
    val clockColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleBlue,
        animationSpec = tween(400),
        label = "condensedClockColor"
    )

    val parts = timeString.split(":")
    val hours = parts.getOrNull(0) ?: "14"
    val minutes = parts.getOrNull(1) ?: "38"

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 16.dp)
            .testTag("apple_condensed_clock"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Tall Condensed Apple Numerals (matching image_e4692b65.png)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = hours.padStart(2, '0'),
                    fontSize = 200.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = clockColor,
                    lineHeight = 200.sp,
                    letterSpacing = (-12).sp
                )

                // Colon dots
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(clockColor)
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(clockColor)
                    )
                }

                Text(
                    text = minutes.padStart(2, '0'),
                    fontSize = 200.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    color = clockColor,
                    lineHeight = 200.sp,
                    letterSpacing = (-12).sp
                )
            }

            // Top Right: "THU 31", "25°C" (matching image_e4692b65.png)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(top = 18.dp, end = 24.dp)
            ) {
                Row {
                    Text(
                        text = "$dayOfWeek ",
                        color = clockColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dayOfMonth",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$temperature°C",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
