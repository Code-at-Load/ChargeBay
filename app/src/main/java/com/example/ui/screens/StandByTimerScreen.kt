package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed

/**
 * Authentic Apple StandBy Fullscreen Countdown Timer matching reference image 4.
 * Features a real-time filling luminous orange progress bar on the left edge,
 * circular translucent Pause and Cancel buttons, and oversized countdown typography.
 */
@Composable
fun StandByTimerScreen(
    remainingSeconds: Int = 544, // e.g. 9:04
    totalSeconds: Int = 600,     // 10 minutes
    isPaused: Boolean = false,
    isNightMode: Boolean = false,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit
) {
    val elapsedFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = elapsedFraction,
        animationSpec = tween(500),
        label = "timerProgressAnimation"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%d:%02d", minutes, seconds)

    val orangeFillColor = if (isNightMode) AppleNightRed else Color(0xFFFF9500)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("standby_timer_screen")
    ) {
        // Real-Time Luminous Orange Filling Bar along the Left Edge (matching image 4)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillWidth = size.width * 0.16f
            val fillHeight = size.height * animatedProgress

            // Inactive track line
            drawRoundRect(
                color = orangeFillColor.copy(alpha = 0.20f),
                topLeft = Offset(0f, 0f),
                size = Size(fillWidth, size.height),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )

            // Dynamic live filling of orange color
            if (animatedProgress > 0f) {
                drawRoundRect(
                    color = orangeFillColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                )
            }
        }

        // Action Controls (Pause / Resume & Cancel) on Left Side
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 72.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause / Resume Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onPauseResume() }
                    .testTag("timer_pause_resume_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Cancel Button
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .clickable { onCancel() }
                    .testTag("timer_cancel_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Center / Right: Big Clean Countdown Time
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 120.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Timer",
                color = if (isNightMode) AppleNightRed.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.9f),
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeFormatted,
                color = if (isNightMode) AppleNightRed else Color.White,
                fontSize = 110.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = (-2).sp,
                lineHeight = 110.sp
            )
        }
    }
}
