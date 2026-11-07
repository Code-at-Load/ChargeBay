package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.TitanOneFontFamily

/**
 * Authentic Apple StandBy Bubbly Overlapping Clock recreation matching reference images
 * image_54ce9bf1.png (Electric Blue duotone) and image_981d2799.png (Candy Pink/Purple):
 * - Bulbous, oversized, rounded typography with jaunty tilt angles
 * - Negative horizontal spacing causing digits to naturally overlap each other
 * - Frosted circular glass colon spheres overlapping adjacent digits
 * - Bottom-right bedside alarm badge "⏰ 6:30 AM"
 * - AMOLED pure black background (#000000) with zero outer bezel/border
 * - Tap to switch between Electric Blue and Candy Pink/Purple colorways
 */
@Composable
fun AppleBubblyClock(
    modifier: Modifier = Modifier,
    timeString: String = "10:35",
    paletteIndex: Int = 0,
    alarmString: String = "6:30 AM",
    isNightMode: Boolean = false,
    onTogglePalette: () -> Unit = {}
) {
    // Palette 0: Electric Blue Duotone (image_54ce9bf1.png)
    // Palette 1: Candy Pink / Lavender / Orchid Purple (image_981d2799.png)
    val isBluePalette = (paletteIndex % 2 == 0)

    val targetH1Color = when {
        isNightMode -> AppleNightRed
        isBluePalette -> Color(0xFF388AF6) // Light Sky Blue
        else -> Color(0xFFED5B9D)         // Candy Bubblegum Pink
    }
    val targetH2Color = when {
        isNightMode -> AppleNightRed
        isBluePalette -> Color(0xFF005AC1) // Vibrant Royal Blue
        else -> Color(0xFFED5B9D)         // Candy Bubblegum Pink
    }
    val targetDotColor = when {
        isNightMode -> AppleNightRed.copy(alpha = 0.55f)
        isBluePalette -> Color(0xFFDCE6F2).copy(alpha = 0.88f) // Frosted ice glass
        else -> Color(0xFF8E8E93).copy(alpha = 0.85f)          // Frosted silver gray
    }
    val targetM1Color = when {
        isNightMode -> AppleNightRed.copy(alpha = 0.85f)
        isBluePalette -> Color(0xFF004396) // Deep Navy Cobalt
        else -> Color(0xFF988BFA)         // Pastel Periwinkle Lavender
    }
    val targetM2Color = when {
        isNightMode -> AppleNightRed.copy(alpha = 0.95f)
        isBluePalette -> Color(0xFF4C9DFE) // Electric Blue
        else -> Color(0xFFAF32C4)         // Vibrant Orchid Purple
    }

    val h1Color by animateColorAsState(targetH1Color, animationSpec = tween(400), label = "h1Color")
    val h2Color by animateColorAsState(targetH2Color, animationSpec = tween(400), label = "h2Color")
    val dotColor by animateColorAsState(targetDotColor, animationSpec = tween(400), label = "dotColor")
    val m1Color by animateColorAsState(targetM1Color, animationSpec = tween(400), label = "m1Color")
    val m2Color by animateColorAsState(targetM2Color, animationSpec = tween(400), label = "m2Color")

    // Parse time string: omit leading zero on single digit hours (e.g. "9:41" or "10:35")
    val parts = timeString.split(":")
    val rawHours = parts.getOrNull(0)?.trim() ?: "10"
    val rawMinutes = parts.getOrNull(1)?.trim() ?: "35"

    val formattedHour = if (rawHours.startsWith("0") && rawHours.length > 1) {
        rawHours.substring(1)
    } else {
        rawHours
    }
    val paddedMinutes = rawMinutes.padStart(2, '0')
    val m1Char = paddedMinutes.getOrNull(0)?.toString() ?: "3"
    val m2Char = paddedMinutes.getOrNull(1)?.toString() ?: "5"

    val hasTwoHourDigits = formattedHour.length > 1
    val h1Char = if (hasTwoHourDigits) formattedHour[0].toString() else ""
    val h2Char = if (hasTwoHourDigits) formattedHour[1].toString() else formattedHour

    // AMOLED Edge-to-Edge Pure Black Container without any outer border or frame
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("apple_bubbly_clock")
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val h = maxHeight
            val w = maxWidth

            // Responsive sizing to fill screen edge-to-edge with massive bubbly digits
            val fontSize = if (hasTwoHourDigits) {
                minOf(h.value * 0.94f, w.value * 0.33f).sp
            } else {
                minOf(h.value * 1.02f, w.value * 0.39f).sp
            }
            val discSize = (h * 0.13f).coerceIn(36.dp, 60.dp)
            val discGap = (h * 0.08f).coerceIn(20.dp, 38.dp)

            // Overlapping digit row with negative horizontal offsets and tilts
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Digit 1 (if 10, 11, 12)
                if (hasTwoHourDigits) {
                    Text(
                        text = h1Char,
                        fontFamily = TitanOneFontFamily,
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = h1Color,
                        modifier = Modifier
                            .graphicsLayer(rotationZ = -3.2f)
                            .offset(x = 24.dp)
                    )
                }

                // Hour Digit 2 (or single hour like '9')
                Text(
                    text = h2Char,
                    fontFamily = TitanOneFontFamily,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = h2Color,
                    modifier = Modifier
                        .graphicsLayer(rotationZ = if (hasTwoHourDigits) 1.8f else -2.5f)
                        .offset(x = if (hasTwoHourDigits) (-18).dp else 8.dp)
                )

                // Overlapping Frosted Glass Colon Spheres
                Column(
                    modifier = Modifier
                        .offset(x = if (hasTwoHourDigits) (-34).dp else (-14).dp)
                        .graphicsLayer(rotationZ = 1.0f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(discGap)
                ) {
                    // Top frosted disc
                    Box(
                        modifier = Modifier
                            .size(discSize)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        dotColor,
                                        dotColor.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )

                    // Bottom frosted disc
                    Box(
                        modifier = Modifier
                            .size(discSize)
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        dotColor,
                                        dotColor.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )
                }

                // Minute Digit 1
                Text(
                    text = m1Char,
                    fontFamily = TitanOneFontFamily,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = m1Color,
                    modifier = Modifier
                        .graphicsLayer(rotationZ = 3.4f)
                        .offset(x = if (hasTwoHourDigits) (-50).dp else (-30).dp)
                )

                // Minute Digit 2
                Text(
                    text = m2Char,
                    fontFamily = TitanOneFontFamily,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = m2Color,
                    modifier = Modifier
                        .graphicsLayer(rotationZ = -2.2f)
                        .offset(x = if (hasTwoHourDigits) (-82).dp else (-62).dp)
                )
            }
        }

        // Bottom-Right Bedside Alarm Pill matching reference image image_54ce9bf1.png
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 28.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "Upcoming Alarm",
                tint = if (isNightMode) AppleNightRed else Color(0xFFFF3B30),
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = alarmString,
                color = if (isNightMode) AppleNightRed else Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
