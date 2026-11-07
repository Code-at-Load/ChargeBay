package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WidgetId
import com.example.state.StandByUiState
import com.example.ui.components.AnalogClock
import com.example.ui.components.AppleCalendarWidget
import com.example.ui.components.BatteryGaugeWidget
import com.example.ui.components.BigDateWidget
import com.example.ui.components.MiniMusicWidget
import com.example.ui.components.RemindersWidget
import com.example.ui.components.WeatherWidget
import com.example.ui.theme.AppleNightRed

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DualWidgetsScreen(
    modifier: Modifier = Modifier,
    state: StandByUiState,
    onCycleLeftWidget: () -> Unit,
    onCycleRightWidget: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onToggleReminder: (String) -> Unit,
    onAddReminder: (String) -> Unit = {},
    onDeleteReminder: (String) -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    onToggleDeviceStatus: (String) -> Unit = {},
    onOpenWidgetCustomizer: () -> Unit = {},
    onVolumeChange: (Float) -> Unit = {}
) {
    val leftWidget = state.leftWidgetStack.getOrNull(state.leftWidgetIndex) ?: WidgetId.BIG_DATE
    val rightWidget = state.rightWidgetStack.getOrNull(state.rightWidgetIndex) ?: WidgetId.CALENDAR

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("dual_widgets_screen"),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT WIDGET STACK
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onDragEnd = {
                            if (kotlin.math.abs(totalDrag) > 60f) {
                                onCycleLeftWidget()
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            totalDrag += dragAmount
                        }
                    )
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCycleLeftWidget() },
                    onLongClick = { onOpenWidgetCustomizer() }
                )
                .testTag("left_widget_stack")
        ) {
            AnimatedContent(
                targetState = leftWidget,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
                },
                label = "leftWidgetAnimation",
                modifier = Modifier.fillMaxSize()
            ) { targetWidget ->
                RenderWidget(
                    widgetId = targetWidget,
                    state = state,
                    onTogglePlayPause = onTogglePlayPause,
                    onToggleReminder = onToggleReminder,
                    onAddReminder = onAddReminder,
                    onDeleteReminder = onDeleteReminder,
                    onRefreshWeather = onRefreshWeather,
                    onToggleDeviceStatus = onToggleDeviceStatus,
                    onVolumeChange = onVolumeChange
                )
            }

            // Vertical indicator dots on left edge (hidden for single clock widgets as requested)
            val showLeftDots = state.leftWidgetStack.size > 1 && leftWidget != WidgetId.ANALOG_CLOCK && leftWidget != WidgetId.WORLD_CLOCK
            if (showLeftDots) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.leftWidgetStack.forEachIndexed { index, _ ->
                        val isCurrent = index == state.leftWidgetIndex
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 5.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) {
                                        if (state.isNightModeActive) AppleNightRed else Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }
            }
        }

        // RIGHT WIDGET STACK
        var isRightTouchInVolumeArea = false
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .pointerInput(Unit) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { offset ->
                            totalDrag = 0f
                            // Prevent widget stack cycling when touching near the right-edge volume slider
                            isRightTouchInVolumeArea = (size.width - offset.x) < 60.dp.toPx()
                        },
                        onDragEnd = {
                            if (!isRightTouchInVolumeArea && kotlin.math.abs(totalDrag) > 60f) {
                                onCycleRightWidget()
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            if (!isRightTouchInVolumeArea && !change.isConsumed) {
                                totalDrag += dragAmount
                            }
                        }
                    )
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCycleRightWidget() },
                    onLongClick = { onOpenWidgetCustomizer() }
                )
                .testTag("right_widget_stack")
        ) {
            AnimatedContent(
                targetState = rightWidget,
                transitionSpec = {
                    (slideInVertically { height -> height / 2 } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut())
                },
                label = "rightWidgetAnimation",
                modifier = Modifier.fillMaxSize()
            ) { targetWidget ->
                RenderWidget(
                    widgetId = targetWidget,
                    state = state,
                    onTogglePlayPause = onTogglePlayPause,
                    onToggleReminder = onToggleReminder,
                    onAddReminder = onAddReminder,
                    onDeleteReminder = onDeleteReminder,
                    onRefreshWeather = onRefreshWeather,
                    onToggleDeviceStatus = onToggleDeviceStatus,
                    onVolumeChange = onVolumeChange
                )
            }

            // Vertical indicator dots on right edge (hidden for single clock widgets as requested)
            val showRightDots = state.rightWidgetStack.size > 1 && rightWidget != WidgetId.ANALOG_CLOCK && rightWidget != WidgetId.WORLD_CLOCK
            if (showRightDots) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    state.rightWidgetStack.forEachIndexed { index, _ ->
                        val isCurrent = index == state.rightWidgetIndex
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 5.dp else 4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) {
                                        if (state.isNightModeActive) AppleNightRed else Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.2f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderWidget(
    widgetId: WidgetId,
    state: StandByUiState,
    onTogglePlayPause: () -> Unit,
    onToggleReminder: (String) -> Unit,
    onAddReminder: (String) -> Unit,
    onDeleteReminder: (String) -> Unit,
    onRefreshWeather: () -> Unit,
    onToggleDeviceStatus: (String) -> Unit,
    onVolumeChange: (Float) -> Unit = {}
) {
    when (widgetId) {
        WidgetId.BIG_DATE -> BigDateWidget(
            dayOfWeek = state.currentDayOfWeek,
            monthShort = state.currentMonthShort,
            dayOfMonth = state.currentDayOfMonth,
            isNightMode = state.isNightModeActive
        )
        WidgetId.ANALOG_CLOCK -> Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141416).copy(alpha = if (state.isNightModeActive) 0.3f else 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            AnalogClock(
                isNightMode = state.isNightModeActive,
                smoothSweep = true
            )
        }
        WidgetId.WORLD_CLOCK -> WorldClockWidget(
            currentTime = state.currentTimeString,
            isNightMode = state.isNightModeActive
        )
        WidgetId.BATTERY_STATUS -> BatteryGaugeWidget(
            devices = state.connectedDevices,
            isNightMode = state.isNightModeActive,
            onToggleDeviceStatus = onToggleDeviceStatus
        )
        WidgetId.CALENDAR -> AppleCalendarWidget(
            monthFull = state.currentMonthFull,
            currentDay = state.currentDayOfMonth,
            year = state.currentYear,
            isNightMode = state.isNightModeActive
        )
        WidgetId.WEATHER -> WeatherWidget(
            city = state.weatherCity,
            temperature = state.weatherTemp,
            condition = state.weatherCondition,
            high = state.weatherHigh,
            low = state.weatherLow,
            isNightMode = state.isNightModeActive,
            onRefresh = onRefreshWeather
        )
        WidgetId.MINI_MUSIC -> MiniMusicWidget(
            track = state.currentTrack,
            isPlaying = state.isMediaPlaying,
            onTogglePlayPause = onTogglePlayPause,
            volume = state.volume,
            onVolumeChange = onVolumeChange,
            isNightMode = state.isNightModeActive
        )
        WidgetId.REMINDERS -> RemindersWidget(
            items = state.reminders,
            onToggleItem = onToggleReminder,
            onAddItem = onAddReminder,
            onDeleteItem = onDeleteReminder,
            isNightMode = state.isNightModeActive
        )
    }
}

@Composable
fun WorldClockWidget(
    modifier: Modifier = Modifier,
    currentTime: String,
    isNightMode: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.7f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CUPERTINO",
                        color = if (isNightMode) AppleNightRed.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = currentTime,
                        color = if (isNightMode) AppleNightRed else Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today, +0HRS",
                        color = if (isNightMode) AppleNightRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "TOKYO",
                        color = if (isNightMode) AppleNightRed.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "01:48",
                        color = if (isNightMode) AppleNightRed else Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tomorrow, +16HRS",
                        color = if (isNightMode) AppleNightRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
