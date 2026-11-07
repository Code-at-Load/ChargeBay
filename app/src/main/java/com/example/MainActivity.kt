package com.example

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.view.MotionEvent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.state.NightModeOption
import com.example.state.StandByViewModel
import com.example.ui.screens.StandByRootScreen
import com.example.ui.theme.StandByTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private val viewModel: StandByViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure window for full-bleed edge-to-edge across camera cutout / notch
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        @Suppress("DEPRECATION")
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // Only keep screen awake while actively docked in landscape
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                val shouldKeepAwake = (state.isCharging || state.simulateCharging) && (state.isLandscape || state.forceLandscapeForDemo)
                if (shouldKeepAwake) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }

        // Allow display on lock screen and turning screen on when charging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        // Exit immediately if in portrait orientation (User requirement: StandBy only runs in landscape)
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish()
            return
        }

        // Hide status bar and navigation bar in sticky immersive mode
        hideSystemBars()

        // Attach listener so status bar stays hidden when system attempts to re-apply insets
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.statusBars()) ||
                insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            ) {
                hideSystemBars()
            }
            ViewCompat.onApplyWindowInsets(view, insets)
        }

        updateOrientation()

        // Listen for auto-exit event (unplugged or stood up into portrait)
        lifecycleScope.launch {
            viewModel.exitAppEvent.collectLatest {
                finish()
            }
        }

        setContent {
            StandByTheme {
                val context = LocalContext.current
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshLocationAndWeather()
                }

                LaunchedEffect(Unit) {
                    val hasFine = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    val hasCoarse = ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    if (!hasFine && !hasCoarse) {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        viewModel.refreshLocationAndWeather()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val state by viewModel.uiState.collectAsState()

                    StandByRootScreen(
                        state = state,
                        onCycleLeftWidget = { viewModel.cycleLeftWidget() },
                        onCycleRightWidget = { viewModel.cycleRightWidget() },
                        onCycleClockFace = { viewModel.cycleClockFace() },
                        onPreviousClockFace = { viewModel.previousClockFace() },
                        onSelectClockFace = { viewModel.selectClockFace(it) },
                        onTogglePlayPause = { viewModel.togglePlayPause() },
                        onNextTrack = { viewModel.nextTrack() },
                        onPrevTrack = { viewModel.previousTrack() },
                        onSelectTrack = { viewModel.selectTrack(it) },
                        onSeek = { viewModel.seekToSeconds(it) },
                        onVolumeChange = { viewModel.setVolume(it) },
                        onToggleReminder = { viewModel.toggleReminder(it) },
                        onAddReminder = { viewModel.addReminder(it) },
                        onDeleteReminder = { viewModel.deleteReminder(it) },
                        onResetReminders = { viewModel.resetReminders() },
                        onToggleNightMode = {
                            val nextOption = if (state.isNightModeActive) {
                                NightModeOption.ALWAYS_OFF
                            } else {
                                NightModeOption.ALWAYS_ON
                            }
                            viewModel.setNightModeOption(nextOption)
                        },
                        onSetNightModeOption = { viewModel.setNightModeOption(it) },
                        onToggleAutoExit = { viewModel.setAutoExitOnUnplugOrPortrait(it) },
                        onToggleSimulateCharging = { viewModel.toggleSimulateCharging() },
                        onToggleForceLandscape = { viewModel.toggleForceLandscape() },
                        onOpenSettings = { viewModel.setSettingsModalVisible(true) },
                        onDismissSettings = { viewModel.setSettingsModalVisible(false) },
                        onUserInteraction = { viewModel.onUserInteraction() },
                        onRefreshWeather = { viewModel.refreshLocationAndWeather() },
                        onStartTimer = { viewModel.startTimer(it) },
                        onPauseResumeTimer = { viewModel.pauseResumeTimer() },
                        onCancelTimer = { viewModel.cancelTimer() },
                        onTriggerCall = { viewModel.triggerIncomingCall() },
                        onAcceptCall = { viewModel.answerCall() },
                        onDeclineCall = { viewModel.declineOrEndCall() },
                        onVoicemail = { viewModel.sendCallToVoicemail() },
                        onToggleMute = { viewModel.toggleCallMute() },
                        onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                        onOpenWidgetCustomizer = { viewModel.setWidgetCustomizerVisible(true) },
                        onDismissWidgetCustomizer = { viewModel.setWidgetCustomizerVisible(false) },
                        onToggleLeftWidget = { viewModel.toggleLeftWidgetInStack(it) },
                        onToggleRightWidget = { viewModel.toggleRightWidgetInStack(it) },
                        onResetDefaultWidgets = { viewModel.resetDefaultWidgetStacks() },
                        onToggleDeviceStatus = { viewModel.toggleDeviceStatus(it) },
                        onSelectSerifFont = { viewModel.setSerifFont(it) },
                        onPhotosSelected = { viewModel.setCustomPhotos(it) }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish()
            return
        }
        hideSystemBars()
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            finish()
            return
        }
        updateOrientation()
        hideSystemBars()
    }

    private fun updateOrientation() {
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        viewModel.onOrientationChanged(isLandscape)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            hideSystemBars()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}
