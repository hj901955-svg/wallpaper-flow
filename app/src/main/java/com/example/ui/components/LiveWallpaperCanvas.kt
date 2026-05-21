package com.example.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.BatteryManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
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

// Fully customizable configuration data class for Advanced Interactive live wallpapers
data class InteractiveEngineConfig(
    val title: String = "Untitled Preset",
    val liveType: String = "PLASMA", // PLASMA, PARTICLES, ORBIT, MATRIX
    val colors: List<Color> = listOf(Color(0xFF6750A4), Color(0xFFEADDFF), Color(0xFFE8DEF8)),
    val speedScale: Float = 1.0f,
    val density: Int = 50,
    val touchReact: Boolean = true,
    val sensorReact: Boolean = true,
    val batteryReact: Boolean = true,
    val alertsCount: Int = 0,
    val formulaPreset: String = "wave_combined", // wave_sine, wave_crest, wave_turbulent, wave_vortex, wave_tan
    val particlePhysics: String = "inertia", // gravity, inertia, friction, celestial
    val primaryHexColors: String = "#6750A4,#EADDFF,#E8DEF8",
    val customMathFactor: Float = 1.0f
) {
    companion object {
        fun parseFrom(prompt: String?, liveType: String, customColors: String?): InteractiveEngineConfig {
            val colorStrs = customColors?.split(",") ?: emptyList()
            val colorsList = if (colorStrs.isNotEmpty()) {
                colorStrs.mapNotNull {
                    try {
                        Color(android.graphics.Color.parseColor(it.trim()))
                    } catch (e: Exception) {
                        null
                    }
                }
            } else emptyList()
            
            val finalColors = if (colorsList.size >= 2) colorsList else listOf(Color(0xFF6750A4), Color(0xFFEADDFF), Color(0xFFE8DEF8))
            val finalHex = customColors ?: "#6750A4,#EADDFF,#E8DEF8"

            if (prompt == null || !prompt.startsWith("PRESET;")) {
                return InteractiveEngineConfig(
                    title = "Preset Settings",
                    liveType = liveType,
                    colors = finalColors,
                    primaryHexColors = finalHex
                )
            }

            var speed = 1.0f
            var density = 50
            var touch = true
            var sensor = true
            var battery = true
            var alerts = 0
            var formula = "wave_combined"
            var physics = "inertia"
            var factor = 1.0f

            try {
                val parts = prompt.split(";")
                for (part in parts) {
                    val kv = part.split("=")
                    if (kv.size == 2) {
                        val key = kv[0].trim()
                        val value = kv[1].trim()
                        when (key) {
                            "speed" -> speed = value.toFloatOrNull() ?: 1.0f
                            "density" -> density = value.toIntOrNull() ?: 50
                            "touch" -> touch = value.toBooleanStrictOrNull() ?: true
                            "sensor" -> sensor = value.toBooleanStrictOrNull() ?: true
                            "battery" -> battery = value.toBooleanStrictOrNull() ?: true
                            "alerts" -> alerts = value.toIntOrNull() ?: 0
                            "formula" -> formula = value
                            "physics" -> physics = value
                            "factor" -> factor = value.toFloatOrNull() ?: 1.0f
                        }
                    }
                }
            } catch (e: Exception) {
                // Safeguard
            }

            return InteractiveEngineConfig(
                title = "Custom Creation",
                liveType = liveType,
                colors = finalColors,
                speedScale = speed,
                density = density,
                touchReact = touch,
                sensorReact = sensor,
                batteryReact = battery,
                alertsCount = alerts,
                formulaPreset = formula,
                particlePhysics = physics,
                primaryHexColors = finalHex,
                customMathFactor = factor
            )
        }

        fun formatToString(config: InteractiveEngineConfig): String {
            return "PRESET;speed=${config.speedScale};density=${config.density};touch=${config.touchReact};sensor=${config.sensorReact};battery=${config.batteryReact};alerts=${config.alertsCount};formula=${config.formulaPreset};physics=${config.particlePhysics};factor=${config.customMathFactor}"
        }
    }
}

