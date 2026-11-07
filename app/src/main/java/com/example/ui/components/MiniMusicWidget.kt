package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.AppleMint
import com.example.ui.theme.AppleNightRed

/**
 * Authentic Apple StandBy Mini Music Widget:
 * - Direct one-tap Play/Pause button right on the StandBy screen
 * - Realtime interactive vertical volume slider capsule at the right marking
 * - Up & down sliding gesture controls volume with live fill percentage and volume icon
 * - Touch isolation preventing accidental widget stack cycling
 */
@Composable
fun MiniMusicWidget(
    modifier: Modifier = Modifier,
    track: Track,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    volume: Float = 0.75f,
    onVolumeChange: (Float) -> Unit = {},
    isNightMode: Boolean = false
) {
    val textColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(500),
        label = "miniMusicText"
    )
    val subtextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.65f),
        animationSpec = tween(500),
        label = "miniMusicSubtext"
    )
    val pillBgColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed.copy(alpha = 0.25f) else Color(0xFF2C2C2E),
        animationSpec = tween(500),
        label = "miniMusicPillBg"
    )
    val playButtonContainer by animateColorAsState(
        targetValue = if (isPlaying) pillBgColor else (if (isNightMode) AppleNightRed else AppleMint),
        animationSpec = tween(400),
        label = "playButtonContainer"
    )
    val playButtonTextColor by animateColorAsState(
        targetValue = if (isPlaying) textColor else Color.Black,
        animationSpec = tween(400),
        label = "playButtonTextColor"
    )

    var isDraggingVolume by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF141416).copy(alpha = if (isNightMode) 0.3f else 0.75f))
            .padding(14.dp)
            .testTag("mini_music_widget")
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Album Artwork with clickable Play/Pause affordance
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.DarkGray)
                    .clickable { onTogglePlayPause() }
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

                // Audio visualizer badge when playing, or music note badge when paused
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .align(Alignment.TopStart),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                        contentDescription = if (isPlaying) "Playing" else "Audio",
                        tint = if (isNightMode) AppleNightRed else (if (isPlaying) AppleMint else Color.White),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // 2. Track Details & Direct StandBy Play/Pause Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPlaying) "NOW PLAYING" else "STANDBY MUSIC",
                        color = if (isNightMode) AppleNightRed.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Music",
                        tint = if (isNightMode) AppleNightRed.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = track.title,
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = track.artist,
                        color = subtextColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Prominent Apple-style Direct Play / Pause Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(playButtonContainer)
                        .clickable { onTogglePlayPause() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("mini_music_play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = playButtonTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "Pause" else "Play Music",
                            color = playButtonTextColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 3. Interactive Vertical Volume Slider Capsule on Right Edge (matching user screenshot marking)
            var sliderHeightPx by remember { mutableFloatStateOf(1f) }
            val clampedVol = volume.coerceIn(0f, 1f)

            Box(
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                // Interactive volume capsule
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E).copy(alpha = 0.9f))
                        .onSizeChanged { sliderHeightPx = it.height.toFloat().coerceAtLeast(1f) }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                isDraggingVolume = true
                                val newVol = (1f - (down.position.y / sliderHeightPx)).coerceIn(0f, 1f)
                                onVolumeChange(newVol)

                                val pointerId = down.id
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                                    if (!change.pressed) {
                                        change.consume()
                                        break
                                    }
                                    change.consume()
                                    val draggedVol = (1f - (change.position.y / sliderHeightPx)).coerceIn(0f, 1f)
                                    onVolumeChange(draggedVol)
                                }
                                isDraggingVolume = false
                            }
                        }
                        .testTag("mini_music_vertical_volume_slider"),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Active filled volume bar from bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(clampedVol)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNightMode) AppleNightRed else Color.White)
                    )

                    // Volume speaker icon inside capsule at bottom
                    val speakerIcon = when {
                        clampedVol <= 0.02f -> Icons.Default.VolumeMute
                        clampedVol < 0.5f -> Icons.Default.VolumeDown
                        else -> Icons.Default.VolumeUp
                    }
                    val iconTint = if (clampedVol > 0.25f) {
                        Color.Black
                    } else {
                        if (isNightMode) AppleNightRed else Color.White
                    }

                    Icon(
                        imageVector = speakerIcon,
                        contentDescription = "Volume ${(clampedVol * 100).toInt()}%",
                        tint = iconTint,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(14.dp)
                    )
                }

                // Floating live volume percentage indicator while sliding
                androidx.compose.animation.AnimatedVisibility(
                    visible = isDraggingVolume,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(end = 30.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${(clampedVol * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
