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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleRed

@Composable
fun BigDateWidget(
    modifier: Modifier = Modifier,
    dayOfWeek: String,
    monthShort: String,
    dayOfMonth: Int,
    isNightMode: Boolean = false
) {
    val dayOfWeekColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleRed,
        animationSpec = tween(500),
        label = "bigDateDayColor"
    )
    val monthColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.9f),
        animationSpec = tween(500),
        label = "bigDateMonthColor"
    )
    val numberColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "bigDateNumberColor"
    )

    // Capitalize first letter only (e.g., "Tue", "Jul")
    val formattedDay = dayOfWeek.lowercase().replaceFirstChar { it.uppercase() }
    val formattedMonth = monthShort.lowercase().replaceFirstChar { it.uppercase() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.7f))
            .padding(16.dp)
            .testTag("big_date_widget"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header e.g. "Tue Jul"
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$formattedDay ",
                    color = dayOfWeekColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = formattedMonth,
                    color = monthColor,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.5).sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Big Bold Day Numeral (like Apple 4)
            Text(
                text = "$dayOfMonth",
                color = numberColor,
                fontSize = 112.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-4).sp,
                lineHeight = 104.sp
            )
        }
    }
}
