package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// Data classes for visual particle simulations
data class CanvasParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color,
    var alpha: Float = 1.0f,
    val speedScale: Float = Random.nextFloat() * 0.8f + 0.4f
)

data class OrbitalBody(
    val radius: Float,
    var angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun LiveWallpaperCanvas(
    liveType: String,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    var tick by remember { mutableLongStateOf(0L) }

    // Start a coroutine to drive the tick animation frame-by-frame
    LaunchedEffect(key1 = liveType) {
        val startTime = System.currentTimeMillis()
        while (isActive) {
            withFrameMillis { frameTime ->
                tick = frameTime - startTime
            }
        }
    }

    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var touchIntensity by remember { mutableStateOf(0.0f) }

    // Decay the touch intensity over time
    LaunchedEffect(tick) {
        if (touchIntensity > 0.01f) {
            touchIntensity *= 0.94f
        } else {
            touchPoint = null
        }
    }

    // Resolve themes from colors
    val themeColor1 = colors.getOrElse(0) { Color(0xFF1a1c29) }
    val themeColor2 = colors.getOrElse(1) { Color(0xFF03001e) }
    val themeColor3 = colors.getOrElse(2) { Color(0xFF7303c0) }
    val themeColor4 = colors.getOrElse(3) { Color(0xFFec38bc) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(liveType) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchPoint = offset
                        touchIntensity = 1.0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchPoint = change.position
                        touchIntensity = 1.0f
                    },
                    onDragEnd = {
                        touchIntensity = 1.0f // fade out
                    }
                )
            }
            .pointerInput(liveType) {
                detectTapGestures { offset ->
                    touchPoint = offset
                    touchIntensity = 1.5f
                }
            }
    ) {
        when (liveType.uppercase()) {
            "PLASMA" -> {
                PlasmaWaveWallpaper(
                    tick = tick,
                    color1 = themeColor1,
                    color2 = themeColor2,
                    color3 = themeColor3,
                    color4 = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity
                )
            }
            "PARTICLES" -> {
                ParticleSwarmWallpaper(
                    tick = tick,
                    bgColor = themeColor1,
                    particleColor1 = themeColor2,
                    particleColor2 = themeColor3,
                    particleColor3 = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity
                )
            }
            "ORBIT" -> {
                OrbitalSymphonyWallpaper(
                    tick = tick,
                    bgColor = themeColor1,
                    sunColor = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity
                )
            }
            "MATRIX" -> {
                DigitalRainWallpaper(
                    tick = tick,
                    bgColor = themeColor1,
                    rainColor = themeColor3,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity
                )
            }
            else -> {
                // Return a beautiful dynamic gradient backup
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(themeColor1, themeColor2)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun PlasmaWaveWallpaper(
    tick: Long,
    color1: Color,
    color2: Color,
    color3: Color,
    color4: Color,
    touchPoint: Offset?,
    touchIntensity: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val timeSec = tick / 1500f

        // Draw solid background or fine gradient
        drawRect(brush = Brush.verticalGradient(listOf(color1, color2)))

        // Draw overlay waving curves with complex sine movements
        val path1 = Path()
        val path2 = Path()

        path1.moveTo(0f, height * 0.6f)
        path2.moveTo(0f, height * 0.7f)

        val steps = 40
        for (i in 0..steps) {
            val cx = (width / steps) * i
            val progress = i.toFloat() / steps

            // Base waves
            var wave1Y = height * 0.6f +
                    sin(cx * 0.003f + timeSec * 1.5f) * 60f +
                    cos(cx * 0.001f - timeSec * 0.8f) * 40f

            var wave2Y = height * 0.75f +
                    cos(cx * 0.002f + timeSec * 1.1f) * 80f +
                    sin(cx * 0.004f - timeSec * 1.4f) * 50f

            // Ripple distortion if touch is active
            touchPoint?.let { tp ->
                val dist = sqrt((cx - tp.x) * (cx - tp.x) + (wave1Y - tp.y) * (wave1Y - tp.y))
                if (dist < 400) {
                    val factor = (1.0f - dist / 400f) * touchIntensity
                    wave1Y += sin(dist * 0.03f - timeSec * 8f) * 50f * factor
                }

                val dist2 = sqrt((cx - tp.x) * (cx - tp.x) + (wave2Y - tp.y) * (wave2Y - tp.y))
                if (dist2 < 400) {
                    val factor2 = (1.0f - dist2 / 400f) * touchIntensity
                    wave2Y += cos(dist2 * 0.03f - timeSec * 6f) * 60f * factor2
                }
            }

            if (i == 0) {
                path1.moveTo(cx, wave1Y)
                path2.moveTo(cx, wave2Y)
            } else {
                path1.lineTo(cx, wave1Y)
                path2.lineTo(cx, wave2Y)
            }
        }

        path1.lineTo(width, height)
        path1.lineTo(0f, height)
        path1.close()

        path2.lineTo(width, height)
        path2.lineTo(0f, height)
        path2.close()

        // Draw translucent fluid layers
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(color3.copy(alpha = 0.35f), color1.copy(alpha = 0.8f))
            )
        )
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(color4.copy(alpha = 0.45f), color2.copy(alpha = 0.9f))
            )
        )

        // Overlay aura aura around touch point
        touchPoint?.let { tp ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color4.copy(alpha = 0.4f * touchIntensity), Color.Transparent),
                    center = tp,
                    radius = 300f
                ),
                radius = 300f,
                center = tp
            )
        }
    }
}

