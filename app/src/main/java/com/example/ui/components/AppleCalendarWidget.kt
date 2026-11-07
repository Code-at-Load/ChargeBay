package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleRed
import java.util.Calendar

@Composable
fun AppleCalendarWidget(
    modifier: Modifier = Modifier,
    monthFull: String,
    currentDay: Int,
    year: Int,
    isNightMode: Boolean = false
) {
    val headerColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleRed,
        animationSpec = tween(500),
        label = "calendarHeader"
    )
    val dayHighlightBg by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleRed,
        animationSpec = tween(500),
        label = "calendarHighlight"
    )
    val normalTextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.85f) else Color.White,
        animationSpec = tween(500),
        label = "calendarText"
    )
    val dimTextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.4f),
        animationSpec = tween(500),
        label = "calendarDimText"
    )

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-indexed for Sunday
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.7f))
            .padding(14.dp)
            .testTag("apple_calendar_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthFull,
                    color = headerColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                // Subtle Pill handle bar indicator
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isNightMode) AppleNightRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Weekday labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dayLetter ->
                    Text(
                        text = dayLetter,
                        color = dimTextColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Days grid (up to 5-6 rows)
            val totalCells = 35 // 5 rows x 7 cols
            val rows = (0 until 5).toList()

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                rows.forEach { rowIdx ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIdx in 0 until 7) {
                            val cellIndex = rowIdx * 7 + colIdx
                            val dayNum = cellIndex - firstDayOfWeek + 1
                            val isValidDay = dayNum in 1..maxDays
                            val isToday = dayNum == currentDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.1f),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isValidDay) {
                                    if (isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(dayHighlightBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$dayNum",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "$dayNum",
                                            color = normalTextColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
