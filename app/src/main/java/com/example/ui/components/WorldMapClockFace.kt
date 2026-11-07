package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleNightRed
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.sin

// Accurate 72x36 continent bitmask sampled directly from reference world map (image_e9d18609.png)
private val CONTINENT_GRID = arrayOf(
    "....................###########.........................................",
    "..................###..#######..................###.....................",
    ".............###.####...######...............##############.............",
    "....###############.##..####.......###############################......",
    "...##############..##...##........################################......",
    "..##..#########...###.............##.########################..##.......",
    ".......##############..........##.##########################....#.......",
    ".......###############..........##############################..........",
    ".......#############............##############################..........",
    "......############.............########..#####################..........",
    "......###########..............##....########################...........",
    "......##########...............####.....####################............",
    "......#########...............######.##.####################............",
    ".......####...................##############################............",
    ".......###...................##################.############............",
    "........##...................#################...####.####..............",
    ".........###.................################.....##...###..............",
    "...........##................###############......#.....##..............",
    "..............####...........###############......#.....................",
    "..............######..........##############............#...............",
    "..............#######..............########.............#..##...........",
    ".............#########.............#######..............#.###....#......",
    ".............###########...........#######...............#.......##.....",
    "..............##########............######..............................",
    "..............##########...........#######.....................#........",
    "...............#########...........#######.#.................######.....",
    "................#######............######..#................#######.....",
    "................######..............#####..#...............#########....",
    "................######..............####...................#########....",
    "................#####................##....................########.....",
    "................####...........................................###......",
    "................####............................................#......#",
    ".................##.....................................................",
    ".................##.....................................................",
    ".................##.....................................................",
    "..................#....................................................."
)

/**
 * Authentic Apple StandBy Earth Rotation World Map Clock Face:
 * - High accuracy dot-matrix world map directly matching reference image image_e9d18609.png
 * - Real-time continuous day/night solar terminator curve based on live UTC time
 * - Pulsing glowing orange pin placed at the user's actual GPS / regional location
 * - Real location city name & high-contrast time typography
 */
@Composable
fun WorldMapClockFace(
    modifier: Modifier = Modifier,
    currentTimeString: String = "10:35",
    locationName: String = "Current Location",
    userLatitude: Double = 28.6139,
    userLongitude: Double = 77.2090,
    isNightMode: Boolean = false
) {
    // Live continuous UTC time fraction [0..1] updating every 1000ms for smooth real-time solar rotation
    val utcTimeFraction by produceState(initialValue = getUtcFraction()) {
        while (true) {
            value = getUtcFraction()
            delay(1000)
        }
    }

    // Pulsing animation for the user's actual location pin
    val infiniteTransition = rememberInfiniteTransition(label = "locationPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale"
    )

    val primaryTextColor by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color.White,
        animationSpec = tween(400),
        label = "worldMapText"
    )
    val accentOrange by animateColorAsState(
        targetValue = if (isNightMode) AppleNightRed else Color(0xFFFF9F0A),
        animationSpec = tween(400),
        label = "worldMapAccent"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("world_map_clock_face")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val rows = CONTINENT_GRID.size // 36
            val cols = CONTINENT_GRID[0].length // 72
            val stepX = w / cols
            val stepY = h / rows

            // Solar calculations based on live UTC time
            // At 12:00 UTC, subsolar longitude is 0° (Greenwich).
            // As time progresses, earth rotates east, so the subsolar meridian moves west at 15° per hour.
            val utcHours = utcTimeFraction * 24.0
            val subsolarLon = (12.0 - utcHours) * 15.0 // in degrees [-180..+180]

            // 1. Draw High-Accuracy Dot-Matrix World Continents
            for (r in 0 until rows) {
                val rowStr = CONTINENT_GRID[r]
                for (c in 0 until cols) {
                    if (rowStr[c] == '#') {
                        val cx = c * stepX + stepX * 0.5f
                        val cy = r * stepY + stepY * 0.5f

                        // Longitude in degrees from -180° (west) to +180° (east)
                        val lonDeg = ((c.toFloat() + 0.5f) / cols.toFloat()) * 360f - 180f
                        var diffLon = lonDeg - subsolarLon
                        while (diffLon > 180.0) diffLon -= 360.0
                        while (diffLon < -180.0) diffLon += 360.0

                        // Physical day/night: sunlight illuminates hemisphere within 90° of subsolar longitude
                        val isDaylight = kotlin.math.abs(diffLon) <= 88.0

                        val dotRadius = if (isDaylight) 2.6.dp.toPx() else 1.4.dp.toPx()
                        val dotAlpha = when {
                            isNightMode -> if (isDaylight) 0.85f else 0.20f
                            isDaylight -> 0.95f
                            else -> 0.22f
                        }

                        drawCircle(
                            color = (if (isNightMode) AppleNightRed else Color.White).copy(alpha = dotAlpha),
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }

            // 2. Draw Smooth Solar Day/Night Terminator Curve
            val terminatorPath = Path()
            val sampleCount = 90
            for (i in 0..sampleCount) {
                val frac = i.toFloat() / sampleCount
                val x = frac * w
                val lonDeg = frac * 360.0 - 180.0
                var diffLon = lonDeg - subsolarLon
                while (diffLon > 180.0) diffLon -= 360.0
                while (diffLon < -180.0) diffLon += 360.0
                val angleRad = diffLon * (PI / 180.0)
                // Natural solar terminator curve across cylindrical projection
                val y = h * 0.50f + (h * 0.28f) * sin(angleRad).toFloat()

                if (i == 0) {
                    terminatorPath.moveTo(x, y)
                } else {
                    terminatorPath.lineTo(x, y)
                }
            }
            drawPath(
                path = terminatorPath,
                color = (if (isNightMode) AppleNightRed else Color.White).copy(alpha = 0.28f),
                style = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. User's Actual Location Pin Marker
            // Standard Equirectangular projection mapping lon [-180..180] and lat [+85..-60]
            val pinX = ((userLongitude + 180.0) / 360.0).toFloat().coerceIn(0.04f, 0.96f) * w
            val pinY = ((85.0 - userLatitude) / 145.0).toFloat().coerceIn(0.06f, 0.94f) * h

            // Animated glowing expanding pulse ring
            val pulseRadius = 5.dp.toPx() + (16.dp.toPx() * pulseScale)
            val pulseAlpha = (1f - pulseScale) * 0.55f
            drawCircle(
                color = accentOrange.copy(alpha = pulseAlpha),
                radius = pulseRadius,
                center = Offset(pinX, pinY)
            )

            // Outer solid glow halo
            drawCircle(
                color = accentOrange.copy(alpha = 0.35f),
                radius = 8.dp.toPx(),
                center = Offset(pinX, pinY)
            )

            // Core orange marker
            drawCircle(
                color = accentOrange,
                radius = 4.5.dp.toPx(),
                center = Offset(pinX, pinY)
            )

            // Pure white center reflection
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(pinX, pinY)
            )
        }

        // Bottom Left: Actual Location City Name & Large Clean Time Typography
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Current Location",
                    tint = accentOrange,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = locationName,
                    color = accentOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = currentTimeString,
                color = primaryTextColor,
                fontSize = 82.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2.5).sp
            )
        }
    }
}

private fun getUtcFraction(): Float {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    val millisInDay = (utcCal.get(Calendar.HOUR_OF_DAY) * 3600 +
            utcCal.get(Calendar.MINUTE) * 60 +
            utcCal.get(Calendar.SECOND)) * 1000L +
            utcCal.get(Calendar.MILLISECOND)
    return (millisInDay.toDouble() / 86400000.0).toFloat()
}