@Composable
fun ParticleSwarmWallpaper(
    tick: Long,
    bgColor: Color,
    particleColor1: Color,
    particleColor2: Color,
    particleColor3: Color,
    touchPoint: Offset?,
    touchIntensity: Float
) {
    val particles = remember {
        mutableStateListOf<CanvasParticle>().apply {
            val random = Random(42)
            for (i in 1..80) {
                val color = when (random.nextInt(3)) {
                    0 -> particleColor1
                    1 -> particleColor2
                    else -> particleColor3
                }
                add(
                    CanvasParticle(
                        x = random.nextFloat() * 1000f,
                        y = random.nextFloat() * 2000f,
                        vx = (random.nextFloat() - 0.5f) * 1.5f,
                        vy = (random.nextFloat() - 0.5f) * 1.5f,
                        radius = random.nextFloat() * 6f + 3f,
                        color = color
                    )
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Init/Bound check particles if screen sizes change
        if (particles.firstOrNull()?.let { it.x > width * 1.5f || it.y > height * 1.5f } == true) {
            val rand = Random(42)
            for (p in particles) {
                p.x = rand.nextFloat() * width
                p.y = rand.nextFloat() * height
            }
        }

        // Fill background
        drawRect(color = bgColor)

        // Track and draw particles
        for (p in particles) {
            // Constant drifting physics
            p.x += p.vx * p.speedScale
            p.y += p.vy * p.speedScale

            // Draw boundaries wrap around
            if (p.x < 0) p.x = width
            if (p.x > width) p.x = 0f
            if (p.y < 0) p.y = height
            if (p.y > height) p.y = 0f

            // Gravitational touch pull instruction
            touchPoint?.let { tp ->
                val dx = tp.x - p.x
                val dy = tp.y - p.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 800) {
                    val force = (1.0f - dist / 800f) * 0.9f * touchIntensity
                    p.x += (dx / dist) * force * 5f
                    p.y += (dy / dist) * force * 5f
                }
            }

            // Draw glowing particle
            drawCircle(
                color = p.color.copy(alpha = 0.8f),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )

            // Dynamic halos
            if (p.radius > 6f) {
                drawCircle(
                    color = p.color.copy(alpha = 0.15f),
                    radius = p.radius * 2.5f,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // Lines joining close neighbor particles for futuristic constellation mesh
        for (i in 0 until particles.size step 3) {
            val p1 = particles[i]
            for (j in i + 1 until particles.size step 12) {
                val p2 = particles[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 180f) {
                    val lineAlpha = (1f - dist / 180f) * 0.15f
                    drawLine(
                        color = p1.color.copy(alpha = lineAlpha),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = 1.5f
                    )
                }
            }
        }
    }
}

@Composable
fun OrbitalSymphonyWallpaper(
    tick: Long,
    bgColor: Color,
    sunColor: Color,
    touchPoint: Offset?,
    touchIntensity: Float
) {
    val bodies = remember {
        listOf(
            OrbitalBody(150f, 0f, 0.009f, 8f, Color(0xFF00f2fe)),
            OrbitalBody(250f, 1.2f, 0.006f, 12f, Color(0xFFa18cd1)),
            OrbitalBody(380f, 3.4f, 0.004f, 15f, Color(0xFFfbc2eb)),
            OrbitalBody(480f, 2.1f, 0.003f, 10f, Color(0xFF38f9d7)),
            OrbitalBody(600f, 5.0f, 0.002f, 18f, Color(0xFFff9a9e))
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        // Draw solid cosmos midnight background
        drawRect(color = bgColor)

        // Stars in orbit backgrounds
        val rand = Random(99)
        for (i in 0..40) {
            val sx = rand.nextFloat() * width
            val sy = rand.nextFloat() * height
            val starAlpha = (sin((tick / 400.0f) + i) * 0.35f + 0.65f).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = starAlpha * 0.4f),
                radius = rand.nextFloat() * 2f + 1f,
                center = Offset(sx, sy)
            )
        }

        // Gravity pull distortion on central point
        val activeCenter = if (touchPoint != null) {
            Offset(
                center.x + (touchPoint.x - center.x) * 0.15f * touchIntensity,
                center.y + (touchPoint.y - center.y) * 0.15f * touchIntensity
            )
        } else {
            center
        }

        // Draw gravity orbits lines
        for (body in bodies) {
            drawCircle(
                color = sunColor.copy(alpha = 0.06f),
                radius = body.radius,
                center = activeCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }

        // Draw central cosmic star
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(sunColor, sunColor.copy(alpha = 0.3f), Color.Transparent),
                center = activeCenter,
                radius = 80f
            ),
            radius = 80f,
            center = activeCenter
        )

        // Update body angles and draw
        for (body in bodies) {
            // Orbit calculation
            // If touch is near, speed accelerates
            val speedFactor = if (touchPoint != null) {
                1.5f + touchIntensity
            } else {
                1.0f
            }
            body.angle += body.speed * speedFactor

            val bx = activeCenter.x + body.radius * cos(body.angle)
            val by = activeCenter.y + body.radius * sin(body.angle)

            // Draw glowing core
            drawCircle(
                color = body.color,
                radius = body.size,
                center = Offset(bx, by)
            )

            // Draw moon aura
            drawCircle(
                color = body.color.copy(alpha = 0.2f),
                radius = body.size * 2f,
                center = Offset(bx, by)
            )
        }
    }
}

@Composable
fun DigitalRainWallpaper(
    tick: Long,
    bgColor: Color,
    rainColor: Color,
    touchPoint: Offset?,
    touchIntensity: Float
) {
    // Rain droplets position trackers
    val columns = 25
    val dropYPositions = remember { FloatArray(columns) { Random.nextFloat() * -1000f } }
    val dropSpeeds = remember { FloatArray(columns) { Random.nextFloat() * 12f + 8f } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val colWidth = width / columns

        // Midnight base color
        drawRect(color = bgColor)

        for (col in 0 until columns) {
            val cx = col * colWidth + colWidth / 2f
            var cy = dropYPositions[col]

            // Drift speed
            val adjustedSpeed = if (touchPoint != null && sqrt((cx - touchPoint.x) * (cx - touchPoint.x)) < 150) {
                dropSpeeds[col] * (0.3f) // slows rain inside touch aura
            } else {
                dropSpeeds[col]
            }

            cy += adjustedSpeed
            if (cy > height + 200f) {
                cy = -100f
                dropSpeeds[col] = Random.nextFloat() * 12f + 8f
            }
            dropYPositions[col] = cy

            // Draw trailing tail glow
            val trailLength = 8
            for (t in 0 until trailLength) {
                val tailY = cy - t * 24f
                if (tailY in 0.0f..height) {
                    val progress = 1.0f - (t.toFloat() / trailLength)
                    val glyphAlpha = progress * 0.75f
                    val sizeScale = progress * 8.dp.toPx()

                    // Glowing digital particle glyph
                    drawCircle(
                        color = rainColor.copy(alpha = glyphAlpha),
                        radius = sizeScale / 2.5f,
                        center = Offset(cx, tailY)
                    )

                    // Draw fine glowing digital text core
                    if (t == 0) { // leading drop head is white-hot
                        drawCircle(
                            color = Color.White,
                            radius = sizeScale / 2f,
                            center = Offset(cx, tailY)
                        )
                    }
                }
            }
        }

        // Ripple reaction around touches
        touchPoint?.let { tp ->
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(rainColor.copy(alpha = 0.2f * touchIntensity), Color.Transparent),
                    center = tp,
                    radius = 200f
                ),
                radius = 200f,
                center = tp
            )
        }
    }
}
