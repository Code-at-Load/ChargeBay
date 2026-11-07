package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.model.WidgetId
import com.example.state.NightModeOption
import com.example.state.StandByUiState
import com.example.ui.components.TopPillBar
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed
import kotlinx.coroutines.launch

@Composable
fun StandByRootScreen(
    state: StandByUiState,
    onCycleLeftWidget: () -> Unit,
    onCycleRightWidget: () -> Unit,
    onCycleClockFace: () -> Unit,
    onPreviousClockFace: () -> Unit = {},
    onSelectClockFace: (com.example.model.ClockFaceStyle) -> Unit = {},
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onSelectTrack: (Track) -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleReminder: (String) -> Unit,
    onAddReminder: (String) -> Unit = {},
    onDeleteReminder: (String) -> Unit = {},
    onResetReminders: () -> Unit = {},
    onToggleNightMode: () -> Unit,
    onSetNightModeOption: (NightModeOption) -> Unit,
    onToggleAutoExit: (Boolean) -> Unit,
    onToggleSimulateCharging: () -> Unit,
    onToggleForceLandscape: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissSettings: () -> Unit,
    onUserInteraction: () -> Unit,
    onRefreshWeather: () -> Unit = {},
    onStartTimer: (Int) -> Unit = {},
    onPauseResumeTimer: () -> Unit = {},
    onCancelTimer: () -> Unit = {},
    onTriggerCall: () -> Unit = {},
    onAcceptCall: () -> Unit = {},
    onDeclineCall: () -> Unit = {},
    onVoicemail: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onToggleSpeaker: () -> Unit = {},
    onOpenWidgetCustomizer: () -> Unit = {},
    onDismissWidgetCustomizer: () -> Unit = {},
    onToggleLeftWidget: (WidgetId) -> Unit = {},
    onToggleRightWidget: (WidgetId) -> Unit = {},
    onResetDefaultWidgets: () -> Unit = {},
    onToggleDeviceStatus: (String) -> Unit = {},
    onSelectSerifFont: (com.example.model.SerifFontOption) -> Unit = {},
    onPhotosSelected: (List<String>) -> Unit = {}
) {
    val context = LocalContext.current

    val totalPages = if (state.isMediaPlaying) 3 else 2
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()
    var isScrubbing by remember { mutableStateOf(false) }

    val isCharging = state.isCharging || state.simulateCharging
    val isLandscape = state.isLandscape || state.forceLandscapeForDemo
    val shouldShowStandBy = isLandscape && isCharging

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onUserInteraction() }
            .testTag("standby_root_container")
    ) {
        if (!shouldShowStandBy) {
            // Screen Saver / Charging & Orientation Setup Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (!isCharging) Icons.Default.Power else Icons.Default.ScreenRotation,
                    contentDescription = "StandBy Status",
                    tint = if (!isCharging) AppleMint else AppleBlue,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!isCharging) "Plug in your phone" else "Turn phone sideways",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "StandBy activates automatically while your phone is plugged in and charging in landscape orientation.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onToggleSimulateCharging,
                        colors = ButtonDefaults.buttonColors(containerColor = AppleBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (state.simulateCharging) "Stop Charging" else "Simulate Charging")
                    }

                    OutlinedButton(
                        onClick = onToggleForceLandscape,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (state.forceLandscapeForDemo) "Normal Rotation" else "Force Landscape", color = Color.White)
                    }
                }
            }
        } else {
            // MAIN STANDBY CONTENT HORIZONTAL PAGER
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isScrubbing,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("standby_horizontal_pager")
            ) { page ->
                when (page) {
                    0 -> DualWidgetsScreen(
                        state = state,
                        onCycleLeftWidget = onCycleLeftWidget,
                        onCycleRightWidget = onCycleRightWidget,
                        onTogglePlayPause = onTogglePlayPause,
                        onToggleReminder = onToggleReminder,
                        onAddReminder = onAddReminder,
                        onDeleteReminder = onDeleteReminder,
                        onRefreshWeather = onRefreshWeather,
                        onToggleDeviceStatus = onToggleDeviceStatus,
                        onOpenWidgetCustomizer = onOpenWidgetCustomizer,
                        onVolumeChange = onVolumeChange
                    )
                    1 -> BigClocksScreen(
                        state = state,
                        onCycleClockFace = onCycleClockFace,
                        onPreviousClockFace = onPreviousClockFace,
                        onSelectClockFace = onSelectClockFace,
                        onSelectSerifFont = onSelectSerifFont,
                        onPhotosSelected = onPhotosSelected
                    )
                    2 -> if (state.isMediaPlaying) {
                        MusicPlayerScreen(
                            state = state,
                            onTogglePlayPause = onTogglePlayPause,
                            onNextTrack = onNextTrack,
                            onPrevTrack = onPrevTrack,
                            onSelectTrack = onSelectTrack,
                            onSeek = onSeek,
                            onVolumeChange = onVolumeChange,
                            onScrubbingStateChange = { isScrubbing = it }
                        )
                    } else {
                        BigClocksScreen(
                            state = state,
                            onCycleClockFace = onCycleClockFace,
                            onPreviousClockFace = onPreviousClockFace,
                            onSelectClockFace = onSelectClockFace,
                            onSelectSerifFont = onSelectSerifFont,
                            onPhotosSelected = onPhotosSelected
                        )
                    }
                }
            }

            // Top Floating Status Pill
            TopPillBar(
                visible = state.showControlsBar && !state.isCallOverlayVisible && !state.isTimerOverlayVisible,
                batteryLevel = state.batteryLevel,
                isCharging = state.isCharging,
                simulateCharging = state.simulateCharging,
                ambientLux = state.ambientLux,
                nightModeOption = state.nightModeOption,
                isNightModeActive = state.isNightModeActive,
                onToggleNightMode = onToggleNightMode,
                onToggleSimulateCharging = onToggleSimulateCharging,
                onOpenSettings = onOpenSettings,
                onStartTimer = { onStartTimer(600) },
                onTriggerCall = onTriggerCall,
                onOpenWidgetCustomizer = onOpenWidgetCustomizer,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )

            // Bottom Horizontal Mode Pagination Indicator (hidden on full screen clocks)
            AnimatedVisibility(
                visible = state.showControlsBar && !state.isCallOverlayVisible && !state.isTimerOverlayVisible && pagerState.currentPage == 0,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalPages) {
                        val isCurrent = pagerState.currentPage == i
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) {
                                        if (state.isNightModeActive) AppleNightRed else Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.25f)
                                    }
                                )
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i)
                                    }
                                }
                        )
                    }
                }
            }
        }

        // FULLSCREEN CALL POSTER OVERLAY (Activates automatically for calls)
        AnimatedVisibility(
            visible = state.isCallOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CallPosterScreen(
                callerName = state.callerName,
                callerType = state.callerNumber,
                isActiveCall = state.isCallActive,
                callDurationSeconds = state.callDurationSeconds,
                isMuted = state.isCallMuted,
                isSpeakerOn = state.isCallSpeakerOn,
                onAcceptCall = onAcceptCall,
                onDeclineCall = onDeclineCall,
                onVoicemail = onVoicemail,
                onToggleMute = onToggleMute,
                onToggleSpeaker = onToggleSpeaker
            )
        }

        // FULLSCREEN REAL-TIME ORANGE FILLING TIMER OVERLAY
        AnimatedVisibility(
            visible = state.isTimerOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            StandByTimerScreen(
                remainingSeconds = state.timerRemainingSeconds,
                totalSeconds = state.timerTotalSeconds,
                isPaused = state.isTimerPaused,
                isNightMode = state.isNightModeActive,
                onPauseResume = onPauseResumeTimer,
                onCancel = onCancelTimer
            )
        }

        // Widget Stacks Customizer Modal
        if (state.showWidgetCustomizerModal) {
            WidgetCustomizerDialog(
                leftStack = state.leftWidgetStack,
                rightStack = state.rightWidgetStack,
                onToggleLeftWidget = onToggleLeftWidget,
                onToggleRightWidget = onToggleRightWidget,
                onResetDefaultWidgets = onResetDefaultWidgets,
                onDismiss = onDismissWidgetCustomizer
            )
        }

        // Settings Dialog Modal
        if (state.showSettingsModal) {
            SettingsDialog(
                state = state,
                onDismiss = onDismissSettings,
                onSetNightModeOption = onSetNightModeOption,
                onToggleAutoExit = onToggleAutoExit,
                onToggleSimulateCharging = onToggleSimulateCharging,
                onToggleForceLandscape = onToggleForceLandscape,
                onAddReminder = onAddReminder,
                onDeleteReminder = onDeleteReminder,
                onResetReminders = onResetReminders,
                onSelectSerifFont = onSelectSerifFont,
                onPhotosSelected = onPhotosSelected
            )
        }
    }
}
