package com.example.service

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LiveMediaState(
    val hasActiveMedia: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val coverBitmap: Bitmap? = null,
    val durationSeconds: Int = 0,
    val positionSeconds: Int = 0,
    val packageName: String = ""
)

object MediaSessionBridge {
    private val _liveMedia = MutableStateFlow(LiveMediaState())
    val liveMedia: StateFlow<LiveMediaState> = _liveMedia.asStateFlow()

    private var activeController: MediaController? = null

    fun updateMedia(state: LiveMediaState, controller: MediaController? = null) {
        if (controller != null) {
            activeController = controller
        }
        _liveMedia.value = state
    }

    fun play() {
        activeController?.transportControls?.play()
    }

    fun pause() {
        activeController?.transportControls?.pause()
    }

    fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun seekTo(seconds: Int) {
        activeController?.transportControls?.seekTo((seconds * 1000).toLong())
    }
}

class StandByNotificationListener : NotificationListenerService() {

    private var mediaSessionManager: MediaSessionManager? = null

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        scanAllActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        extractMediaFromNotification(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        val currentPackage = MediaSessionBridge.liveMedia.value.packageName
        if (currentPackage.isNotEmpty() && sbn.packageName == currentPackage) {
            // Active media player notification was dismissed
            scanAllActiveNotifications()
        }
    }

    private fun scanAllActiveNotifications() {
        try {
            val notifications = activeNotifications ?: emptyArray()
            for (sbn in notifications) {
                if (extractMediaFromNotification(sbn)) {
                    return
                }
            }

            // Fallback: Check MediaSessionManager
            val component = ComponentName(this, StandByNotificationListener::class.java)
            val controllers = mediaSessionManager?.getActiveSessions(component) ?: emptyList()
            val active = controllers.firstOrNull {
                it.playbackState?.state == PlaybackState.STATE_PLAYING
            } ?: controllers.firstOrNull()

            if (active != null) {
                attachMediaController(active)
            } else {
                MediaSessionBridge.updateMedia(LiveMediaState(hasActiveMedia = false, isPlaying = false))
            }
        } catch (_: Exception) {
        }
    }

    private fun extractMediaFromNotification(sbn: StatusBarNotification): Boolean {
        val notification = sbn.notification ?: return false
        val extras = notification.extras ?: return false

        // Check for media session token in notification extras
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(android.app.Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(android.app.Notification.EXTRA_MEDIA_SESSION) as? MediaSession.Token
        }

        if (token != null) {
            try {
                val controller = MediaController(this, token)
                attachMediaController(controller)
                return true
            } catch (_: Exception) {
            }
        }

        // Direct extraction from notification title and text
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val artist = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val album = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString() ?: ""

        // Extract album art from large icon, android.picture, or largeIcon.big
        val coverBitmap: Bitmap? = try {
            val rawLargeIcon = extras.get("android.picture")
                ?: extras.get("android.largeIcon.big")
                ?: extras.get(android.app.Notification.EXTRA_LARGE_ICON)
            when (rawLargeIcon) {
                is Bitmap -> rawLargeIcon
                is Icon -> drawableToBitmap(rawLargeIcon.loadDrawable(this))
                else -> null
            }
        } catch (_: Exception) {
            null
        }

        // Only consider it a media notification if it has title and is from music category or has media actions
        val isMediaCategory = notification.category == android.app.Notification.CATEGORY_TRANSPORT
        val hasActions = notification.actions?.any { action ->
            val titleStr = action.title?.toString()?.lowercase() ?: ""
            titleStr.contains("play") || titleStr.contains("pause") ||
                    titleStr.contains("next") || titleStr.contains("previous")
        } == true

        if (title.isNotBlank() && (isMediaCategory || hasActions || token != null)) {
            val isPlaying = notification.actions?.any {
                it.title?.toString()?.lowercase()?.contains("pause") == true
            } ?: true

            MediaSessionBridge.updateMedia(
                LiveMediaState(
                    hasActiveMedia = true,
                    isPlaying = isPlaying,
                    title = title,
                    artist = artist.ifBlank { "Now Playing" },
                    album = album,
                    coverBitmap = coverBitmap,
                    durationSeconds = 210,
                    positionSeconds = 15,
                    packageName = sbn.packageName
                )
            )
            return true
        }

        return false
    }

    private fun attachMediaController(controller: MediaController) {
        val metadata = controller.metadata
        val playbackState = controller.playbackState
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: ""
        val album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""

        val bitmap = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val durationMs = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val positionMs = playbackState?.position ?: 0L

        val durationSec = if (durationMs > 0) (durationMs / 1000).toInt() else 180
        val positionSec = (positionMs / 1000).toInt()

        if (title.isNotBlank()) {
            MediaSessionBridge.updateMedia(
                LiveMediaState(
                    hasActiveMedia = true,
                    isPlaying = isPlaying,
                    title = title,
                    artist = artist.ifBlank { "Now Playing" },
                    album = album,
                    coverBitmap = bitmap,
                    durationSeconds = durationSec,
                    positionSeconds = positionSec,
                    packageName = controller.packageName
                ),
                controller
            )

            // Register real-time callback on controller for live updates
            controller.registerCallback(object : MediaController.Callback() {
                override fun onPlaybackStateChanged(state: PlaybackState?) {
                    val playing = state?.state == PlaybackState.STATE_PLAYING
                    val pos = ((state?.position ?: 0L) / 1000).toInt()
                    val cur = MediaSessionBridge.liveMedia.value
                    MediaSessionBridge.updateMedia(
                        cur.copy(isPlaying = playing, positionSeconds = pos),
                        controller
                    )
                }

                override fun onMetadataChanged(meta: MediaMetadata?) {
                    val t = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                    val a = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                    val b = meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                        ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                    val d = ((meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L) / 1000).toInt()
                    val cur = MediaSessionBridge.liveMedia.value
                    if (t.isNotBlank()) {
                        MediaSessionBridge.updateMedia(
                            cur.copy(
                                title = t,
                                artist = a.ifBlank { cur.artist },
                                coverBitmap = b ?: cur.coverBitmap,
                                durationSeconds = if (d > 0) d else cur.durationSeconds
                            ),
                            controller
                        )
                    }
                }
            })
        }
    }

    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null
        if (drawable is BitmapDrawable) return drawable.bitmap
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 200
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
