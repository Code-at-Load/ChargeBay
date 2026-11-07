package com.example.service

import android.os.Build
import android.os.Bundle
import android.service.dreams.DreamService
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.state.NightModeOption
import com.example.state.StandByViewModel
import com.example.ui.screens.StandByRootScreen
import com.example.ui.theme.StandByTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Native Android Screen Saver (Daydream) service:
 * - Runs StandBy directly within the DreamService window without bouncing to MainActivity
 * - Properly manages lifecycle and window flags so the phone can sleep normally
 * - Allows full touch interactivity (swiping widgets, changing clocks, controlling volume)
 * - Cleanly releases screen on detach so the device enters sleep state as intended
 */
class StandByDreamService : DreamService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val customViewModelStore = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private var viewModel: StandByViewModel? = null

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = customViewModelStore
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        isFullscreen = true
        isInteractive = true
        isScreenBright = true
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isFullscreen = true
        isInteractive = true
        isScreenBright = true

        val decorView = window?.decorView ?: return
        decorView.setViewTreeLifecycleOwner(this)
        decorView.setViewTreeViewModelStoreOwner(this)
        decorView.setViewTreeSavedStateRegistryOwner(this)

        hideSystemBars()

        ViewCompat.setOnApplyWindowInsetsListener(decorView) { view, insets ->
            if (insets.isVisible(WindowInsetsCompat.Type.statusBars()) ||
                insets.isVisible(WindowInsetsCompat.Type.navigationBars())
            ) {
                hideSystemBars()
            }
            ViewCompat.onApplyWindowInsets(view, insets)
        }

        val vm = StandByViewModel(application)
        viewModel = vm

        lifecycleScope.launch {
            vm.exitAppEvent.collectLatest {
                finish()
            }
        }

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
            setContent {
                StandByTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = Color.Black
                    ) {
                        val state by vm.uiState.collectAsState()

                        StandByRootScreen(
                            state = state,
                            onCycleLeftWidget = { vm.cycleLeftWidget() },
                            onCycleRightWidget = { vm.cycleRightWidget() },
                            onCycleClockFace = { vm.cycleClockFace() },
                            onPreviousClockFace = { vm.previousClockFace() },
                            onSelectClockFace = { vm.selectClockFace(it) },
                            onTogglePlayPause = { vm.togglePlayPause() },
                            onNextTrack = { vm.nextTrack() },
                            onPrevTrack = { vm.previousTrack() },
                            onSelectTrack = { vm.selectTrack(it) },
                            onSeek = { vm.seekToSeconds(it) },
                            onVolumeChange = { vm.setVolume(it) },
                            onToggleReminder = { vm.toggleReminder(it) },
                            onAddReminder = { vm.addReminder(it) },
                            onDeleteReminder = { vm.deleteReminder(it) },
                            onResetReminders = { vm.resetReminders() },
                            onToggleNightMode = {
                                val nextOption = if (state.isNightModeActive) {
                                    NightModeOption.ALWAYS_OFF
                                } else {
                                    NightModeOption.ALWAYS_ON
                                }
                                vm.setNightModeOption(nextOption)
                            },
                            onSetNightModeOption = { vm.setNightModeOption(it) },
                            onToggleAutoExit = { vm.setAutoExitOnUnplugOrPortrait(it) },
                            onToggleSimulateCharging = { vm.toggleSimulateCharging() },
                            onToggleForceLandscape = { vm.toggleForceLandscape() },
                            onOpenSettings = { vm.setSettingsModalVisible(true) },
                            onDismissSettings = { vm.setSettingsModalVisible(false) },
                            onUserInteraction = {
                                vm.onUserInteraction()
                                hideSystemBars()
                            },
                            onRefreshWeather = { vm.refreshLocationAndWeather() },
                            onStartTimer = { vm.startTimer(it) },
                            onPauseResumeTimer = { vm.pauseResumeTimer() },
                            onCancelTimer = { vm.cancelTimer() },
                            onTriggerCall = { vm.triggerIncomingCall() },
                            onAcceptCall = { vm.answerCall() },
                            onDeclineCall = { vm.declineOrEndCall() },
                            onVoicemail = { vm.sendCallToVoicemail() },
                            onToggleMute = { vm.toggleCallMute() },
                            onToggleSpeaker = { vm.toggleCallSpeaker() },
                            onOpenWidgetCustomizer = { vm.setWidgetCustomizerVisible(true) },
                            onDismissWidgetCustomizer = { vm.setWidgetCustomizerVisible(false) },
                            onToggleLeftWidget = { vm.toggleLeftWidgetInStack(it) },
                            onToggleRightWidget = { vm.toggleRightWidgetInStack(it) },
                            onResetDefaultWidgets = { vm.resetDefaultWidgetStacks() },
                            onToggleDeviceStatus = { vm.toggleDeviceStatus(it) },
                            onSelectSerifFont = { vm.setSerifFont(it) },
                            onPhotosSelected = { vm.setCustomPhotos(it) }
                        )
                    }
                }
            }
        }

        setContentView(composeView)
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            finish()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            hideSystemBars()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        hideSystemBars()
    }

    private fun hideSystemBars() {
        isFullscreen = true
        window?.let { win ->
            val decorView = win.decorView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                win.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                win.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            @Suppress("DEPRECATION")
            win.addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            WindowCompat.setDecorFitsSystemWindows(win, false)
            val controller = WindowCompat.getInsetsController(win, decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())

            @Suppress("DEPRECATION")
            decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    override fun onDreamingStopped() {
        super.onDreamingStopped()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        finish()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        customViewModelStore.clear()
        viewModel = null
    }
}
