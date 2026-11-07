package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryDevice
import com.example.model.BatteryDeviceType
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.AppleYellow

@Composable
fun BatteryGaugeWidget(
    modifier: Modifier = Modifier,
    devices: List<BatteryDevice>,
    isNightMode: Boolean = false,
    onToggleDeviceStatus: (String) -> Unit = {}
) {
    val activeColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else AppleMint,
        animationSpec = tween(500),
        label = "batteryActiveColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "batteryTextColor"
    )
    val subtextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f),
        animationSpec = tween(500),
        label = "batterySubtextColor"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.7f))
            .padding(14.dp)
            .testTag("battery_gauge_widget"),
        contentAlignment = Alignment.Center
    ) {
        if (devices.size <= 1) {
            // Single Device Mode: Big Centered Ring
            val dev = devices.firstOrNull() ?: BatteryDevice("phone", "Phone", BatteryDeviceType.PHONE, 88)
            SingleDeviceRing(
                device = dev,
                activeColor = activeColor,
                textColor = textColor,
                isNightMode = isNightMode,
                modifier = Modifier
                    .fillMaxHeight(0.88f)
                    .aspectRatio(1f)
                    .clickable { onToggleDeviceStatus(dev.id) }
            )
        } else {
            // Up to 4 Devices Mode: 2x2 Grid matching Apple iOS Batteries Widget
            val topRow = devices.take(2)
            val bottomRow = devices.drop(2).take(2)

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row (e.g. Phone & Earphones)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    topRow.forEach { device ->
                        CompactDeviceBatteryItem(
                            device = device,
                            activeColor = activeColor,
                            textColor = textColor,
                            subtextColor = subtextColor,
                            isNightMode = isNightMode,
                            onClick = { onToggleDeviceStatus(device.id) }
                        )
                    }
                }

                // Bottom Row (e.g. Smartwatch & Accessory/Case)
                if (bottomRow.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        bottomRow.forEach { device ->
                            CompactDeviceBatteryItem(
                                device = device,
                                activeColor = activeColor,
                                textColor = textColor,
                                subtextColor = subtextColor,
                                isNightMode = isNightMode,
                                onClick = { onToggleDeviceStatus(device.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDeviceBatteryItem(
    device: BatteryDevice,
    activeColor: Color,
    textColor: Color,
    subtextColor: Color,
    isNightMode: Boolean,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (device.deviceType) {
        BatteryDeviceType.PHONE -> Icons.Default.PhoneAndroid
        BatteryDeviceType.EARPHONES -> Icons.Default.Headphones
        BatteryDeviceType.WATCH -> Icons.Default.Watch
        BatteryDeviceType.ACCESSORY -> Icons.Default.Widgets
    }

    val ringColor = when {
        isNightMode -> AppleNightRed
        device.level <= 20 -> Color(0xFFFF453A) // Low battery red
        device.level <= 40 -> AppleYellow
        else -> AppleMint
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 4.8.dp.toPx()
                // Background Track
                drawCircle(
                    color = Color.White.copy(alpha = if (isNightMode) 0.12f else 0.16f),
                    style = Stroke(width = strokeW)
                )
                // Battery sweep arc
                val sweep = (device.level / 100f) * 360f
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (device.isCharging) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = ringColor,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = device.name,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "${device.level}%",
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = device.name,
            color = subtextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun SingleDeviceRing(
    device: BatteryDevice,
    activeColor: Color,
    textColor: Color,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 12.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = if (isNightMode) 0.12f else 0.16f),
                style = Stroke(width = strokeW)
            )
            val sweep = (device.level / 100f) * 360f
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (device.isCharging) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Charging",
                    tint = activeColor,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = "${device.level}%",
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = device.name,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
