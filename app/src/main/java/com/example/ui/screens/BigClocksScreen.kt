package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ClockFaceStyle
import com.example.model.SerifFontOption
import com.example.state.StandByUiState
import com.example.ui.components.AppleBubblyClock
import com.example.ui.components.AppleCondensedClock
import com.example.ui.components.AppleLandscapeAnalogClock
import com.example.ui.components.ElegantBurgundySerifClock
import com.example.ui.components.FullCalendarStandByClock
import com.example.ui.components.GradientBatteryClock
import com.example.ui.components.PhotosStandByClock
import com.example.ui.components.WorldMapClockFace
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleNightRed

/**
 * Fullscreen StandBy Clock Screen with authentic vertical sliding transitions:
 * - Dragging downward or tapping the screen slides the clock face vertically downward
 * - Dragging upward slides the clock face vertically upward
 * - Right-edge Apple StandBy vertical pagination pill indicator with up/down controls
 * - Fully unblocks child gesture interactions (photo picker, font picker)
 */
@Composable
fun BigClocksScreen(
    modifier: Modifier = Modifier,
    state: StandByUiState,
    onCycleClockFace: () -> Unit,
    onPreviousClockFace: () -> Unit = onCycleClockFace,
    onSelectClockFace: (ClockFaceStyle) -> Unit = {},
    onSelectSerifFont: (SerifFontOption) -> Unit = {},
    onPhotosSelected: (List<String>) -> Unit = {}
) {
    // 1 = downward slide transition (slide in from top -height to 0, slide out to bottom 0 to +height)
    // -1 = upward slide transition (slide in from bottom +height to 0, slide out to top 0 to -height)
    var slideDirection by remember { mutableIntStateOf(1) }

    val allStyles = remember { ClockFaceStyle.entries }
    val currentStyleIndex = allStyles.indexOf(state.clockFace).coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var totalDragY = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDragY = 0f },
                    onDragEnd = {
                        if (totalDragY > 25f) {
                            // Swiped DOWNWARD -> Slide vertically downward to change style
                            slideDirection = 1
                            onCycleClockFace()
                        } else if (totalDragY < -25f) {
                            // Swiped UPWARD -> Slide vertically upward
                            slideDirection = -1
                            onPreviousClockFace()
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDragY += dragAmount
                    }
                )
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tapping on background slides vertically downward to change style
                slideDirection = 1
                onCycleClockFace()
            }
            .testTag("big_clocks_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Vertical sliding animated content container
        AnimatedContent(
            targetState = state.clockFace,
            transitionSpec = {
                val motionSpring = spring<androidx.compose.ui.unit.IntOffset>(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
                if (slideDirection >= 0) {
                    // Vertical DOWNWARD slide:
                    // Incoming face slides in from top (-it -> 0)
                    // Outgoing face slides down to bottom (0 -> +it)
                    (slideInVertically(animationSpec = motionSpring, initialOffsetY = { -it }) + fadeIn(tween(250)))
                        .togetherWith(slideOutVertically(animationSpec = motionSpring, targetOffsetY = { it }) + fadeOut(tween(180)))
                } else {
                    // Vertical UPWARD slide:
                    // Incoming face slides in from bottom (+it -> 0)
                    // Outgoing face slides up to top (0 -> -it)
                    (slideInVertically(animationSpec = motionSpring, initialOffsetY = { it }) + fadeIn(tween(250)))
                        .togetherWith(slideOutVertically(animationSpec = motionSpring, targetOffsetY = { -it }) + fadeOut(tween(180)))
                }
            },
            label = "clockFaceVerticalSlideTransition",
            modifier = Modifier.fillMaxSize()
        ) { targetStyle ->
            when {
                targetStyle.isFullCalendar -> {
                    // Full-month Calendar Clock with giant date silhouette matching reference image_ddb10ad9.png
                    FullCalendarStandByClock(
                        currentTimeString = state.currentTimeString,
                        monthName = "${state.currentMonthFull} ${state.currentYear}",
                        currentDay = state.currentDayOfMonth,
                        isNightMode = state.isNightModeActive
                    )
                }

                targetStyle.isElegantSerif -> {
                    // Authentic Apple StandBy Burgundy Serif Clock with floating font pill matching image_b3e92e4d.png
                    ElegantBurgundySerifClock(
                        timeString = state.currentTimeString,
                        dayOfWeek = state.currentDayOfWeek,
                        dayOfMonth = state.currentDayOfMonth,
                        selectedFont = state.selectedSerifFont,
                        onSelectFont = onSelectSerifFont,
                        isNightMode = state.isNightModeActive
                    )
                }

                targetStyle.isPhotosClock -> {
                    // Authentic Apple StandBy Photo Memories Clock with slideshow matching image_8a8048df.png
                    PhotosStandByClock(
                        currentTimeString = state.currentTimeString,
                        location = if (state.weatherCity.isNotBlank() && state.weatherCity != "Locating...") state.weatherCity else "Goa",
                        dateStamp = "${state.currentDayOfMonth}-${state.currentMonthShort}-${state.currentYear}",
                        customPhotos = state.customPhotoUris,
                        photoIndex = state.photoSlideshowIndex,
                        onPhotosSelected = onPhotosSelected,
                        isNightMode = state.isNightModeActive
                    )
                }

                targetStyle.isWorldMap -> {
                    // Authentic Apple StandBy Earth Rotation World Map Clock
                    WorldMapClockFace(
                        currentTimeString = state.currentTimeString,
                        locationName = state.weatherCity,
                        userLatitude = state.userLatitude,
                        userLongitude = state.userLongitude,
                        isNightMode = state.isNightModeActive
                    )
                }

                targetStyle.isLavenderGradient -> {
                    // Authentic Apple StandBy Lavender Gradient Battery Clock
                    GradientBatteryClock(
                        timeString = state.currentTimeString,
                        batteryLevel = state.batteryLevel,
                        isCharging = state.isCharging || state.simulateCharging,
                        isNightMode = state.isNightModeActive
                    )
                }

                targetStyle.isLandscapeAnalog -> {
                    // Fullscreen Apple Landscape Analog Clock
                    AppleLandscapeAnalogClock(
                        dayOfWeek = state.currentDayOfWeek,
                        dayOfMonth = state.currentDayOfMonth,
                        isNightMode = state.isNightModeActive,
                        smoothSweep = true
                    )
                }

                targetStyle.isCondensed -> {
                    // Apple StandBy Condensed Digital
                    AppleCondensedClock(
                        timeString = state.currentTimeString,
                        dayOfWeek = state.currentDayOfWeek,
                        dayOfMonth = state.currentDayOfMonth,
                        temperature = state.weatherTemp,
                        isNightMode = state.isNightModeActive
                    )
                }

                else -> {
                    // Authentic Apple StandBy Bubbly Overlapping Clock
                    AppleBubblyClock(
                        timeString = state.currentTimeString,
                        paletteIndex = if (targetStyle == ClockFaceStyle.BUBBLY_CANDY) 1 else 0,
                        alarmString = state.alarmTimeString,
                        isNightMode = state.isNightModeActive
                    )
                }
            }
        }
    }
}
