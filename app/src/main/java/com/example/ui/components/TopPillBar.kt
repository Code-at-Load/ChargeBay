package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.state.NightModeOption
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed

@Composable
fun TopPillBar(
    modifier: Modifier = Modifier,
    visible: Boolean,
    batteryLevel: Int,
    isCharging: Boolean,
    simulateCharging: Boolean,
    ambientLux: Float,
    nightModeOption: NightModeOption,
    isNightModeActive: Boolean,
    onToggleNightMode: () -> Unit,
    onToggleSimulateCharging: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartTimer: () -> Unit = {},
    onTriggerCall: () -> Unit = {},
    onOpenWidgetCustomizer: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1C1C1E).copy(alpha = if (isNightModeActive) 0.6f else 0.85f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("top_pill_bar"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Battery Status Chip (Click to toggle charging)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleSimulateCharging() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                if (isCharging || simulateCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = if (isNightModeActive) AppleNightRed else AppleMint,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$batteryLevel%",
                    color = if (isNightModeActive) AppleNightRed else Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Quick Timer Trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onStartTimer() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = "Timer",
                    tint = if (isNightModeActive) AppleNightRed else Color(0xFFFF9500),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Timer",
                    color = if (isNightModeActive) AppleNightRed else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Quick Phone Call Poster Trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTriggerCall() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneCallback,
                    contentDescription = "Call Demo",
                    tint = if (isNightModeActive) AppleNightRed else AppleBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Call",
                    color = if (isNightModeActive) AppleNightRed else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Customize Widgets Trigger
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenWidgetCustomizer() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = "Customize Widgets",
                    tint = if (isNightModeActive) AppleNightRed else AppleMint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Widgets",
                    color = if (isNightModeActive) AppleNightRed else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Red Night Mode Toggle Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleNightMode() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                val icon = when (nightModeOption) {
                    NightModeOption.AUTO -> Icons.Default.BrightnessAuto
                    NightModeOption.ALWAYS_ON -> Icons.Default.DarkMode
                    NightModeOption.ALWAYS_OFF -> Icons.Default.LightMode
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Night Mode",
                    tint = if (isNightModeActive) AppleNightRed else Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (isNightModeActive) "Red Mode" else "Day",
                    color = if (isNightModeActive) AppleNightRed else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Settings button
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .clickable { onOpenSettings() }
                    .testTag("settings_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = if (isNightModeActive) AppleNightRed.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
