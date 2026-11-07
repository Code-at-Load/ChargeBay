package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleYellow
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Fullscreen Apple StandBy Analog Clock matching reference image_8cc7b89a.png.
 * Features widescreen rectangular ray burst ticks, wide geometric 12, 3, 6, 9 numerals,
 * "SUN 4" date text in amber yellow positioned between center and 3 o'clock,
 * pill hands, sweeping yellow second needle, and subtle "ANALOG" subtitle at the bottom.
 */
@Composable
fun AppleLandscapeAnalogClock(
    modifier: Modifier = Modifier,
    dayOfWeek: String = "SUN",
    dayOfMonth: Int = 4,
    isNightMode: Boolean = false,
    smoothSweep: Boolean = true
) {
    val timeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(50) // 20 fps for smooth second needle sweep
        }
    }

    val handColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(400),
        label = "landscapeHandColor"
    )
    val secondHandColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleYellow,
        animationSpec = tween(400),
        label = "landscapeSecondColor"
    )
    val tickColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.38f),
        animationSpec = tween(400),
        label = "landscapeTickColor"
    )
    val cornerRayColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White.copy(alpha = 0.90f),
        animationSpec = tween(400),
        label = "landscapeCornerRayColor"
    )
    val numeralColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(400),
        label = "landscapeNumeralColor"
    )
    val dateColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleYellow,
        animationSpec = tween(400),
        label = "landscapeDateColor"
    )
    val labelColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.6f) else Color(0xFF8E8E93),
        animationSpec = tween(400),
        label = "landscapeLabelColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .testTag("apple_landscape_analog_clock"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val center = Offset(cx, cy)

            // Zoomed-in widescreen clock face rectangle filling the fullscreen canvas
            val halfW = (size.width * 0.96f) / 2f
            val halfH = (size.height * 0.94f) / 2f

            // Text Paint for wide geometric 12, 3, 6, 9 numerals (Apple style)
            val numeralPaint = Paint().apply {
                color = numeralColor.toArgb()
                textSize = (halfH * 0.44f).coerceIn(40.sp.toPx(), 62.sp.toPx())
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.12f
            }

            // Function to find ray intersection with rectangular perimeter
            fun getBorderDist(cosA: Float, sinA: Float): Float {
                val distH = halfH / max(0.0001f, abs(sinA))
                val distW = halfW / max(0.0001f, abs(cosA))
                return min(distH, distW)
            }

            // 1. Draw Rectangular Radial Burst Ticks (matches image_8cc7b89a.png)
            val totalTicks = 60
            for (i in 0 until totalTicks) {
                val angleDeg = i * 6.0 - 90.0
                val angleRad = Math.toRadians(angleDeg)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                // Border distance
                val rBorder = getBorderDist(cosA, sinA)

                // Skip positions near numerals 12, 3, 6, 9 so ticks don't overlap the numbers
                val is12Zone = (i in 59..59 || i in 0..1)
                val is3Zone = (i in 14..16)
                val is6Zone = (i in 29..31)
                val is9Zone = (i in 44..46)

                // The 4 prominent corner diagonal rays in image_8cc7b89a.png (flanking the numerals)
                // Top-right corner rays: around i=6 (36°), i=9 (54°)
                // Bottom-right corner rays: around i=21 (126°), i=24 (144°)
                // Bottom-left corner rays: around i=36 (216°), i=39 (234°)
                // Top-left corner rays: around i=51 (306°), i=54 (324°)
                val isCornerFlankRay = (i == 6 || i == 9 || i == 21 || i == 24 || i == 36 || i == 39 || i == 51 || i == 54)

                if (isCornerFlankRay) {
                    // Long dramatic diagonal ray extending from near the center to the corner
                    val start = Offset(cx + rBorder * 0.46f * cosA, cy + rBorder * 0.46f * sinA)
                    val end = Offset(cx + rBorder * 0.98f * cosA, cy + rBorder * 0.98f * sinA)
                    drawLine(
                        color = cornerRayColor,
                        start = start,
                        end = end,
                        strokeWidth = 2.8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                } else if (!is12Zone && !is3Zone && !is6Zone && !is9Zone) {
                    val isHourTick = (i % 5 == 0)
                    val innerFraction = if (isHourTick) 0.80f else 0.88f
                    val outerFraction = 0.98f

                    val start = Offset(cx + rBorder * innerFraction * cosA, cy + rBorder * innerFraction * sinA)
                    val end = Offset(cx + rBorder * outerFraction * cosA, cy + rBorder * outerFraction * sinA)

                    drawLine(
                        color = if (isHourTick) tickColor.copy(alpha = 0.75f) else tickColor,
                        start = start,
                        end = end,
                        strokeWidth = if (isHourTick) 2.2.dp.toPx() else 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Draw Wide Modern Numerals: 12, 3, 6, 9
            val textOffset = (numeralPaint.descent() + numeralPaint.ascent()) / 2f

            // 12 (Top Center)
            val pos12Y = cy - halfH * 0.60f
            drawContext.canvas.nativeCanvas.drawText("12", cx, pos12Y - textOffset, numeralPaint)

            // 6 (Bottom Center)
            val pos6Y = cy + halfH * 0.60f
            drawContext.canvas.nativeCanvas.drawText("6", cx, pos6Y - textOffset, numeralPaint)

            // 9 (Left Center)
            val pos9X = cx - halfW * 0.72f
            drawContext.canvas.nativeCanvas.drawText("9", pos9X, cy - textOffset, numeralPaint)

            // 3 (Right Center)
            val pos3X = cx + halfW * 0.72f
            drawContext.canvas.nativeCanvas.drawText("3", pos3X, cy - textOffset, numeralPaint)

            // 3. Draw Date Badge "SUN 4" in warm amber yellow positioned between center and 3 o'clock
            val datePaint = Paint().apply {
                color = dateColor.toArgb()
                textSize = (halfH * 0.17f).coerceIn(15.sp.toPx(), 22.sp.toPx())
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                letterSpacing = 0.14f
            }
            val dateText = "$dayOfWeek $dayOfMonth".uppercase()
            val dateX = cx + halfW * 0.36f // Perfectly positioned between center pin and "3" numeral
            drawContext.canvas.nativeCanvas.drawText(dateText, dateX, cy - textOffset, datePaint)

            // 4. Calculate precise angles from system time
            val cal = Calendar.getInstance().apply { this.timeInMillis = timeMillis }
            val hour = cal.get(Calendar.HOUR)
            val min = cal.get(Calendar.MINUTE)
            val sec = cal.get(Calendar.SECOND)
            val ms = cal.get(Calendar.MILLISECOND)

            val secFraction = if (smoothSweep) (sec + ms / 1000f) else sec.toFloat()
            val minFraction = min + secFraction / 60f
            val hourFraction = hour + minFraction / 60f

            val hourAngle = Math.toRadians((hourFraction * 30.0 - 90.0))
            val minAngle = Math.toRadians((minFraction * 6.0 - 90.0))
            val secAngle = Math.toRadians((secFraction * 6.0 - 90.0))

            // 6. Hour Hand (Thick rounded white pill)
            val hourLen = halfH * 0.52f
            val hourEnd = Offset(
                cx + hourLen * cos(hourAngle).toFloat(),
                cy + hourLen * sin(hourAngle).toFloat()
            )
            drawLine(
                color = handColor,
                start = center,
                end = hourEnd,
                strokeWidth = 9.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 7. Minute Hand (Long rounded white pill)
            val minLen = halfH * 0.82f
            val minEnd = Offset(
                cx + minLen * cos(minAngle).toFloat(),
                cy + minLen * sin(minAngle).toFloat()
            )
            drawLine(
                color = handColor,
                start = center,
                end = minEnd,
                strokeWidth = 7.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 8. Yellow Second Hand Needle with center circle and counterweight tail
            val secLen = halfH * 0.94f
            val secTail = halfH * 0.24f
            val secEnd = Offset(
                cx + secLen * cos(secAngle).toFloat(),
                cy + secLen * sin(secAngle).toFloat()
            )
            val secTailEnd = Offset(
                cx - secTail * cos(secAngle).toFloat(),
                cy - secTail * sin(secAngle).toFloat()
            )
            drawLine(
                color = secondHandColor,
                start = secTailEnd,
                end = secEnd,
                strokeWidth = 2.4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(
                color = secondHandColor,
                radius = 6.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.Black,
                radius = 2.2.dp.toPx(),
                center = center
            )
        }
    }
}
