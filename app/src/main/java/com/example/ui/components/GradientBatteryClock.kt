package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Authentic Apple StandBy Lavender Gradient Clock (matching reference image_9c1c9e0f.png):
 * - Perfectly scaled typography that never clips or truncates digits (e.g. "12:07") on any aspect ratio
 * - Smooth, seamless ambient gradient with gentle, continuous live shifting
 * - Frosted glass luminous digits with soft pastel lilac/orchid gradient and ethereal glow
 * - Top-right circular Apple Mint green battery complication matching iOS StandBy
 */
@Composable
fun GradientBatteryClock(
    modifier: Modifier = Modifier,
    timeString: String = "5:18",
    batteryLevel: Int = 92,
    isCharging: Boolean = false,
    isNightMode: Boolean = false
) {
    // Continuous 22-second ambient phase rotation for live slow gradient shifting
    val infiniteTransition = rememberInfiniteTransition(label = "gradientAmbientShift")
    val phaseAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambientPhaseAngle"
    )

    val batteryRingColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleMint,
        animationSpec = tween(400),
        label = "gradientBatteryColor"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("gradient_battery_clock"),
        contentAlignment = Alignment.Center
    ) {
        val h = maxHeight
        val w = maxWidth

        // Responsive font scaling ensuring 4-digit and 5-digit times (e.g. "12:07") NEVER truncate or clip
        val digitCount = timeString.length.coerceAtLeast(4)
        val maxAllowedWidth = w.value * 0.84f
        val approxCharWidthRatio = 0.56f
        val fontByWidth = maxAllowedWidth / (digitCount * approxCharWidthRatio)
        val fontByHeight = h.value * 0.70f
        val fontSize = minOf(fontByHeight, fontByWidth).sp

        // Smooth Seamless Ambient Gradient Background (matching reference image_9c1c9e0f.png)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            val orbitX = canvasW * 0.45f
            val orbitY = canvasH * 0.35f

            val startX = canvasW * 0.5f + orbitX * cos(phaseAngle)
            val startY = canvasH * 0.5f + orbitY * sin(phaseAngle)
            val endX = canvasW * 0.5f - orbitX * cos(phaseAngle)
            val endY = canvasH * 0.5f - orbitY * sin(phaseAngle)

            val gradientColors = if (isNightMode) {
                listOf(
                    Color(0xFF380808),
                    Color(0xFF220000),
                    Color(0xFF140000),
                    Color.Black
                )
            } else {
                // Authentic Apple StandBy Lavender Gradient palette (smooth, seamless blend)
                listOf(
                    Color(0xFF5B3868), // Dusky plum base
                    Color(0xFF7A4B88), // Rich violet
                    Color(0xFF9E65AD), // Vibrant orchid
                    Color(0xFFBA84CB), // Luminous lilac
                    Color(0xFFCCA0DA)  // Soft pastel lavender highlight
                )
            }

            drawRect(
                brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY)
                )
            )

            // Very soft, expansive ambient radial warmth with broad falloff (no visible hard circles)
            val centerGlowX = canvasW * (0.5f + 0.15f * cos(phaseAngle * 0.5f))
            val centerGlowY = canvasH * (0.5f + 0.12f * sin(phaseAngle * 0.5f))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isNightMode) {
                        listOf(AppleNightRed.copy(alpha = 0.15f), Color.Transparent)
                    } else {
                        listOf(Color(0xFFF3C7FF).copy(alpha = 0.28f), Color.Transparent)
                    },
                    center = Offset(centerGlowX, centerGlowY),
                    radius = maxOf(canvasW, canvasH) * 0.95f
                )
            )
        }

        // Frosted Glass Digits: Multi-stop luminous gradient adapting to ambient shift
        val frostedBrush = if (isNightMode) {
            Brush.verticalGradient(
                colors = listOf(
                    AppleNightRed.copy(alpha = 0.96f),
                    AppleNightRed.copy(alpha = 0.78f)
                )
            )
        } else {
            // Ethereal pastel orchid/pink frosted digits matching Apple reference
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF6FF),
                    Color(0xFFF8CBFF),
                    Color(0xFFEAA6FF),
                    Color(0xFFF4B8FF)
                )
            )
        }

        // Centered StandBy Time (guaranteed single-line, fully visible and centered)
        Text(
            text = timeString,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            style = TextStyle(
                brush = frostedBrush,
                shadow = Shadow(
                    color = if (isNightMode) Color(0x80FF3B30) else Color(0x66F5A7FF),
                    offset = Offset(0f, 4f),
                    blurRadius = 22f
                )
            ),
            letterSpacing = (-3).sp
        )

        // Top-Right Corner: Circular Apple Mint Battery Complication
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 22.dp, end = 26.dp)
                .size(52.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 4.dp.toPx()
                // Inactive track
                drawCircle(
                    color = Color.White.copy(alpha = 0.22f),
                    style = Stroke(width = strokeW)
                )
                // Active sweep arc
                val sweep = (batteryLevel / 100f) * 360f
                drawArc(
                    color = batteryRingColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = batteryRingColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Text(
                    text = "$batteryLevel",
                    color = batteryRingColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
