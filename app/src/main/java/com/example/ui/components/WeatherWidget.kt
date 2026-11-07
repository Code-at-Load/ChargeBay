package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleYellow

/**
 * Pixel-accurate Apple StandBy Weather Widget strictly matching image_cbe516a4.png:
 * - Pure black background
 * - Top-left city name with white navigation arrow indicator
 * - Giant, thick, bold temperature ("83°") in white
 * - Bottom cluster with weather condition icon, condition text ("Sunny"), and high/low ("H:84° L:61°")
 */
@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    city: String,
    temperature: Int,
    condition: String,
    high: Int,
    low: Int,
    isNightMode: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    val tempColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "weatherTempColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color(0xFFFFCC00),
        animationSpec = tween(500),
        label = "weatherIconColor"
    )
    val subtextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.85f) else Color.White,
        animationSpec = tween(500),
        label = "weatherSubtextColor"
    )

    val weatherIcon = when {
        condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true) || condition.contains("Shower", ignoreCase = true) -> Icons.Default.WaterDrop
        condition.contains("Thunder", ignoreCase = true) || condition.contains("Storm", ignoreCase = true) -> Icons.Default.FlashOn
        condition.contains("Snow", ignoreCase = true) || condition.contains("Ice", ignoreCase = true) -> Icons.Default.AcUnit
        condition.contains("Cloud", ignoreCase = true) || condition.contains("Overcast", ignoreCase = true) -> Icons.Default.Cloud
        condition.contains("Fog", ignoreCase = true) || condition.contains("Mist", ignoreCase = true) || condition.contains("Haze", ignoreCase = true) -> Icons.Default.Air
        else -> Icons.Default.WbSunny
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.Black)
            .clickable { onRefresh() }
            .padding(horizontal = 20.dp, vertical = 18.dp)
            .testTag("weather_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // 1. Top Location Header: City + Arrow (e.g., "Louisville 1")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = city,
                    color = tempColor,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Location Arrow",
                    tint = tempColor,
                    modifier = Modifier
                        .size(13.dp)
                        .rotate(45f)
                )
            }

            // 2. Center: Oversized, Ultra-Bold, Rounded Temperature (e.g., "83°")
            Text(
                text = "$temperature°",
                color = tempColor,
                fontSize = 82.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-2.5).sp,
                lineHeight = 82.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 3. Bottom Cluster: Weather Icon, Condition Text ("Sunny"), High/Low ("H:84° L:61°")
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = condition,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )

                Text(
                    text = condition,
                    color = subtextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "H:$high° L:$low°",
                    color = subtextColor.copy(alpha = if (isNightMode) 0.7f else 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
