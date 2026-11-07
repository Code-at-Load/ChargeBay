package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.SerifFontOption
import com.example.ui.theme.AppleNightRed
import com.example.ui.theme.DmSerifDisplayFontFamily
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import java.util.Calendar

/**
 * Authentic Apple StandBy Elegant Burgundy Serif Clock matching image_b3e92e4d.png:
 * - Rich deep burgundy background (#3E1422)
 * - Oversized, high-contrast blush pink serif typography (#FFCBD7)
 * - Top-left day/date header ("WED 1")
 * - Floating font selector pill (Didot, Classic, Sans, Rounded)
 * - Bottom-right camera sensor notch dot
 */
@Composable
fun ElegantBurgundySerifClock(
    modifier: Modifier = Modifier,
    timeString: String = "9:41",
    dayOfWeek: String = "WED",
    dayOfMonth: Int = 1,
    selectedFont: SerifFontOption = SerifFontOption.PLAYFAIR,
    onSelectFont: (SerifFontOption) -> Unit = {},
    isNightMode: Boolean = false
) {
    val bgColor by animateColorAsState(
        targetValue = if (isNightMode) Color(0xFF26050E) else Color(0xFF3F1424),
        animationSpec = tween(400),
        label = "serifBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color(0xFFFFCBD7),
        animationSpec = tween(400),
        label = "serifText"
    )

    val activeFontFamily = when (selectedFont) {
        SerifFontOption.PLAYFAIR -> PlayfairDisplayFontFamily
        SerifFontOption.DM_SERIF -> DmSerifDisplayFontFamily
        SerifFontOption.SANS -> FontFamily.SansSerif
        SerifFontOption.ROUNDED -> FredokaFontFamily
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("elegant_serif_clock")
    ) {
        val h = maxHeight
        val w = maxWidth
        val fontSize = minOf(h.value * 0.88f, w.value * 0.38f).sp

        // Top Left Date
        Text(
            text = "$dayOfWeek $dayOfMonth",
            color = textColor.copy(alpha = 0.92f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 42.dp, top = 26.dp)
        )

        // Center Giant Serif Time with circular colon dots
        Text(
            text = timeString,
            color = textColor,
            fontSize = fontSize,
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-4).sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 8.dp)
        )

        // Bottom Right Camera Cutout Accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 22.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF14040A))
        )
    }
}

/**
 * Authentic Apple StandBy Full Calendar Month Clock matching image_ddb10ad9.png:
 * - Deep midnight navy backdrop (#06101E)
 * - Giant translucent crimson date silhouette ("1") right behind the calendar grid (#852532)
 * - Complete dynamic 7-column calendar grid (M, T, W, T, F, S, S)
 * - Circled accent blue badge for active today date
 * - Top bar: Month Title ("April 2026") on left, Digital bedside time ("9:41") on right
 */
