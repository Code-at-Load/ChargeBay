package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleOrange
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    modifier: Modifier = Modifier,
    isNightMode: Boolean = false,
    smoothSweep: Boolean = true
) {
    // Continuous time ticker updating at high frequency so second hand always moves
    val timeMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(50) // 20 updates/sec for silky smooth sweeping second hand
        }
    }

    val handColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(400),
        label = "handColor"
    )
    val secondHandColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleOrange,
        animationSpec = tween(400),
        label = "secondHandColor"
    )
    val tickColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.45f),
        animationSpec = tween(400),
        label = "tickColor"
    )
    val hourTickColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.85f),
        animationSpec = tween(400),
        label = "hourTickColor"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f

        // Text Paint for Apple Clock Numbers 1..12
        val textPaint = Paint().apply {
            color = if (isNightMode) AppleNightRed.toArgb() else Color.White.toArgb()
            textSize = (radius * 0.17f).coerceIn(12.sp.toPx(), 26.sp.toPx())
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        // Draw 60 Minute/Hour tick markings
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6 - 90).toDouble())
            val isHour = i % 5 == 0
            val tickLen = if (isHour) radius * 0.08f else radius * 0.045f
            val strokeW = if (isHour) 2.5.dp.toPx() else 1.2.dp.toPx()
            val startR = radius - tickLen - 2.dp.toPx()
            val endR = radius - 2.dp.toPx()

            val start = Offset(
                x = (center.x + startR * cos(angle)).toFloat(),
                y = (center.y + startR * sin(angle)).toFloat()
            )
            val end = Offset(
                x = (center.x + endR * cos(angle)).toFloat(),
                y = (center.y + endR * sin(angle)).toFloat()
            )

            drawLine(
                color = if (isHour) hourTickColor else tickColor,
                start = start,
                end = end,
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }

        // Draw Hour Numerals (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)
        val numRadius = radius * 0.74f
        for (h in 1..12) {
            val angle = Math.toRadians((h * 30 - 90).toDouble())
            val numX = (center.x + numRadius * cos(angle)).toFloat()
            val numY = (center.y + numRadius * sin(angle)).toFloat()
            val textVerticalOffset = (textPaint.descent() + textPaint.ascent()) / 2f
            drawContext.canvas.nativeCanvas.drawText(
                h.toString(),
                numX,
                numY - textVerticalOffset,
                textPaint
            )
        }

        // Calculate continuous angles from system millis
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val sec = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val secondFraction = if (smoothSweep) (sec + millis / 1000f) else sec.toFloat()
        val minuteFraction = minute + secondFraction / 60f
        val hourFraction = hour + minuteFraction / 60f

        val hourAngle = Math.toRadians((hourFraction * 30 - 90).toDouble())
        val minuteAngle = Math.toRadians((minuteFraction * 6 - 90).toDouble())
        val secondAngle = Math.toRadians((secondFraction * 6 - 90).toDouble())

        // Hour Hand
        val hourLen = radius * 0.44f
        val hourEnd = Offset(
            x = (center.x + hourLen * cos(hourAngle)).toFloat(),
            y = (center.y + hourLen * sin(hourAngle)).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = hourEnd,
            strokeWidth = 4.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute Hand
        val minuteLen = radius * 0.62f
        val minuteEnd = Offset(
            x = (center.x + minuteLen * cos(minuteAngle)).toFloat(),
            y = (center.y + minuteLen * sin(minuteAngle)).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = minuteEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Orange Second Hand with counterweight tail
        val secondLen = radius * 0.82f
        val secondTailLen = radius * 0.16f
        val secondEnd = Offset(
            x = (center.x + secondLen * cos(secondAngle)).toFloat(),
            y = (center.y + secondLen * sin(secondAngle)).toFloat()
        )
        val secondTail = Offset(
            x = (center.x - secondTailLen * cos(secondAngle)).toFloat(),
            y = (center.y - secondTailLen * sin(secondAngle)).toFloat()
        )
        drawLine(
            color = secondHandColor,
            start = secondTail,
            end = secondEnd,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center Pin
        drawCircle(
            color = secondHandColor,
            radius = 4.dp.toPx(),
            center = center
        )
        drawCircle(
            color = Color.Black,
            radius = 1.8.dp.toPx(),
            center = center
        )
    }
}
