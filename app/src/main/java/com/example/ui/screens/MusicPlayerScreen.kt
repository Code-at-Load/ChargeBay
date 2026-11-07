package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SampleTracks
import com.example.model.Track
import com.example.state.StandByUiState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed
import java.util.Locale

@Composable
fun MusicPlayerScreen(
    modifier: Modifier = Modifier,
    state: StandByUiState,
    onTogglePlayPause: () -> Unit,
    onNextTrack: () -> Unit,
    onPrevTrack: () -> Unit,
    onSelectTrack: (Track) -> Unit,
    onSeek: (Int) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onScrubbingStateChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val track = state.currentTrack
    val isMediaActive = state.isMediaPlaying || state.hasActiveMedia

    val dominantColor by animateColorAsState(
        targetValue = if (state.isNightModeActive) {
            Color(0xFF380000)
        } else {
            Color(track.dominantColorHex)
        },
        animationSpec = tween(600),
        label = "ambientBgColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (state.isNightModeActive) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "musicTextColor"
    )
    val subtextColor by animateColorAsState(
        targetValue = if (state.isNightModeActive) AppleNightRed.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f),
        animationSpec = tween(500),
        label = "musicSubtextColor"
    )
    val controlIconColor by animateColorAsState(
        targetValue = if (state.isNightModeActive) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "controlIconColor"
    )
    val playButtonBg by animateColorAsState(
        targetValue = if (state.isNightModeActive) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "playButtonBg"
    )

    val elapsedFormatted = formatTime(state.playbackSeconds)
    val remainingFormatted = "-" + formatTime((track.durationSeconds - state.playbackSeconds).coerceAtLeast(0))

    // Dynamic beat animations that shift gradient coordinates and pulse with the rhythm of music
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "musicBeats")
    val beatScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (state.isMediaPlaying) 1.045f else 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "beatScale"
    )
    val gradientShiftX by infiniteTransition.animateFloat(
        initialValue = -0.22f,
        targetValue = 0.22f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(4200, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "gradientShiftX"
    )
    val gradientShiftY by infiniteTransition.animateFloat(
        initialValue = -0.18f,
        targetValue = 0.18f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(3500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "gradientShiftY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("music_player_screen")
    ) {
        if (!isMediaActive) {
            // Idle StandBy "Not Playing" State (Apple style)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1C1E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = if (state.isNightModeActive) AppleNightRed else AppleMint,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Not Playing",
                    color = textColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Audio playing on Spotify, YouTube Music, or Apple Music will automatically show album art, titles, and controls here.",
                    color = subtextColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.width(480.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Open Android notification access settings so StandBy can read Spotify / YouTube
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        },
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Grant Notification Access", color = Color.White)
                    }

                    // Play demo song button for instant testing
                    Button(
                        onClick = {
                            onSelectTrack(SampleTracks[0])
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppleMint),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play Demo",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play Demo Track", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Live Active Media Player with Blurred Dynamic Backdrop (image_e4692b65.png)

            // 1. Blurred Album Art Background
            if (track.coverBitmap != null) {
                Image(
                    bitmap = track.coverBitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(65.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = track.coverRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(65.dp)
                )
            }

            // Dynamic shifting mesh gradient that pulses with the beat
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w * (0.50f + gradientShiftX)
                val centerY = h * (0.45f + gradientShiftY)
                val radius = (minOf(w, h) * 0.95f) * beatScale

                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            dominantColor.copy(alpha = if (state.isNightModeActive) 0.55f else 0.85f),
                            dominantColor.copy(alpha = 0.40f),
                            Color.Black.copy(alpha = 0.88f)
                        ),
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY),
                        radius = radius
                    )
                )
            }

            // Main Content Row: Left Album Art + Right Controls
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Album Cover Card with beat pulse and Apple drop shadow
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.88f)
                        .aspectRatio(1f)
                        .graphicsLayer(
                            scaleX = if (state.isMediaPlaying) beatScale else 1f,
                            scaleY = if (state.isMediaPlaying) beatScale else 1f
                        )
                        .shadow(elevation = 28.dp, shape = RoundedCornerShape(22.dp))
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.DarkGray)
                ) {
                    if (track.coverBitmap != null) {
                        Image(
                            bitmap = track.coverBitmap.asImageBitmap(),
                            contentDescription = track.album,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            painter = painterResource(id = track.coverRes),
                            contentDescription = track.album,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Right: Info, Transport Controls, and Progress Scrubber
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
                ) {
                    // Track Title & Artist
                    Text(
                        text = track.title,
                        color = textColor,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = track.artist,
                        color = subtextColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Transport Controls (Previous, Play/Pause, Next)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPrevTrack,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("music_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = controlIconColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        // Big Circular Play / Pause Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(playButtonBg)
                                .clickable { onTogglePlayPause() }
                                .testTag("music_play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (state.isMediaPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isMediaPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        IconButton(
                            onClick = onNextTrack,
                            modifier = Modifier
                                .size(52.dp)
                                .testTag("music_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = controlIconColor,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Isolated Realtime Progress Scrubber Bar that consumes pointer down & drag to prevent HorizontalPager conflict
                    IsolatedMusicScrubber(
                        currentSeconds = state.playbackSeconds,
                        durationSeconds = track.durationSeconds,
                        accentColor = playButtonBg,
                        onSeek = onSeek,
                        onScrubbingStateChange = onScrubbingStateChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = elapsedFormatted,
                            color = subtextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = remainingFormatted,
                            color = subtextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Apple Music style scrubber that immediately consumes pointer events on press and drag,
 * ensuring parent HorizontalPager never intercepts horizontal scrubber gestures.
 */
@Composable
fun IsolatedMusicScrubber(
    currentSeconds: Int,
    durationSeconds: Int,
    accentColor: Color,
    onSeek: (Int) -> Unit,
    onScrubbingStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDur = durationSeconds.toFloat().coerceAtLeast(1f)
    var isDragging by remember { androidx.compose.runtime.mutableStateOf(false) }
    var dragFraction by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var barWidthPx by remember { androidx.compose.runtime.mutableFloatStateOf(1f) }

    val activeFraction = if (isDragging) dragFraction else (currentSeconds / totalDur).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(totalDur) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isDragging = true
                    onScrubbingStateChange(true)
                    val initialFraction = (down.position.x / barWidthPx).coerceIn(0f, 1f)
                    dragFraction = initialFraction
                    onSeek((initialFraction * totalDur).toInt())

                    val pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        change.consume()
                        val newFraction = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                        dragFraction = newFraction
                        onSeek((newFraction * totalDur).toInt())
                    }
                    isDragging = false
                    onScrubbingStateChange(false)
                }
            }
            .testTag("music_scrubber"),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
        ) {
            val centerY = size.height / 2f
            val w = size.width
            val trackHeight = if (isDragging) 6.dp.toPx() else 4.dp.toPx()
            val thumbRadius = if (isDragging) 8.5.dp.toPx() else 6.5.dp.toPx()

            // Inactive Background Track
            drawLine(
                color = Color.White.copy(alpha = 0.22f),
                start = androidx.compose.ui.geometry.Offset(0f, centerY),
                end = androidx.compose.ui.geometry.Offset(w, centerY),
                strokeWidth = trackHeight,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Active Elapsed Track
            val activeX = (w * activeFraction).coerceIn(0f, w)
            if (activeX > 0f) {
                drawLine(
                    color = accentColor,
                    start = androidx.compose.ui.geometry.Offset(0f, centerY),
                    end = androidx.compose.ui.geometry.Offset(activeX, centerY),
                    strokeWidth = trackHeight,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }

            // Glowing Outer Thumb
            drawCircle(
                color = accentColor,
                radius = thumbRadius,
                center = androidx.compose.ui.geometry.Offset(activeX, centerY)
            )

            // Inner Contrast Core
            drawCircle(
                color = Color.White,
                radius = thumbRadius * 0.45f,
                center = androidx.compose.ui.geometry.Offset(activeX, centerY)
            )
        }
    }
}

private fun formatTime(seconds: Int): String {
    val sec = seconds.coerceAtLeast(0)
    val mins = sec / 60
    val remSec = sec % 60
    return String.format(Locale.getDefault(), "%d:%02d", mins, remSec)
}
