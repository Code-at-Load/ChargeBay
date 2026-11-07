package com.example.model

import androidx.annotation.DrawableRes
import com.example.R

enum class StandByMode(val title: String) {
    DUAL_WIDGETS("Widgets"),
    BIG_CLOCKS("Clocks"),
    MUSIC_PLAYER("Now Playing")
}

enum class BatteryDeviceType {
    PHONE,
    EARPHONES,
    WATCH,
    ACCESSORY
}

data class BatteryDevice(
    val id: String,
    val name: String,
    val deviceType: BatteryDeviceType,
    val level: Int,
    val isCharging: Boolean = false,
    val isConnected: Boolean = true
)

enum class WidgetId(
    val title: String,
    val category: String,
    val description: String
) {
    BIG_DATE("Date & Day", "Time", "Large clean date, weekday, and year"),
    ANALOG_CLOCK("Analog Clock", "Time", "Minimalist bedside ticking clock"),
    WORLD_CLOCK("World Time", "Time", "Dual timezone clock Cupertino & Tokyo"),
    BATTERY_STATUS("Battery & Devices", "System", "Real-time battery for phone & connected earbuds"),
    CALENDAR("Calendar Month", "Productivity", "Interactive month calendar view"),
    WEATHER("Live Weather", "Weather", "Current GPS location, temperature & condition"),
    MINI_MUSIC("Now Playing", "Media", "Playback controls and track album art"),
    REMINDERS("Bedside Checklist", "Productivity", "Interactive nightstand to-do list")
}

enum class LeftWidgetType(val displayName: String) {
    BIG_DATE("Date & Day"),
    ANALOG_CLOCK("Analog Clock"),
    WORLD_CLOCK("World Time"),
    BATTERY_STATUS("Battery & Stats")
}

enum class RightWidgetType(val displayName: String) {
    CALENDAR("Calendar Month"),
    WEATHER("Live Weather"),
    MINI_MUSIC("Now Playing"),
    REMINDERS("Bedside Checklist")
}

enum class ClockFaceStyle(
    val title: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long = 0xFFFFA399,
    val isLandscapeAnalog: Boolean = false,
    val isCondensed: Boolean = false,
    val isBubbly: Boolean = true,
    val isWorldMap: Boolean = false,
    val isLavenderGradient: Boolean = false,
    val isElegantSerif: Boolean = false,
    val isFullCalendar: Boolean = false,
    val isPhotosClock: Boolean = false
) {
    BUBBLY_BLUE("Bubbly Blue (iOS 17)", 0xFF007AFF, 0xFF38ACFF, isBubbly = true),
    BUBBLY_CANDY("Bubbly Candy (Pink/Purple)", 0xFFED5B9D, 0xFF988BFA, isBubbly = true),
    WORLD_MAP("World Map Dot Matrix", 0xFFFF9500, 0xFFFFFFFF, isBubbly = false, isWorldMap = true),
    ELEGANT_SERIF("Elegant Burgundy Serif", 0xFFFCD0DF, 0xFF4A1525, isBubbly = false, isElegantSerif = true),
    PHOTOS_CLOCK("Memories Photo Clock", 0xFFFFFFFF, 0xFF000000, isBubbly = false, isPhotosClock = true),
    FULL_CALENDAR("Monthly Calendar StandBy", 0xFFFF3B30, 0xFF007AFF, isBubbly = false, isFullCalendar = true),
    LANDSCAPE_ANALOG("Apple StandBy Analog", 0xFFFFFFFF, 0xFFFFD60A, isLandscapeAnalog = true, isBubbly = false),
    CONDENSED_DIGITAL("Apple Condensed", 0xFF6AA7FC, 0xFFFFFFFF, isCondensed = true, isBubbly = false),
    LAVENDER_GRADIENT("Lavender Gradient", 0xFFE085FF, 0xFFF5B8FF, isBubbly = false, isLavenderGradient = true)
}

enum class SerifFontOption(val displayName: String) {
    PLAYFAIR("Didot Serif"),
    DM_SERIF("Classic Serif"),
    SANS("Modern Sans"),
    ROUNDED("Soft Rounded")
}

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    @DrawableRes val coverRes: Int = R.drawable.art_all_i_need,
    val dominantColorHex: Long = 0xFF8A5A2B,
    val coverBitmap: android.graphics.Bitmap? = null
)

val SampleTracks = listOf(
    Track(
        id = "1",
        title = "All I Need",
        artist = "LP Giobbi",
        album = "Light Places",
        durationSeconds = 215,
        coverRes = R.drawable.art_all_i_need,
        dominantColorHex = 0xFF8A5A2B
    ),
    Track(
        id = "2",
        title = "I Ain't Worried",
        artist = "OneRepublic",
        album = "Top Gun: Maverick",
        durationSeconds = 148,
        coverRes = R.drawable.art_top_gun,
        dominantColorHex = 0xFF5C442A
    ),
    Track(
        id = "3",
        title = "Midnight Reverie",
        artist = "Bedside Lo-Fi",
        album = "Sleep Harmonies",
        durationSeconds = 184,
        coverRes = R.drawable.art_ambient,
        dominantColorHex = 0xFF2D3250
    )
)

data class ReminderItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)
