package com.example.state

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ClockFaceStyle
import com.example.model.LeftWidgetType
import com.example.model.ReminderItem
import com.example.model.RightWidgetType
import com.example.model.SampleTracks
import com.example.model.StandByMode
import com.example.model.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

import com.example.model.BatteryDevice
import com.example.model.BatteryDeviceType
import com.example.model.WidgetId

enum class NightModeOption {
    AUTO,
    ALWAYS_ON,
    ALWAYS_OFF
}

fun resolveInitialLocation(): Triple<Double, Double, String> {
    val tz = TimeZone.getDefault()
    val tzId = tz.id
    val rawOffset = tz.rawOffset
    val country = Locale.getDefault().country

    // Detect India (+5:30 offset is 19800000 ms, or tzId has Kolkata/Calcutta/IST, or country IN)
    if (rawOffset == 19800000 ||
        tzId.contains("Kolkata", ignoreCase = true) ||
        tzId.contains("Calcutta", ignoreCase = true) ||
        tzId.equals("IST", ignoreCase = true) ||
        tzId.contains("India", ignoreCase = true) ||
        country.equals("IN", ignoreCase = true)
    ) {
        return Triple(28.6139, 77.2090, "New Delhi")
    }

    return when {
        tzId.contains("New_York") || rawOffset == -18000000 -> Triple(40.7128, -74.0060, "New York")
        tzId.contains("Chicago") || rawOffset == -21600000 -> Triple(41.8781, -87.6298, "Chicago")
        tzId.contains("Denver") || rawOffset == -25200000 -> Triple(39.7392, -104.9903, "Denver")
        tzId.contains("Los_Angeles") || rawOffset == -28800000 -> Triple(34.0522, -118.2437, "Los Angeles")
        tzId.contains("London") || rawOffset == 0 -> Triple(51.5074, -0.1278, "London")
        tzId.contains("Paris") || tzId.contains("Berlin") || rawOffset == 3600000 -> Triple(48.8566, 2.3522, "Paris")
        tzId.contains("Dubai") || rawOffset == 14400000 -> Triple(25.2048, 55.2708, "Dubai")
        tzId.contains("Singapore") || rawOffset == 28800000 -> Triple(1.3521, 103.8198, "Singapore")
        tzId.contains("Tokyo") || rawOffset == 32400000 -> Triple(35.6762, 139.6503, "Tokyo")
        tzId.contains("Sydney") || rawOffset == 36000000 -> Triple(-33.8688, 151.2093, "Sydney")
        else -> {
            val approxLon = (rawOffset.toDouble() / (3600.0 * 1000.0)) * 15.0
            val city = tzId.substringAfterLast("/").replace("_", " ")
            Triple(28.0, approxLon.coerceIn(-170.0, 170.0), if (city.isNotBlank()) city else "Local Time")
        }
    }
}

private val defaultLocation = resolveInitialLocation()