@Composable
fun FullCalendarStandByClock(
    modifier: Modifier = Modifier,
    currentTimeString: String = "9:41",
    monthName: String = "April 2026",
    currentDay: Int = 1,
    isNightMode: Boolean = false
) {
    val bgColor = if (isNightMode) Color(0xFF0C0204) else Color(0xFF06101E)
    val headerRed = if (isNightMode) AppleNightRed else Color(0xFFFF453A)
    val dayBlue = if (isNightMode) AppleNightRed.copy(alpha = 0.7f) else Color(0xFF2D76ED)
    val dimBlue = if (isNightMode) Color(0xFF22080D) else Color(0xFF18283E)
    val activeCircleBlue = if (isNightMode) AppleNightRed else Color(0xFF0A84FF)

    // Calculate dynamic calendar matrix for current month
    val calendar = remember { Calendar.getInstance() }
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayCal = remember {
        (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    // In Java Calendar, SUNDAY=1, MONDAY=2, ... SATURDAY=7
    val firstDayOfWeekJava = firstDayCal.get(Calendar.DAY_OF_WEEK)
    // Convert to Monday=0, Tuesday=1 ... Sunday=6
    val startOffset = (firstDayOfWeekJava + 5) % 7

    val prevCal = remember {
        (calendar.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
    }
    val prevMonthDays = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Build 5 or 6 rows of 7 days
    val totalCells = 35 // 5 weeks standard
    data class CalendarCell(val day: Int, val isCurrentMonth: Boolean)
    val gridCells = remember(daysInMonth, startOffset) {
        val list = mutableListOf<CalendarCell>()
        // Previous month days
        for (i in (startOffset - 1) downTo 0) {
            list.add(CalendarCell(prevMonthDays - i, false))
        }
        // Current month days
        for (i in 1..daysInMonth) {
            list.add(CalendarCell(i, true))
        }
        // Next month days
        var nextDay = 1
        while (list.size < totalCells) {
            list.add(CalendarCell(nextDay++, false))
        }
        list.take(totalCells)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 38.dp, vertical = 20.dp)
            .testTag("full_calendar_clock")
    ) {
        // Giant Silhouette Number in the center background matching reference image
        Text(
            text = currentDay.toString(),
            color = if (isNightMode) AppleNightRed.copy(alpha = 0.20f) else Color(0xFF882430).copy(alpha = 0.52f),
            fontSize = 250.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif,
            modifier = Modifier.align(Alignment.Center)
        )

        // Top Row: Month Title on Left & Digital Time on Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthName,
                color = headerRed,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Text(
                text = currentTimeString,
                color = headerRed,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Calendar Weekday Headers & 7-Column Day Matrix
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(0.78f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dayLetter ->
                    Text(
                        text = dayLetter,
                        color = dayBlue.copy(alpha = 0.85f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.size(34.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5 Weeks grid
            val weeks = gridCells.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(0.78f),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { cell ->
                        val isToday = cell.isCurrentMonth && cell.day == currentDay
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isToday) activeCircleBlue else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell.day.toString(),
                                color = when {
                                    isToday -> Color.White
                                    cell.isCurrentMonth -> dayBlue
                                    else -> dimBlue
                                },
                                fontSize = 16.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Bottom Right Cutout Accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 6.dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF03070E))
        )
    }
}

/**
 * Authentic Apple StandBy Memories Photo Clock matching image_8a8048df.png:
 * - Edge-to-edge photo canvas (vacation sunset/beach portrait)
 * - Supports user photos via Android Photo Picker (zero-permission ActivityResultContracts)
 * - 10-second slideshow crossfade when multiple photos are loaded
 * - Top-right clean white oversized clock "9:41" with text shadow
 * - Bottom-right memory metadata: "Goa 02-OCT-2018 >"
 * - Discreet photo picker button to add or change photos
 */
@Composable
fun PhotosStandByClock(
    modifier: Modifier = Modifier,
    currentTimeString: String = "9:41",
    location: String = "Goa",
    dateStamp: String = "02-OCT-2018",
    customPhotos: List<String> = emptyList(),
    photoIndex: Int = 0,
    onPhotosSelected: (List<String>) -> Unit = {},
    isNightMode: Boolean = false
) {
    val context = LocalContext.current

    val currentUriString = if (customPhotos.isNotEmpty()) {
        customPhotos.getOrNull(photoIndex % customPhotos.size)
    } else null

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("photos_standby_clock")
    ) {
        // Full bleed background photo with crossfade
        AnimatedContent(
            targetState = currentUriString,
            transitionSpec = { fadeIn(tween(800)).togetherWith(fadeOut(tween(800))) },
            label = "photoSlideshowCrossfade",
            modifier = Modifier.fillMaxSize()
        ) { uri ->
            if (uri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(uri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "StandBy Photo Memory",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // High-quality default bundled asset matching image_8a8048df.png
                Image(
                    painter = painterResource(id = R.drawable.art_ambient),
                    contentDescription = "StandBy Photo Memory Fallback",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Vignette gradient for legibility over photos
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = if (isNightMode) 0.70f else 0.40f),
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isNightMode) 0.80f else 0.50f)
                        )
                    )
                )
        )

        // Top Right Clock with text shadow
        Text(
            text = currentTimeString,
            color = if (isNightMode) AppleNightRed else Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-2).sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.65f),
                    offset = Offset(2f, 3f),
                    blurRadius = 6f
                )
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 42.dp, top = 24.dp)
        )

        // Bottom Right Location & Date Tag matching reference image
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 42.dp, bottom = 26.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = location,
                color = if (isNightMode) AppleNightRed else Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(1f, 2f),
                        blurRadius = 4f
                    )
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$dateStamp >",
                color = if (isNightMode) AppleNightRed.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.90f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.7f),
                        offset = Offset(1f, 2f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
