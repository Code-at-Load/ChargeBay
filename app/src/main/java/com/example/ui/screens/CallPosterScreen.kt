package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleMint

/**
 * Authentic Apple StandBy Phone Call Poster Screen matching the user's reference image 3.
 * Supports both Incoming Call (Accept / Decline / Voicemail) and Active Ongoing Call
 * (Timer / Mute / FaceTime / Speaker / End).
 */
@Composable
fun CallPosterScreen(
    callerName: String = "Sébastien",
    callerType: String = "mobile",
    isActiveCall: Boolean = false,
    callDurationSeconds: Int = 0,
    isMuted: Boolean = false,
    isSpeakerOn: Boolean = false,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
    onVoicemail: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    val durationFormatted = String.format(
        "%02d:%02d",
        callDurationSeconds / 60,
        callDurationSeconds % 60
    )

    // Dynamic background gradient matching reference image 3 (electric blue into deep charcoal)
    val callBackground = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F8CFF),
            Color(0xFF1E5B99),
            Color(0xFF203248),
            Color(0xFF131518)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(callBackground)
            .padding(horizontal = 48.dp, vertical = 24.dp)
            .testTag("call_poster_screen")
    ) {
        // Speaker toggle button top right (active during call)
        if (isActiveCall) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isSpeakerOn) Color.White else Color.White.copy(alpha = 0.25f))
                    .clickable { onToggleSpeaker() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaker",
                    tint = if (isSpeakerOn) Color.Black else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Center Content Row (Avatar on Left, Details & Action Buttons on Right)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Circular Contact Photo Poster with white outer border
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.art_all_i_need),
                    contentDescription = callerName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(36.dp))

            // Right: Contact Name, Status, and Call Action Buttons
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // Call status header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isActiveCall) Icons.Default.Phone else Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActiveCall) durationFormatted else callerType,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Big Caller Name
                Text(
                    text = callerName,
                    color = Color.White,
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(28.dp))

                if (!isActiveCall) {
                    // Incoming Call Buttons Row (Voicemail, Decline, Accept)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Voicemail
                        CallActionButton(
                            icon = Icons.Default.Voicemail,
                            label = "Send to Voicemail",
                            backgroundColor = Color.White,
                            iconColor = Color(0xFFFF3B30),
                            onClick = onVoicemail
                        )

                        // Decline
                        CallActionButton(
                            icon = Icons.Default.CallEnd,
                            label = "Decline",
                            backgroundColor = Color(0xFFFF3B30),
                            iconColor = Color.White,
                            onClick = onDeclineCall
                        )

                        // Accept
                        CallActionButton(
                            icon = Icons.Default.Call,
                            label = "Accept",
                            backgroundColor = Color(0xFF34C759),
                            iconColor = Color.White,
                            onClick = onAcceptCall
                        )
                    }
                } else {
                    // Active Call Buttons Row (FaceTime, Mute, End)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // FaceTime
                        CallActionButton(
                            icon = Icons.Default.Videocam,
                            label = "FaceTime",
                            backgroundColor = Color.White.copy(alpha = 0.20f),
                            iconColor = Color.White,
                            onClick = {}
                        )

                        // Mute
                        CallActionButton(
                            icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            label = if (isMuted) "Muted" else "Mute",
                            backgroundColor = if (isMuted) Color.White else Color.White.copy(alpha = 0.20f),
                            iconColor = if (isMuted) Color.Black else Color.White,
                            onClick = onToggleMute
                        )

                        // End Call
                        CallActionButton(
                            icon = Icons.Default.CallEnd,
                            label = "End",
                            backgroundColor = Color(0xFFFF3B30),
                            iconColor = Color.White,
                            onClick = onDeclineCall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallActionButton(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(30.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