@Composable
fun LiveWallpaperCanvas(
    liveType: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    promptConfig: String? = null
) {
    var tick by remember { mutableLongStateOf(0L) }

    // Resolve advanced configuration parsed from state or template values
    val config = remember(promptConfig, liveType, colors) {
        // Construct standard hex string representation from colors if custom colors not present
        val hexStr = colors.joinToString(",") { String.format("#%06X", 0xFFFFFF and it.value.toInt()) }
        InteractiveEngineConfig.parseFrom(promptConfig, liveType, hexStr)
    }

    // Start a coroutine to drive the tick animation frame-by-frame
    LaunchedEffect(config.liveType, config.speedScale) {
        val startTime = System.currentTimeMillis()
        while (isActive) {
            withFrameMillis { frameTime ->
                // Apply our customizable speed scaling directly to the animation clock!
                val delta = (frameTime - startTime) * config.speedScale
                tick = delta.toLong()
            }
        }
    }

    // Sensor state tracking
    val context = LocalContext.current
    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }

    DisposableEffect(config.sensorReact) {
        if (!config.sensorReact) onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    tiltX = -event.values[0] // Inverse for intuitive parallax shifting
                    tiltY = event.values[1]
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Battery status tracking
    var batteryPercent by remember { mutableStateOf(85f) }
    var isCharging by remember { mutableStateOf(false) }

    DisposableEffect(config.batteryReact) {
        if (!config.batteryReact) onDispose {}

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null && intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryPercent = (level * 100f) / scale
                    }
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || 
                                 status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        onDispose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Safeguard against unregister errors on rapid lifecycle switches
            }
        }
    }

    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var touchIntensity by remember { mutableStateOf(0.0f) }

    // Decay the touch intensity over time
    LaunchedEffect(tick) {
        if (touchIntensity > 0.01f) {
            touchIntensity *= 0.95f
        } else {
            touchPoint = null
        }
    }

    // Colors mapping
    val themeColor1 = colors.getOrElse(0) { Color(0xFFFDF7FF) }
    val themeColor2 = colors.getOrElse(1) { Color(0xFFEADDFF) }
    val themeColor3 = colors.getOrElse(2) { Color(0xFFE8DEF8) }
    val themeColor4 = colors.getOrElse(3) { Color(0xFF6750A4) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(config.liveType) {
                if (config.touchReact) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            touchPoint = offset
                            touchIntensity = 1.0f
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            touchPoint = change.position
                            touchIntensity = 1.0f
                        },
                        onDragEnd = {
                            touchIntensity = 1.0f
                        }
                    )
                }
            }
            .pointerInput(config.liveType) {
                if (config.touchReact) {
                    detectTapGestures { offset ->
                        touchPoint = offset
                        touchIntensity = 1.5f
                    }
                }
            }
    ) {
        when (config.liveType.uppercase()) {
            "PLASMA" -> {
                PlasmaWaveWallpaper(
                    tick = tick,
                    color1 = themeColor1,
                    color2 = themeColor2,
                    color3 = themeColor3,
                    color4 = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    batteryPct = batteryPercent,
                    isCharging = isCharging,
                    config = config
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
                    touchIntensity = touchIntensity,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    batteryPct = batteryPercent,
                    isCharging = isCharging,
                    config = config
                )
            }
            "ORBIT" -> {
                OrbitalSymphonyWallpaper(
                    tick = tick,
                    bgColor = themeColor1,
                    sunColor = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    batteryPct = batteryPercent,
                    isCharging = isCharging,
                    config = config
                )
            }
            "MATRIX" -> {
                DigitalRainWallpaper(
                    tick = tick,
                    bgColor = themeColor1,
                    rainColor = themeColor4,
                    touchPoint = touchPoint,
                    touchIntensity = touchIntensity,
                    tiltX = tiltX,
                    tiltY = tiltY,
                    batteryPct = batteryPercent,
                    config = config
                )
            }
            else -> {
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
    touchIntensity: Float,
    tiltX: Float,
    tiltY: Float,
    batteryPct: Float,
    isCharging: Boolean,
    config: InteractiveEngineConfig
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val timeSec = tick / 1500f

        // Adjust backgrounds based on battery status
        val dynamicBgColor1 = if (config.batteryReact && batteryPct < 20f) {
            color1.copy(alpha = 0.5f) // Deeply dimmed background for battery optimization mode
        } else {
            color1
        }
        val dynamicBgColor2 = if (config.batteryReact && batteryPct < 20f) {
            Color(0xFF121214)
        } else {
            color2
        }

        drawRect(brush = Brush.verticalGradient(listOf(dynamicBgColor1, dynamicBgColor2)))

        // Setup wave paths
        val path1 = Path()
        val path2 = Path()

        // Parallax sensor shift offsets
        val sensorXShift = if (config.sensorReact) tiltX * 12f else 0f
        val sensorYShift = if (config.sensorReact) tiltY * 15f else 0f

        // Density determines drawing complexity
        val steps = (config.density / 2).coerceIn(10, 60)
        
        // Base formulas driven by the code API expression
        val waveFreq = config.customMathFactor * 0.003f
        val ampMultiplier = if (config.batteryReact && isCharging) 1.3f else 1.0f

        for (i in 0..steps) {
            val cx = (width / steps) * i
            
            // Evaluates math formula presets selected or composed by the advanced user!
            val baselineWave1 = when (config.formulaPreset) {
                "wave_sine" -> {
                    sin(cx * waveFreq + timeSec)
                }
                "wave_crest" -> {
                    sin(cx * waveFreq + timeSec * 1.5f) * cos(cx * waveFreq * 0.5f)
                }
                "wave_tan" -> {
                    sin(cx * waveFreq + timeSec) * tan((cx * waveFreq * 0.1f).coerceIn(-1.4f, 1.4f))
                }
                "wave_turbulent" -> {
                    sin(cx * waveFreq + timeSec) * 0.7f + sin(cx * waveFreq * 2.5f - timeSec * 1.8f) * 0.3f
                }
                else -> { // wave_combined
                    sin(cx * waveFreq + timeSec * 1.5f) * 0.65f + cos(cx * waveFreq * 0.8f - timeSec * 0.8f) * 0.35f
                }
            }

            val baselineWave2 = when (config.formulaPreset) {
                "wave_sine" -> {
                    cos(cx * waveFreq * 0.8f - timeSec)
                }
                "wave_crest" -> {
                    cos(cx * waveFreq + timeSec * 1.1f) * sin(cx * waveFreq * 0.4f)
                }
                "wave_tan" -> {
                    cos(cx * waveFreq - timeSec) * tan((cx * waveFreq * 0.08f).coerceIn(-1.4f, 1.4f))
                }
                "wave_turbulent" -> {
                    cos(cx * waveFreq * 0.5f - timeSec * 1.2f) * 0.8f + sin(cx * waveFreq * 3.5f + timeSec * 2f) * 0.2f
                }
                else -> { // wave_combined
                    cos(cx * waveFreq * 0.7f + timeSec * 1.1f) * 0.6f + sin(cx * waveFreq * 1.4f - timeSec * 1.4f) * 0.4f
                }
            }

            var wave1Y = height * 0.58f + sensorYShift + baselineWave1 * 75f * ampMultiplier
            var wave2Y = height * 0.72f + sensorYShift + baselineWave2 * 90f * ampMultiplier

            // Ripple force reactions on Touch
            if (config.touchReact) {
                touchPoint?.let { tp ->
                    val dx = (cx + sensorXShift) - tp.x
                    
                    val dist1 = sqrt(dx * dx + (wave1Y - tp.y) * (wave1Y - tp.y))
                    if (dist1 < 420f) {
                        val factor1 = (1.0f - dist1 / 420f) * touchIntensity
                        wave1Y += sin(dist1 * 0.03f - timeSec * 8f) * 65f * factor1
                    }

                    val dist2 = sqrt(dx * dx + (wave2Y - tp.y) * (wave2Y - tp.y))
                    if (dist2 < 420f) {
                        val factor2 = (1.0f - dist2 / 420f) * touchIntensity
                        wave2Y += cos(dist2 * 0.03f - timeSec * 6f) * 75f * factor2
                    }
                }
            }

            if (i == 0) {
                path1.moveTo(cx + sensorXShift, wave1Y)
                path2.moveTo(cx + sensorXShift, wave2Y)
            } else {
                path1.lineTo(cx + sensorXShift, wave1Y)
                path2.lineTo(cx + sensorXShift, wave2Y)
            }
        }

        path1.lineTo(width + 200f, height + 100f)
        path1.lineTo(-200f, height + 100f)
        path1.close()

        path2.lineTo(width + 200f, height + 100f)
        path2.lineTo(-200f, height + 100f)
        path2.close()

        // Draw translucent Material fluid layers
        drawPath(
            path = path2,
            brush = Brush.verticalGradient(
                colors = listOf(color3.copy(alpha = 0.38f), color1.copy(alpha = 0.72f))
            )
        )
        drawPath(
            path = path1,
            brush = Brush.verticalGradient(
                colors = listOf(color4.copy(alpha = 0.48f), color2.copy(alpha = 0.85f))
            )
        )

        // Overlay glowing elements for charging or high unread notices!
        if (config.batteryReact && isCharging) {
            // Electrical rising charging energy nodes
            val random = Random(42)
            for (p in 1..8) {
                val sparkX = (timeSec * 50f + random.nextFloat() * width) % width
                val sparkY = (height - (timeSec * 80f + random.nextFloat() * height) % height)
                drawCircle(
                    color = Color(0xFFFFD54F).copy(alpha = 0.6f),
                    radius = 4.dp.toPx(),
                    center = Offset(sparkX, sparkY)
                )
            }
        }

        // Concentric unread alerts pulsing warnings
        if (config.alertsCount > 0) {
            val alertPulse = (sin(timeSec * 4f) * 0.4f + 0.6f)
            val glowColor = Color(0xFFFF5252).copy(alpha = alertPulse * 0.15f)
            drawCircle(
                color = glowColor,
                radius = 120.dp.toPx() * config.alertsCount.coerceAtMost(3),
                center = Offset(width / 2f, 80.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
            )
        }

        // Overlay touch visual halos
        if (config.touchReact) {
            touchPoint?.let { tp ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(color4.copy(alpha = 0.45f * touchIntensity), Color.Transparent),
                        center = tp,
                        radius = 250f
                    ),
                    radius = 250f,
                    center = tp
                )
            }
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
    touchIntensity: Float,
    tiltX: Float,
    tiltY: Float,
    batteryPct: Float,
    isCharging: Boolean,
    config: InteractiveEngineConfig
) {
    // Generate particle population matching user density preference
    val maxParticles = config.density.coerceIn(15, 120)
    
    val particles = remember(maxParticles) {
        mutableStateListOf<CanvasParticle>().apply {
            val random = Random(7)
            for (i in 1..maxParticles) {
                val color = when (random.nextInt(3)) {
                    0 -> particleColor1
                    1 -> particleColor2
                    else -> particleColor3
                }
                add(
                    CanvasParticle(
                        x = random.nextFloat() * 1000f,
                        y = random.nextFloat() * 2000f,
                        vx = (random.nextFloat() - 0.5f) * 2f,
                        vy = (random.nextFloat() - 0.5f) * 2f,
                        radius = random.nextFloat() * 5f + 3f,
                        color = color
                    )
                )
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Boundaries checks
        if (particles.firstOrNull()?.let { it.x > width * 1.5f || it.y > height * 1.5f } == true) {
            val rand = Random(13)
            for (p in particles) {
                p.x = rand.nextFloat() * width
                p.y = rand.nextFloat() * height
            }
        }

        drawRect(color = bgColor)

        // Accelerometer directional force
        val forceX = if (config.sensorReact) tiltX * 0.18f else 0f
        val forceY = if (config.sensorReact) tiltY * 0.18f else 0f

        // Physics preset multipliers derived from customize studio
        val friction = when (config.particlePhysics) {
            "friction" -> 0.94f
            "gravity" -> 0.99f
            else -> 0.975f // inertia
        }

        val attractionPower = config.customMathFactor * 0.85f

        for (p in particles) {
            // Apply Accelerometer drift
            p.vx = (p.vx + forceX) * friction
            p.vy = (p.vy + forceY) * friction

            // Update local coordinate ticks
            p.x += p.vx * p.speedScale
            p.y += p.vy * p.speedScale

            // Boundary wrapping
            if (p.x < 0) p.x = width
            if (p.x > width) p.x = 0f
            if (p.y < 0) p.y = height
            if (p.y > height) p.y = 0f

            // Touch attraction algorithms
            if (config.touchReact) {
                touchPoint?.let { tp ->
                    val dx = tp.x - p.x
                    val dy = tp.y - p.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < 750f) {
                        val forceStrength = (1.0f - dist / 750f) * attractionPower * touchIntensity
                        
                        when (config.particlePhysics) {
                            "gravity" -> { // Real galactic gravity pull
                                val grav = (40f / (dist + 50f)).coerceAtMost(3f)
                                p.vx += (dx / dist) * grav * forceStrength
                                p.vy += (dy / dist) * grav * forceStrength
                            }
                            "friction" -> { // Slows particles and locks them
                                p.x += (dx / dist) * forceStrength * 3.5f
                                p.y += (dy / dist) * forceStrength * 3.5f
                                p.vx *= 0.85f
                                p.vy *= 0.85f
                            }
                            else -> { // Standard kinetic inertia pull
                                p.x += (dx / dist) * forceStrength * 6f
                                p.y += (dy / dist) * forceStrength * 6f
                            }
                        }
                    }
                }
            }

            // Adjust colors or render sizes dynamically based on system indicators
            val finalRadius = if (config.batteryReact) {
                p.radius * (0.5f + (batteryPct / 100f) * 0.7f)
            } else {
                p.radius
            }

            val finalColor = if (config.alertsCount > 0 && Random.nextFloat() < 0.2f) {
                Color(0xFFFF5252) // Warning alert nodes colored alarm red
            } else {
                p.color
            }

            // Draw particle
            drawCircle(
                color = finalColor.copy(alpha = 0.8f),
                radius = finalRadius,
                center = Offset(p.x, p.y)
            )

            // Outer glows
            if (p.radius > 5f) {
                val glowAlpha = if (isCharging) 0.35f else 0.15f
                drawCircle(
                    color = finalColor.copy(alpha = glowAlpha),
                    radius = finalRadius * 2.8f,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // Draw structural futuristic constellation meshes
        for (i in 0 until particles.size step 4) {
            val p1 = particles[i]
            for (j in i + 1 until particles.size step 12) {
                val p2 = particles[j]
                val dx = p1.x - p2.x
                val dy = p1.y - p2.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 190f) {
                    val lineAlpha = (1f - dist / 190f) * 0.16f
                    drawLine(
                        color = p1.color.copy(alpha = lineAlpha),
                        start = Offset(p1.x, p1.y),
                        end = Offset(p2.x, p2.y),
                        strokeWidth = 1.2f
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
    touchIntensity: Float,
    tiltX: Float,
    tiltY: Float,
    batteryPct: Float,
    isCharging: Boolean,
    config: InteractiveEngineConfig
) {
    // Advanced user counts determine orbits
    val ringCount = (config.density / 20).coerceIn(3, 8)
    val bodies = remember(ringCount) {
        val colorsPalette = listOf(
            Color(0xFF00F2FE), Color(0xFF9C27B0), Color(0xFFFF2A6D), 
            Color(0xFF05D9E8), Color(0xFFF77F00), Color(0xFF00FF87)
        )
        val rand = Random(23)
        List(ringCount) { idx ->
            OrbitalBody(
                radius = 130f + idx * 80f,
                angle = rand.nextFloat() * 6.28f,
                speed = 0.003f + (0.012f / (idx + 1)),
                size = 7f + rand.nextFloat() * 10f,
                color = colorsPalette.getOrElse(idx % colorsPalette.size) { sunColor }
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)

        drawRect(color = bgColor)

        // Passive sky background cosmic stars
        val rand = Random(88)
        for (i in 0..30) {
            val sx = rand.nextFloat() * width
            val sy = rand.nextFloat() * height
            val starAlpha = (sin((tick / 400f) + i) * 0.3f + 0.7f)
            drawCircle(
                color = Color.White.copy(alpha = starAlpha * 0.32f),
                radius = rand.nextFloat() * 1.5f + 1f,
                center = Offset(sx, sy)
            )
        }

        // Dynamic gravitational anchor coordinates influenced by Gyro tilt & touches
        var gravityOffset = Offset.Zero
        if (config.sensorReact) {
            gravityOffset += Offset(tiltX * 14f, tiltY * 14f)
        }
        if (config.touchReact && touchPoint != null) {
            gravityOffset += Offset(
                (touchPoint.x - center.x) * 0.16f * touchIntensity,
                (touchPoint.y - center.y) * 0.16f * touchIntensity
            )
        }

        val activeCenter = center + gravityOffset

        // Interactive visual orbital lines drawing
        for (body in bodies) {
            drawCircle(
                color = sunColor.copy(alpha = 0.05f),
                radius = body.radius,
                center = activeCenter,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }

        // Draw Central Star (Core radius represents battery level scale!)
        val baseSunRadius = 60f + if (config.batteryReact) (batteryPct / 100f) * 45f else 30f
        val pulseIntensity = (sin(tick / 200f) * 0.1f + 0.9f)
        val finalSunRadius = baseSunRadius * pulseIntensity

        // Charging halo ring overrides
        if (config.batteryReact && isCharging) {
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.12f),
                radius = finalSunRadius * 2.2f,
                center = activeCenter
            )
        }

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(sunColor, sunColor.copy(alpha = 0.28f), Color.Transparent),
                center = activeCenter,
                radius = finalSunRadius * 1.6f
            ),
            radius = finalSunRadius * 1.6f,
            center = activeCenter
        )

        val orbitalSpeedFactor = config.customMathFactor * (if (touchPoint != null) 2.2f else 1.0f)

        // Dynamic planet bodies animations
        for (body in bodies) {
            body.angle += body.speed * orbitalSpeedFactor
            val bx = activeCenter.x + body.radius * cos(body.angle)
            val by = activeCenter.y + body.radius * sin(body.angle)

            drawCircle(
                color = body.color,
                radius = body.size,
                center = Offset(bx, by)
            )
            drawCircle(
                color = body.color.copy(alpha = 0.22f),
                radius = body.size * 2.2f,
                center = Offset(bx, by)
            )
        }

        // Alerts triggers create warning alert moons!
        if (config.alertsCount > 0) {
            for (i in 0 until config.alertsCount.coerceAtMost(5)) {
                val alertAngle = (tick / 300f) + (i * 1.25f)
                val alertRadius = finalSunRadius + 45f + (i * 20f)
                val ax = activeCenter.x + alertRadius * cos(alertAngle)
                val ay = activeCenter.y + alertRadius * sin(alertAngle)
                drawCircle(
                    color = Color(0xFFFF5252),
                    radius = 4.5f.dp.toPx(),
                    center = Offset(ax, ay)
                )
            }
        }
    }
}

@Composable
fun DigitalRainWallpaper(
    tick: Long,
    bgColor: Color,
    rainColor: Color,
    touchPoint: Offset?,
    touchIntensity: Float,
    tiltX: Float,
    tiltY: Float,
    batteryPct: Float,
    config: InteractiveEngineConfig
) {
    val columns = config.density.coerceIn(10, 40)
    val dropYPositions = remember(columns) { FloatArray(columns) { Random.nextFloat() * -1200f } }
    val dropSpeeds = remember(columns) { FloatArray(columns) { Random.nextFloat() * 11f + 7f } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val colWidth = width / columns

        // Deep minimalist background
        drawRect(color = bgColor)

        // Warn notifications override color streams to retro red
        val finalRainColor = if (config.alertsCount > 0) {
            Color(0xFFFF5252)
        } else {
            rainColor
        }

        // Battery level governs maximum brightness alphas
        val batteryAlphaScale = if (config.batteryReact) {
            (0.35f + (batteryPct / 100f) * 0.65f)
        } else 1.0f

        for (col in 0 until columns) {
            val cx = col * colWidth + colWidth / 2f
            var cy = dropYPositions[col]

            // Interaction slow-buffer inside Touch coordinate circles
            val touchSlowdown = if (config.touchReact && touchPoint != null && sqrt((cx - touchPoint.x) * (cx - touchPoint.x)) < 160f) {
                0.3f // slow rain down to 30% speed
            } else {
                1.0f
            }

            cy += dropSpeeds[col] * touchSlowdown
            if (cy > height + 200f) {
                cy = -120f
                dropSpeeds[col] = Random.nextFloat() * 11f + 7f
            }
            dropYPositions[col] = cy

            // Stream trailing rain nodes
            val trailCount = 8
            for (t in 0 until trailCount) {
                // Incorporate Sensor Tilt directly into diagonal rain cascade trajectories!
                val trajectoryTiltOffset = if (config.sensorReact) (t * 8f) * -tiltX else 0f
                val tailY = cy - t * 22f

                if (tailY in 0.0f..height) {
                    val progress = 1.0f - (t.toFloat() / trailCount)
                    val glyphAlpha = progress * 0.78f * batteryAlphaScale
                    val finalSize = progress * 7.dp.toPx()

                    drawCircle(
                        color = finalRainColor.copy(alpha = glyphAlpha),
                        radius = finalSize / 2f,
                        center = Offset(cx + trajectoryTiltOffset, tailY)
                    )

                    // Glowing hot rain heads
                    if (t == 0) {
                        drawCircle(
                            color = Color.White.copy(alpha = batteryAlphaScale),
                            radius = finalSize / 1.6f,
                            center = Offset(cx + trajectoryTiltOffset, tailY)
                        )
                    }
                }
            }
        }

        // Touch feedback radial highlights
        if (config.touchReact) {
            touchPoint?.let { tp ->
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(finalRainColor.copy(alpha = 0.25f * touchIntensity), Color.Transparent),
                        center = tp,
                        radius = 180f
                    ),
                    radius = 180f,
                    center = tp
                )
            }
        }
    }
}