data class StandByUiState(
    val currentTimeString: String = "09:48",
    val currentSeconds: Int = 0,
    val currentDayOfWeek: String = "TUE",
    val currentMonthShort: String = "JUL",
    val currentMonthFull: String = "JULY",
    val currentDayOfMonth: Int = 4,
    val currentYear: Int = 2026,
    val isCharging: Boolean = false,
    val simulateCharging: Boolean = true, // Convenient default for emulator & preview
    val batteryLevel: Int = 88,
    val isLandscape: Boolean = true,
    val forceLandscapeForDemo: Boolean = true, // Ensures immediate display in web preview
    val autoExitOnUnplugOrPortrait: Boolean = true, // Enabled: automatically exits on portrait or unplug
    val ambientLux: Float = 50f,
    val nightModeOption: NightModeOption = NightModeOption.ALWAYS_OFF, // Fixed: day mode by default
    val isNightModeActive: Boolean = false,
    val currentMode: StandByMode = StandByMode.DUAL_WIDGETS,
    val leftWidgetType: LeftWidgetType = LeftWidgetType.BIG_DATE,
    val rightWidgetType: RightWidgetType = RightWidgetType.CALENDAR,
    val leftWidgetStack: List<WidgetId> = listOf(
        WidgetId.BIG_DATE,
        WidgetId.ANALOG_CLOCK,
        WidgetId.BATTERY_STATUS,
        WidgetId.WORLD_CLOCK
    ),
    val rightWidgetStack: List<WidgetId> = listOf(
        WidgetId.CALENDAR,
        WidgetId.WEATHER,
        WidgetId.MINI_MUSIC,
        WidgetId.REMINDERS
    ),
    val leftWidgetIndex: Int = 0,
    val rightWidgetIndex: Int = 0,
    val connectedDevices: List<BatteryDevice> = listOf(
        BatteryDevice("phone", "Phone", BatteryDeviceType.PHONE, 88, isCharging = true)
    ),
    val showWidgetCustomizerModal: Boolean = false,
    val clockFace: ClockFaceStyle = ClockFaceStyle.BUBBLY_BLUE,
    val isStandByEnabled: Boolean = true,
    val userLatitude: Double = defaultLocation.first,
    val userLongitude: Double = defaultLocation.second,
    val paletteToggleIndex: Int = 0,
    val customPhotoUris: List<String> = emptyList(),
    val photoSlideshowIndex: Int = 0,
    val selectedSerifFont: com.example.model.SerifFontOption = com.example.model.SerifFontOption.PLAYFAIR,
    val isFontPickerVisible: Boolean = true,
    val currentTrack: Track = SampleTracks[0],
    val hasActiveMedia: Boolean = false, // True only when system music is detected or user started demo
    val isMediaPlaying: Boolean = false, // Only true when music is actually playing
    val playbackSeconds: Int = 0,
    val volume: Float = 0.75f,
    val weatherCity: String = defaultLocation.third,
    val weatherTemp: Int = 72,
    val weatherCondition: String = "Mostly Sunny",
    val weatherHigh: Int = 76,
    val weatherLow: Int = 58,
    val alarmTimeString: String = "7:00 AM",
    val reminders: List<ReminderItem> = listOf(
        ReminderItem("1", "Set 7:00 AM alarm", true),
        ReminderItem("2", "Drink 500ml water", true),
        ReminderItem("3", "Charge Apple Watch", false),
        ReminderItem("4", "Read 15 min book", false)
    ),
    val isCallOverlayVisible: Boolean = false,
    val isCallActive: Boolean = false,
    val callerName: String = "Sébastien",
    val callerNumber: String = "mobile",
    val callDurationSeconds: Int = 0,
    val isCallMuted: Boolean = false,
    val isCallSpeakerOn: Boolean = false,
    val isTimerOverlayVisible: Boolean = false,
    val timerTotalSeconds: Int = 600,
    val timerRemainingSeconds: Int = 544,
    val isTimerPaused: Boolean = false,
    val showControlsBar: Boolean = false,
    val showSettingsModal: Boolean = false
)

class StandByViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StandByUiState())
    val uiState: StateFlow<StandByUiState> = _uiState.asStateFlow()

    // Shared flow to trigger activity finish / exit
    private val _exitAppEvent = MutableSharedFlow<Unit>()
    val exitAppEvent: SharedFlow<Unit> = _exitAppEvent.asSharedFlow()

    private var timeTickerJob: Job? = null
    private var playbackJob: Job? = null
    private var musicDetectionJob: Job? = null
    private var controlsDismissJob: Job? = null
    private var weatherJob: Job? = null
    private var callJob: Job? = null
    private var activeTimerJob: Job? = null
    private var photoTickerJob: Job? = null

    private val audioManager = application.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val lightSensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
                val lux = event.values.getOrNull(0) ?: 50f
                updateAmbientLux(lux)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    _uiState.update { it.copy(isCharging = true) }
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    _uiState.update { it.copy(isCharging = false, simulateCharging = false) }
                    checkAutoExitConditions()
                }
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isPlugged = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 85
                    _uiState.update { current ->
                        val updatedDevices = current.connectedDevices.map { dev ->
                            if (dev.deviceType == BatteryDeviceType.PHONE) {
                                dev.copy(level = batteryPct, isCharging = isPlugged)
                            } else dev
                        }
                        current.copy(
                            batteryLevel = batteryPct,
                            isCharging = isPlugged,
                            simulateCharging = if (!isPlugged) false else current.simulateCharging,
                            connectedDevices = updatedDevices
                        )
                    }
                    if (!isPlugged) {
                        checkAutoExitConditions()
                    }
                }
            }
        }
    }

    init {
        startTimeTicker()
        startPlaybackTicker()
        startMusicDetection()
        registerSensors()
        registerAudioDeviceListeners()
        updateDateTimeNow()
        scheduleControlsAutoHide()
        refreshLocationAndWeather()
        refreshConnectedDevices()
        startPhotoSlideshowTicker()
        val currentVol = try {
            val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
            val cur = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 10
            (cur.toFloat() / max.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)
        } catch (_: Exception) { 0.75f }
        _uiState.update { it.copy(volume = currentVol) }
    }

    private fun startTimeTicker() {
        timeTickerJob = viewModelScope.launch {
            while (isActive) {
                updateDateTimeNow()
                delay(1000)
            }
        }
    }

    private fun startPhotoSlideshowTicker() {
        photoTickerJob = viewModelScope.launch {
            while (isActive) {
                delay(10_000) // 10s photo slideshow rotation as requested
                val photos = _uiState.value.customPhotoUris
                if (photos.size > 1) {
                    _uiState.update { current ->
                        current.copy(
                            photoSlideshowIndex = (current.photoSlideshowIndex + 1) % photos.size
                        )
                    }
                }
            }
        }
    }

    private fun updateDateTimeNow() {
        val calendar = Calendar.getInstance()
        // Format with 12-hour without leading zero (e.g. "9:41" matching Apple StandBy reference image)
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val monthShortFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val monthFullFormat = SimpleDateFormat("MMMM", Locale.getDefault())

        val timeStr = timeFormat.format(calendar.time)
        val dayOfWeek = dayOfWeekFormat.format(calendar.time).uppercase()
        val monthShort = monthShortFormat.format(calendar.time).uppercase()
        val monthFull = monthFullFormat.format(calendar.time).uppercase()
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)
        val seconds = calendar.get(Calendar.SECOND)

        _uiState.update {
            it.copy(
                currentTimeString = timeStr,
                currentSeconds = seconds,
                currentDayOfWeek = dayOfWeek,
                currentMonthShort = monthShort,
                currentMonthFull = monthFull,
                currentDayOfMonth = dayOfMonth,
                currentYear = year
            )
        }
    }

    /**
     * Actively detects if system audio is playing via MediaSessionBridge and AudioManager.
     */
    private fun startMusicDetection() {
        musicDetectionJob = viewModelScope.launch {
            com.example.service.MediaSessionBridge.liveMedia.collect { live ->
                if (live.hasActiveMedia && live.title.isNotBlank()) {
                    _uiState.update { current ->
                        current.copy(
                            hasActiveMedia = true,
                            isMediaPlaying = live.isPlaying,
                            playbackSeconds = live.positionSeconds,
                            currentTrack = Track(
                                id = "system_live_${live.title.hashCode()}",
                                title = live.title,
                                artist = live.artist,
                                album = live.album,
                                durationSeconds = if (live.durationSeconds > 0) live.durationSeconds else 180,
                                coverBitmap = live.coverBitmap,
                                dominantColorHex = 0xFF5C442A
                            )
                        )
                    }
                } else {
                    val isSystemMusicActive = audioManager?.isMusicActive == true
                    if (!isSystemMusicActive && _uiState.value.currentTrack.id.startsWith("system_live_")) {
                        _uiState.update { it.copy(isMediaPlaying = false, hasActiveMedia = false) }
                    }
                }
            }
        }
    }

    private fun startPlaybackTicker() {
        playbackJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_uiState.value.isMediaPlaying) {
                    _uiState.update { current ->
                        val nextSec = current.playbackSeconds + 1
                        val maxDur = current.currentTrack.durationSeconds.coerceAtLeast(1)
                        if (nextSec >= maxDur) {
                            if (current.currentTrack.id.startsWith("system_live_")) {
                                current.copy(playbackSeconds = maxDur)
                            } else {
                                val nextTrackIdx = (SampleTracks.indexOf(current.currentTrack) + 1) % SampleTracks.size
                                current.copy(
                                    currentTrack = SampleTracks[nextTrackIdx],
                                    playbackSeconds = 0
                                )
                            }
                        } else {
                            current.copy(playbackSeconds = nextSec)
                        }
                    }
                }
            }
        }
    }

    private val audioDeviceCallback = object : android.media.AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out android.media.AudioDeviceInfo>?) {
            refreshConnectedDevices()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out android.media.AudioDeviceInfo>?) {
            refreshConnectedDevices()
        }
    }

    private val audioReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshConnectedDevices()
        }
    }

    private fun registerAudioDeviceListeners() {
        try {
            audioManager?.registerAudioDeviceCallback(audioDeviceCallback, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val audioFilter = IntentFilter().apply {
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.headset.profile.action.CONNECTION_STATE_CHANGED")
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
        }
        try {
            getApplication<Application>().registerReceiver(audioReceiver, audioFilter)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Actively checks connected real audio devices (Bluetooth earbuds, headphones, wired headsets).
     * If no external audio device is connected, displays phone ONLY - zero fake devices.
     */
    fun refreshConnectedDevices() {
        val phoneLevel = _uiState.value.batteryLevel
        val isPlugged = _uiState.value.isCharging || _uiState.value.simulateCharging
        val deviceList = mutableListOf<BatteryDevice>()
        deviceList.add(BatteryDevice("phone", "Phone", BatteryDeviceType.PHONE, phoneLevel, isCharging = isPlugged))

        val audioOutputs = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS) ?: emptyArray()
        for (device in audioOutputs) {
            val type = device.type
            val isBt = type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                    type == android.media.AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    type == android.media.AudioDeviceInfo.TYPE_BLE_HEADSET ||
                    type == android.media.AudioDeviceInfo.TYPE_BLE_SPEAKER
            val isWired = type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    type == android.media.AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    type == android.media.AudioDeviceInfo.TYPE_USB_HEADSET

            if (isBt || isWired) {
                val rawName = device.productName?.toString()?.trim() ?: ""
                val name = if (rawName.isNotBlank()) rawName else if (isBt) "Bluetooth Earbuds" else "Headphones"
                // Clean any trailing (L), (R), (Left), (Right) channel identifiers
                val normalizedName = name.replace(Regex("(?i)\\s*\\((left|right|l|r)\\)"), "").trim()

                // Deduplicate so Bluetooth earbuds with dual A2DP/SCO profiles or L/R channel descriptors aren't added twice
                val alreadyExists = deviceList.any { existing ->
                    existing.name.equals(name, ignoreCase = true) ||
                            existing.name.equals(normalizedName, ignoreCase = true) ||
                            (existing.deviceType == BatteryDeviceType.EARPHONES && isBt && (existing.name.contains("Earbud", ignoreCase = true) || existing.name.contains("Buds", ignoreCase = true) || existing.name.contains("Pods", ignoreCase = true)))
                }

                if (!alreadyExists) {
                    val devId = "audio_${normalizedName.lowercase().replace(Regex("[^a-z0-9]"), "_")}"
                    deviceList.add(
                        BatteryDevice(
                            id = devId,
                            name = if (normalizedName.isNotBlank()) normalizedName else name,
                            deviceType = BatteryDeviceType.EARPHONES,
                            level = 85,
                            isCharging = false,
                            isConnected = true
                        )
                    )
                }
            }
        }

        _uiState.update { it.copy(connectedDevices = deviceList) }
    }

    private fun registerSensors() {
        lightSensor?.let {
            sensorManager?.registerListener(
                lightSensorListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_CHANGED)
        }
        getApplication<Application>().registerReceiver(powerReceiver, filter)
    }

    fun onOrientationChanged(isLandscape: Boolean) {
        _uiState.update { it.copy(isLandscape = isLandscape) }
        if (!isLandscape) {
            checkAutoExitConditions()
        }
    }

    fun checkAutoExitConditions() {
        val state = _uiState.value
        if (!state.autoExitOnUnplugOrPortrait) return

        val charging = state.isCharging || state.simulateCharging
        val landscape = state.isLandscape || state.forceLandscapeForDemo

        // If unplugged OR in portrait, auto-exit
        if (!charging || !landscape) {
            viewModelScope.launch {
                _exitAppEvent.emit(Unit)
            }
        }
    }

    fun setAutoExitOnUnplugOrPortrait(enabled: Boolean) {
        _uiState.update { it.copy(autoExitOnUnplugOrPortrait = enabled) }
    }

    fun updateAmbientLux(lux: Float) {
        _uiState.update { state ->
            val isNight = when (state.nightModeOption) {
                NightModeOption.ALWAYS_ON -> true
                NightModeOption.ALWAYS_OFF -> false
                NightModeOption.AUTO -> lux in 0.05f..5.0f // Only activate on genuine low light, not missing sensor
            }
            state.copy(ambientLux = lux, isNightModeActive = isNight)
        }
    }

    fun setNightModeOption(option: NightModeOption) {
        _uiState.update { state ->
            val isNight = when (option) {
                NightModeOption.ALWAYS_ON -> true
                NightModeOption.ALWAYS_OFF -> false
                NightModeOption.AUTO -> state.ambientLux in 0.05f..5.0f
            }
            state.copy(nightModeOption = option, isNightModeActive = isNight)
        }
    }

    fun toggleSimulateCharging() {
        _uiState.update { current ->
            val next = !current.simulateCharging
            current.copy(simulateCharging = next)
        }
        checkAutoExitConditions()
    }

    fun toggleForceLandscape() {
        _uiState.update { current ->
            val next = !current.forceLandscapeForDemo
            current.copy(forceLandscapeForDemo = next)
        }
        checkAutoExitConditions()
    }

    fun setScreenOrientation(isLandscape: Boolean) {
        _uiState.update { it.copy(isLandscape = isLandscape) }
        if (!isLandscape) {
            checkAutoExitConditions()
        }
    }

    fun togglePlayPause() {
        val current = _uiState.value
        if (current.currentTrack.id.startsWith("system_live_")) {
            if (current.isMediaPlaying) {
                com.example.service.MediaSessionBridge.pause()
            } else {
                com.example.service.MediaSessionBridge.play()
            }
            _uiState.update { it.copy(isMediaPlaying = !it.isMediaPlaying) }
        } else {
            _uiState.update { it.copy(isMediaPlaying = !it.isMediaPlaying) }
        }
    }

    fun nextTrack() {
        val current = _uiState.value
        if (current.currentTrack.id.startsWith("system_live_")) {
            com.example.service.MediaSessionBridge.skipToNext()
        } else {
            _uiState.update { state ->
                val currentIndex = SampleTracks.indexOf(state.currentTrack)
                val nextIndex = (currentIndex + 1) % SampleTracks.size
                state.copy(
                    currentTrack = SampleTracks[nextIndex],
                    playbackSeconds = 0,
                    isMediaPlaying = true
                )
            }
        }
    }

    fun previousTrack() {
        val current = _uiState.value
        if (current.currentTrack.id.startsWith("system_live_")) {
            com.example.service.MediaSessionBridge.skipToPrevious()
        } else {
            _uiState.update { state ->
                if (state.playbackSeconds > 3) {
                    state.copy(playbackSeconds = 0)
                } else {
                    val currentIndex = SampleTracks.indexOf(state.currentTrack)
                    val prevIndex = if (currentIndex - 1 < 0) SampleTracks.size - 1 else currentIndex - 1
                    state.copy(
                        currentTrack = SampleTracks[prevIndex],
                        playbackSeconds = 0,
                        isMediaPlaying = true
                    )
                }
            }
        }
    }

    fun selectTrack(track: Track) {
        _uiState.update { it.copy(currentTrack = track, playbackSeconds = 0, isMediaPlaying = true) }
    }

    fun seekToSeconds(seconds: Int) {
        val current = _uiState.value
        if (current.currentTrack.id.startsWith("system_live_")) {
            com.example.service.MediaSessionBridge.seekTo(seconds)
        }
        _uiState.update { it.copy(playbackSeconds = seconds.coerceIn(0, it.currentTrack.durationSeconds)) }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _uiState.update { it.copy(volume = clamped) }
        try {
            audioManager?.let { am ->
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = (clamped * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            }
        } catch (_: Exception) {}
    }

    fun cycleLeftWidget() {
        _uiState.update { current ->
            if (current.leftWidgetStack.isEmpty()) current
            else {
                val nextIdx = (current.leftWidgetIndex + 1) % current.leftWidgetStack.size
                val nextId = current.leftWidgetStack[nextIdx]
                val mappedType = when (nextId) {
                    WidgetId.BIG_DATE -> LeftWidgetType.BIG_DATE
                    WidgetId.ANALOG_CLOCK -> LeftWidgetType.ANALOG_CLOCK
                    WidgetId.BATTERY_STATUS -> LeftWidgetType.BATTERY_STATUS
                    WidgetId.WORLD_CLOCK -> LeftWidgetType.WORLD_CLOCK
                    else -> LeftWidgetType.BIG_DATE
                }
                current.copy(leftWidgetIndex = nextIdx, leftWidgetType = mappedType)
            }
        }
    }

    fun cycleRightWidget() {
        _uiState.update { current ->
            if (current.rightWidgetStack.isEmpty()) current
            else {
                val nextIdx = (current.rightWidgetIndex + 1) % current.rightWidgetStack.size
                val nextId = current.rightWidgetStack[nextIdx]
                val mappedType = when (nextId) {
                    WidgetId.CALENDAR -> RightWidgetType.CALENDAR
                    WidgetId.WEATHER -> RightWidgetType.WEATHER
                    WidgetId.MINI_MUSIC -> RightWidgetType.MINI_MUSIC
                    WidgetId.REMINDERS -> RightWidgetType.REMINDERS
                    else -> RightWidgetType.CALENDAR
                }
                current.copy(rightWidgetIndex = nextIdx, rightWidgetType = mappedType)
            }
        }
    }

    fun setWidgetCustomizerVisible(visible: Boolean) {
        _uiState.update { it.copy(showWidgetCustomizerModal = visible) }
    }

    fun toggleLeftWidgetInStack(widgetId: WidgetId) {
        _uiState.update { current ->
            val stack = current.leftWidgetStack.toMutableList()
            if (stack.contains(widgetId)) {
                if (stack.size > 1) stack.remove(widgetId)
            } else {
                stack.add(widgetId)
            }
            val newIdx = current.leftWidgetIndex.coerceIn(0, (stack.size - 1).coerceAtLeast(0))
            current.copy(leftWidgetStack = stack, leftWidgetIndex = newIdx)
        }
    }

    fun toggleRightWidgetInStack(widgetId: WidgetId) {
        _uiState.update { current ->
            val stack = current.rightWidgetStack.toMutableList()
            if (stack.contains(widgetId)) {
                if (stack.size > 1) stack.remove(widgetId)
            } else {
                stack.add(widgetId)
            }
            val newIdx = current.rightWidgetIndex.coerceIn(0, (stack.size - 1).coerceAtLeast(0))
            current.copy(rightWidgetStack = stack, rightWidgetIndex = newIdx)
        }
    }

    fun resetDefaultWidgetStacks() {
        _uiState.update { current ->
            current.copy(
                leftWidgetStack = listOf(
                    WidgetId.BIG_DATE,
                    WidgetId.ANALOG_CLOCK,
                    WidgetId.BATTERY_STATUS,
                    WidgetId.WORLD_CLOCK
                ),
                rightWidgetStack = listOf(
                    WidgetId.CALENDAR,
                    WidgetId.WEATHER,
                    WidgetId.MINI_MUSIC,
                    WidgetId.REMINDERS
                ),
                leftWidgetIndex = 0,
                rightWidgetIndex = 0
            )
        }
    }

    fun toggleDeviceStatus(deviceId: String) {
        _uiState.update { current ->
            current.copy(
                connectedDevices = current.connectedDevices.map { dev ->
                    if (dev.id == deviceId) {
                        dev.copy(isCharging = !dev.isCharging)
                    } else dev
                }
            )
        }
    }

    fun cycleClockFace() {
        _uiState.update { current ->
            val values = ClockFaceStyle.entries
            val nextIdx = (values.indexOf(current.clockFace) + 1) % values.size
            current.copy(clockFace = values[nextIdx])
        }
    }

    fun previousClockFace() {
        _uiState.update { current ->
            val values = ClockFaceStyle.entries
            val currentIdx = values.indexOf(current.clockFace)
            val prevIdx = if (currentIdx <= 0) values.size - 1 else currentIdx - 1
            current.copy(clockFace = values[prevIdx])
        }
    }

    fun selectClockFace(style: ClockFaceStyle) {
        _uiState.update { it.copy(clockFace = style) }
    }

    fun setCustomPhotos(uris: List<String>) {
        _uiState.update {
            it.copy(
                customPhotoUris = uris,
                photoSlideshowIndex = 0
            )
        }
    }

    fun setSerifFont(font: com.example.model.SerifFontOption) {
        _uiState.update { it.copy(selectedSerifFont = font) }
    }

    fun toggleFontPickerVisible() {
        _uiState.update { it.copy(isFontPickerVisible = !it.isFontPickerVisible) }
    }

    fun toggleReminder(id: String) {
        _uiState.update { current ->
            current.copy(
                reminders = current.reminders.map { item ->
                    if (item.id == id) item.copy(isCompleted = !item.isCompleted) else item
                }
            )
        }
    }

    fun addReminder(title: String) {
        if (title.isBlank()) return
        _uiState.update { current ->
            current.copy(
                reminders = current.reminders + ReminderItem(
                    id = System.currentTimeMillis().toString(),
                    title = title.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun deleteReminder(id: String) {
        _uiState.update { current ->
            current.copy(reminders = current.reminders.filterNot { it.id == id })
        }
    }

    fun resetReminders() {
        _uiState.update { current ->
            current.copy(
                reminders = listOf(
                    ReminderItem("1", "Set morning alarm", true),
                    ReminderItem("2", "Drink 500ml water", true),
                    ReminderItem("3", "Charge Apple Watch", false),
                    ReminderItem("4", "Read 15 min book", false)
                )
            )
        }
    }

    fun refreshLocationAndWeather() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

                var bestLocation: Location? = null
                val hasFine = ContextCompat.checkSelfPermission(
                    app,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                val hasCoarse = ContextCompat.checkSelfPermission(
                    app,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (hasFine || hasCoarse) {
                    val providers = locationManager?.getProviders(true) ?: emptyList()
                    for (provider in providers) {
                        val loc = locationManager?.getLastKnownLocation(provider) ?: continue
                        if (bestLocation == null || loc.accuracy < (bestLocation?.accuracy ?: Float.MAX_VALUE)) {
                            bestLocation = loc
                        }
                    }
                }

                val (lat, lon, detectedCity) = if (bestLocation != null) {
                    val lat = bestLocation.latitude
                    val lon = bestLocation.longitude
                    var cityName = "Current Location"
                    try {
                        val geocoder = Geocoder(app, Locale.getDefault())
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            cityName = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: cityName
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    Triple(lat, lon, cityName)
                } else {
                    resolveInitialLocation()
                }

                _uiState.update { it.copy(userLatitude = lat, userLongitude = lon, weatherCity = detectedCity) }
                fetchOpenMeteoWeather(lat, lon, detectedCity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun fetchOpenMeteoWeather(lat: Double, lon: Double, cityName: String) {
        withContext(Dispatchers.IO) {
            try {
                val urlString = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min&temperature_unit=fahrenheit&timezone=auto"
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 8000
                conn.readTimeout = 8000

                if (conn.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val json = JSONObject(response)
                    val currentObj = json.getJSONObject("current")
                    val temp = currentObj.getDouble("temperature_2m").toInt()
                    val code = currentObj.getInt("weather_code")
                    val condition = weatherCodeToCondition(code)

                    var high = temp + 4
                    var low = temp - 10
                    if (json.has("daily")) {
                        val dailyObj = json.getJSONObject("daily")
                        if (dailyObj.has("temperature_2m_max")) {
                            val maxArr = dailyObj.getJSONArray("temperature_2m_max")
                            if (maxArr.length() > 0) high = maxArr.getDouble(0).toInt()
                        }
                        if (dailyObj.has("temperature_2m_min")) {
                            val minArr = dailyObj.getJSONArray("temperature_2m_min")
                            if (minArr.length() > 0) low = minArr.getDouble(0).toInt()
                        }
                    }

                    _uiState.update {
                        it.copy(
                            weatherCity = cityName,
                            weatherTemp = temp,
                            weatherCondition = condition,
                            weatherHigh = high,
                            weatherLow = low
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun weatherCodeToCondition(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Drizzle"
            56, 57 -> "Freezing Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with Hail"
            else -> "Mostly Sunny"
        }
    }

    fun triggerIncomingCall(name: String = "Sébastien", number: String = "mobile") {
        callJob?.cancel()
        _uiState.update {
            it.copy(
                isCallOverlayVisible = true,
                isCallActive = false,
                callerName = name,
                callerNumber = number,
                callDurationSeconds = 0,
                isCallMuted = false,
                isCallSpeakerOn = false
            )
        }
    }

    fun answerCall() {
        callJob?.cancel()
        _uiState.update { it.copy(isCallActive = true, callDurationSeconds = 0) }
        callJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.update { it.copy(callDurationSeconds = it.callDurationSeconds + 1) }
            }
        }
    }

    fun declineOrEndCall() {
        callJob?.cancel()
        _uiState.update { it.copy(isCallOverlayVisible = false, isCallActive = false, callDurationSeconds = 0) }
    }

    fun sendCallToVoicemail() {
        declineOrEndCall()
    }

    fun toggleCallMute() {
        _uiState.update { it.copy(isCallMuted = !it.isCallMuted) }
    }

    fun toggleCallSpeaker() {
        _uiState.update { it.copy(isCallSpeakerOn = !it.isCallSpeakerOn) }
    }

    fun startTimer(seconds: Int = 600) {
        activeTimerJob?.cancel()
        _uiState.update {
            it.copy(
                isTimerOverlayVisible = true,
                timerTotalSeconds = seconds,
                timerRemainingSeconds = seconds,
                isTimerPaused = false
            )
        }
        activeTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value
                if (!current.isTimerPaused && current.timerRemainingSeconds > 0) {
                    _uiState.update { it.copy(timerRemainingSeconds = it.timerRemainingSeconds - 1) }
                } else if (current.timerRemainingSeconds <= 0) {
                    break
                }
            }
        }
    }

    fun pauseResumeTimer() {
        _uiState.update { it.copy(isTimerPaused = !it.isTimerPaused) }
    }

    fun cancelTimer() {
        activeTimerJob?.cancel()
        _uiState.update { it.copy(isTimerOverlayVisible = false, isTimerPaused = false) }
    }

    fun dismissTimerOverlay() {
        _uiState.update { it.copy(isTimerOverlayVisible = false) }
    }

    fun onUserInteraction() {
        _uiState.update { it.copy(showControlsBar = true) }
        scheduleControlsAutoHide()
    }

    fun setSettingsModalVisible(visible: Boolean) {
        _uiState.update { it.copy(showSettingsModal = visible) }
    }

    private fun scheduleControlsAutoHide() {
        controlsDismissJob?.cancel()
        controlsDismissJob = viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(showControlsBar = false) }
        }
    }

    fun toggleClockPalette() {
        _uiState.update { it.copy(paletteToggleIndex = (it.paletteToggleIndex + 1) % 2) }
    }

    fun setStandByEnabled(enabled: Boolean) {
        _uiState.update { it.copy(isStandByEnabled = enabled) }
    }

    fun toggleStandByEnabled() {
        _uiState.update { it.copy(isStandByEnabled = !it.isStandByEnabled) }
    }

    override fun onCleared() {
        super.onCleared()
        timeTickerJob?.cancel()
        playbackJob?.cancel()
        musicDetectionJob?.cancel()
        controlsDismissJob?.cancel()
        weatherJob?.cancel()
        callJob?.cancel()
        activeTimerJob?.cancel()
        sensorManager?.unregisterListener(lightSensorListener)
        try {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Exception) {
        }
        try {
            getApplication<Application>().unregisterReceiver(audioReceiver)
        } catch (_: Exception) {
        }
        try {
            getApplication<Application>().unregisterReceiver(powerReceiver)
        } catch (_: Exception) {
        }
    }
}
