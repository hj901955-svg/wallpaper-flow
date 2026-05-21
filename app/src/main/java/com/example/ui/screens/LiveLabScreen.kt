package com.example.ui.screens

import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entities.WallpaperEntity
import com.example.ui.components.InteractiveEngineConfig
import com.example.ui.components.LiveWallpaperCanvas
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLabScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allWallpapers by viewModel.allWallpapers.collectAsStateWithLifecycle()
    val activeWallpaper by viewModel.activeWallpaper.collectAsStateWithLifecycle()

    // Filter list to only identify dynamic wallpapers
    val dynamicWallpapers = remember(allWallpapers) {
        allWallpapers.filter { it.type == "LIVE_GENERATIVE" && it.liveType != null }
    }

    // Interactive custom state variables for Sandbox Studio
    var activeEngineType by remember { mutableStateOf("PLASMA") } // PLASMA, PARTICLES, ORBIT, MATRIX
    var speedScale by remember { mutableFloatStateOf(1.0f) }
    var density by remember { mutableFloatStateOf(50f) }
    var touchReact by remember { mutableStateOf(true) }
    var sensorReact by remember { mutableStateOf(true) }
    var batteryReact by remember { mutableStateOf(true) }
    var alertsCount by remember { mutableFloatStateOf(0f) }
    var formulaPreset by remember { mutableStateOf("wave_combined") } // wave_sine, wave_crest, wave_turbulent, wave_tan
    var particlePhysics by remember { mutableStateOf("inertia") } // inertia, gravity, friction
    var customMathFactor by remember { mutableFloatStateOf(1.0f) }
    var creatorTitle by remember { mutableStateOf("My Cosmic Stream") }

    // Preset color themes mapping matching the Clean Minimalism design instructions
    val themes = remember {
        listOf(
            EngineTheme("Sunset M3", listOf(Color(0xFFFDF7FF), Color(0xFFEADDFF), Color(0xFFE8DEF8), Color(0xFF6750A4))),
            EngineTheme("Dream Aurora", listOf(Color(0xFF0F0C20), Color(0xFF1F0C3D), Color(0xFF7303C0), Color(0xFFEC38BC))),
            EngineTheme("Cyber Neon", listOf(Color(0xFF050510), Color(0xFF0D102D), Color(0xFF00FFEA), Color(0xFFE91E63))),
            EngineTheme("Solar Gold", listOf(Color(0xFF1F1005), Color(0xFF331D0A), Color(0xFFFF9F0A), Color(0xFFFFCC00)))
        )
    }
    var activeThemeIndex by remember { mutableIntStateOf(0) }
    val currentThemeColors = themes[activeThemeIndex].colors

    var showControls by remember { mutableStateOf(true) }
    var activeSubTab by remember { mutableStateOf("PRESETS") } // PRESETS, STUDIO

    val keyboardController = LocalSoftwareKeyboardController.current

    // Set interactive engine parameters from a selected wallpaper
    val applyWallpaperToEditor: (WallpaperEntity) -> Unit = { wall ->
        activeEngineType = wall.liveType ?: "PLASMA"
        val wallConfig = InteractiveEngineConfig.parseFrom(wall.prompt, activeEngineType, wall.customColors)
        speedScale = wallConfig.speedScale
        density = wallConfig.density.toFloat()
        touchReact = wallConfig.touchReact
        sensorReact = wallConfig.sensorReact
        batteryReact = wallConfig.batteryReact
        alertsCount = wallConfig.alertsCount.toFloat()
        formulaPreset = wallConfig.formulaPreset
        particlePhysics = wallConfig.particlePhysics
        customMathFactor = wallConfig.customMathFactor
        creatorTitle = wall.title
        
        // Find matching theme or extract colors
        val colorsList = wallConfig.colors
        val index = themes.indexOfFirst { it.colors.firstOrNull()?.value == colorsList.firstOrNull()?.value }
        if (index != -1) {
            activeThemeIndex = index
        }
    }

    // Synchronize editor controls with active wallpaper on first screen load
    LaunchedEffect(activeWallpaper) {
        activeWallpaper?.let { wall ->
            if (wall.type == "LIVE_GENERATIVE" && wall.liveType != null) {
                applyWallpaperToEditor(wall)
            }
        }
    }

    // Frame-drive real-time dynamic rendering based on current editor variables
    val activeStudioPrompt = remember(
        speedScale, density, touchReact, sensorReact, batteryReact, 
        alertsCount, formulaPreset, particlePhysics, customMathFactor
    ) {
        val config = InteractiveEngineConfig(
            speedScale = speedScale,
            density = density.toInt(),
            touchReact = touchReact,
            sensorReact = sensorReact,
            batteryReact = batteryReact,
            alertsCount = alertsCount.toInt(),
            formulaPreset = formulaPreset,
            particlePhysics = particlePhysics,
            customMathFactor = customMathFactor
        )
        InteractiveEngineConfig.formatToString(config)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // Live background renderer matching current customized attributes
            LiveWallpaperCanvas(
                liveType = activeEngineType,
                colors = currentThemeColors,
                modifier = Modifier.fillMaxSize(),
                promptConfig = activeStudioPrompt
            )

            // Guidance indicator overlay
            AnimatedVisibility(
                visible = !showControls,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                        .clickable { showControls = true }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Show controls",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Interactive Options",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Glassmorphic control sheet
            AnimatedVisibility(
                visible = showControls,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxHeight(0.68f)
            ) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(24.dp, shape = RoundedCornerShape(28.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                    ) {
                        // Title header controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Interactive Live Lab",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tune physics variables & hardware-reactivity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                            IconButton(
                                onClick = { showControls = false },
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Minimize Panel",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Segmented tabs switcher matching the Clean Minimalism aesthetic
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(4.dp)
                        ) {
                            listOf("PRESETS", "STUDIO").forEach { tab ->
                                val selected = activeSubTab == tab
                                val bg = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
                                val scaleColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .clickable { activeSubTab = tab }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (tab == "PRESETS") Icons.Default.PlayArrow else Icons.Default.Tune,
                                            contentDescription = tab,
                                            tint = scaleColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = tab,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = scaleColor
                                        )
                                    }
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(top = 14.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )

                        // Flexible Inner content container
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            if (activeSubTab == "PRESETS") {
                                PresetLayout(
                                    dynamicWallpapers = dynamicWallpapers,
                                    activeWallpaper = activeWallpaper,
                                    onSelectPreset = { wall ->
                                        viewModel.selectActiveWallpaper(wall)
                                        applyWallpaperToEditor(wall)
                                    },
                                    onDeletePreset = { wall ->
                                        viewModel.deleteWallpaper(wall)
                                    },
                                    onToggleFavorite = { wall ->
                                        viewModel.toggleFavorite(wall)
                                    },
                                    onNavigateToStudio = {
                                        activeSubTab = "STUDIO"
                                    }
                                )
                            } else {
                                StudioLayout(
                                    activeEngineType = activeEngineType,
                                    onChangeEngine = { activeEngineType = it },
                                    speedScale = speedScale,
                                    onChangeSpeed = { speedScale = it },
                                    density = density,
                                    onChangeDensity = { density = it },
                                    touchReact = touchReact,
                                    onChangeTouchReact = { touchReact = it },
                                    sensorReact = sensorReact,
                                    onChangeSensorReact = { sensorReact = it },
                                    batteryReact = batteryReact,
                                    onChangeBatteryReact = { batteryReact = it },
                                    alertsCount = alertsCount,
                                    onChangeAlertsCount = { alertsCount = it },
                                    formulaPreset = formulaPreset,
                                    onChangeFormula = { formulaPreset = it },
                                    particlePhysics = particlePhysics,
                                    onChangePhysics = { particlePhysics = it },
                                    customMathFactor = customMathFactor,
                                    onChangeMathFactor = { customMathFactor = it },
                                    themes = themes,
                                    activeThemeIdx = activeThemeIndex,
                                    onChangeThemeIdx = { activeThemeIndex = it },
                                    creatorTitle = creatorTitle,
                                    onChangeTitle = { creatorTitle = it },
                                    onSavePreset = {
                                        // Package configure preset into Database Wallpaper Entity
                                        val colorsHexStr = currentThemeColors.joinToString(",") {
                                            String.format("#%06X", 0xFFFFFF and it.value.toInt())
                                        }
                                        val finalConfig = InteractiveEngineConfig(
                                            speedScale = speedScale,
                                            density = density.toInt(),
                                            touchReact = touchReact,
                                            sensorReact = sensorReact,
                                            batteryReact = batteryReact,
                                            alertsCount = alertsCount.toInt(),
                                            formulaPreset = formulaPreset,
                                            particlePhysics = particlePhysics,
                                            customMathFactor = customMathFactor
                                        )
                                        val saveWord = if (creatorTitle.isBlank()) "Cosmic Stream" else creatorTitle
                                        val newPreset = WallpaperEntity(
                                            title = saveWord,
                                            type = "LIVE_GENERATIVE",
                                            uri = null,
                                            liveType = activeEngineType,
                                            customColors = colorsHexStr,
                                            category = "Generative Live",
                                            isFavorite = true,
                                            prompt = InteractiveEngineConfig.formatToString(finalConfig)
                                        )
                                        viewModel.insertCustomLiveWallpaper(newPreset)
                                        keyboardController?.hide()
                                        activeSubTab = "PRESETS"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PresetLayout(
    dynamicWallpapers: List<WallpaperEntity>,
    activeWallpaper: WallpaperEntity?,
    onSelectPreset: (WallpaperEntity) -> Unit,
    onDeletePreset: (WallpaperEntity) -> Unit,
    onToggleFavorite: (WallpaperEntity) -> Unit,
    onNavigateToStudio: () -> Unit
) {
    if (dynamicWallpapers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No dynamic live presets loaded.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dynamic Live Presets (${dynamicWallpapers.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(
                    onClick = onNavigateToStudio,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Create Custom", style = MaterialTheme.typography.labelMedium)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dynamicWallpapers, key = { it.id }) { wall ->
                    val isActive = activeWallpaper?.id == wall.id
                    val borderAlpha = if (isActive) 1.0f else 0.15f
                    val outerBorderColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = borderAlpha)

                    Card(
                        onClick = { onSelectPreset(wall) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = outerBorderColor,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Miniature engine icon avatar
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (wall.liveType?.uppercase()) {
                                            "PLASMA" -> Icons.Default.GraphicEq
                                            "PARTICLES" -> Icons.Default.Grain
                                            "ORBIT" -> Icons.Default.BlurCircular
                                            "MATRIX" -> Icons.Default.AlignVerticalBottom
                                            else -> Icons.Default.Wallpaper
                                        },
                                        contentDescription = "Engine Type",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wall.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (wall.prompt?.startsWith("PRESET;") == true) "User Composed • ${wall.liveType}" else "System Default • ${wall.liveType}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Favorite trigger
                                IconButton(
                                    onClick = { onToggleFavorite(wall) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (wall.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = if (wall.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Delete option for user creations
                                if (wall.prompt?.startsWith("PRESET;") == true) {
                                    IconButton(
                                        onClick = { onDeletePreset(wall) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Custom Preset",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioLayout(
    activeEngineType: String,
    onChangeEngine: (String) -> Unit,
    speedScale: Float,
    onChangeSpeed: (Float) -> Unit,
    density: Float,
    onChangeDensity: (Float) -> Unit,
    touchReact: Boolean,
    onChangeTouchReact: (Boolean) -> Unit,
    sensorReact: Boolean,
    onChangeSensorReact: (Boolean) -> Unit,
    batteryReact: Boolean,
    onChangeBatteryReact: (Boolean) -> Unit,
    alertsCount: Float,
    onChangeAlertsCount: (Float) -> Unit,
    formulaPreset: String,
    onChangeFormula: (String) -> Unit,
    particlePhysics: String,
    onChangePhysics: (String) -> Unit,
    customMathFactor: Float,
    onChangeMathFactor: (Float) -> Unit,
    themes: List<EngineTheme>,
    activeThemeIdx: Int,
    onChangeThemeIdx: (Int) -> Unit,
    creatorTitle: String,
    onChangeTitle: (String) -> Unit,
    onSavePreset: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Preset Engine choice
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Master Shader Node",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("PLASMA", "PARTICLES", "ORBIT", "MATRIX").forEach { cell ->
                        val selected = activeEngineType == cell
                        val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        val tc = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bg)
                                .clickable { onChangeEngine(cell) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cell,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tc
                            )
                        }
                    }
                }
            }
        }

        // Color aesthetic themes row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Color Palette",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(themes.size) { idx ->
                        val theme = themes[idx]
                        val selected = activeThemeIdx == idx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { onChangeThemeIdx(idx) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.wrapContentSize()) {
                                    theme.colors.take(3).forEach { color ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                    }
                                }
                                Text(theme.name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Sliders segment (Speed, Density, Factor)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Speed multiplier slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Simulated Physics Speed", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.1fx", speedScale), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = speedScale,
                            onValueChange = onChangeSpeed,
                            valueRange = 0.2f..3.0f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Node density multiplier slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (activeEngineType == "PLASMA") "Wave Grid Resolution" else "Element Density Count", style = MaterialTheme.typography.labelSmall)
                            Text("${density.toInt()}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = density,
                            onValueChange = onChangeDensity,
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Formula custom factor slider
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mathematical Sine Factor", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.1fx", customMathFactor), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = customMathFactor,
                            onValueChange = onChangeMathFactor,
                            valueRange = 0.4f..2.5f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Advanced mathematical formula compilation section (ONLY VISIBLE ON RELEVANT ENGINES)
        item {
            if (activeEngineType == "PLASMA") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Script Math Equations Engine",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("wave_combined" to "Dual Sin", "wave_sine" to "Raw Sin", "wave_crest" to "Crests", "wave_tan" to "Tangent", "wave_turbulent" to "Noisy").forEach { (term, label) ->
                            val selected = formulaPreset == term
                            val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val tc = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable { onChangeFormula(term) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tc
                                )
                            }
                        }
                    }
                }
            } else if (activeEngineType == "PARTICLES") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Physics Attraction Modeling",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("inertia" to "Standard Inertia", "gravity" to "Newton Gravity", "friction" to "Dampened friction").forEach { (term, label) ->
                            val selected = particlePhysics == term
                            val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val tc = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable { onChangePhysics(term) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tc
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sensor inputs checkboxes (Touch enabled, Accelerometer Gyro physical tilt, Battery plug)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Sensor Rigging Bindings",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Touch gestures
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gesture, "Touch React", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("React to Touch Gestures", fontSize = 12.sp)
                            }
                            Switch(checked = touchReact, onCheckedChange = onChangeTouchReact)
                        }

                        // Gyroscope tilt
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ScreenRotation, "Sensor Gyro", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("React to Accelerometer Tilt", fontSize = 12.sp)
                            }
                            Switch(checked = sensorReact, onCheckedChange = onChangeSensorReact)
                        }

                        // Battery state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryChargingFull, "Battery status", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("React to Battery Level / Plug", fontSize = 12.sp)
                            }
                            Switch(checked = batteryReact, onCheckedChange = onChangeBatteryReact)
                        }
                    }
                }
            }
        }

        // Mock alerts warning simulator
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "Simulated Notifications Severity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${alertsCount.toInt()} Alerts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alertsCount > 0) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Simulates unread alarms. High levels visual-shock wave templates into red or flash ripples.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Slider(
                            value = alertsCount,
                            onValueChange = onChangeAlertsCount,
                            valueRange = 0f..5f,
                            steps = 4,
                            colors = SliderDefaults.colors(
                                activeTrackColor = if (alertsCount > 0) Color.Red else MaterialTheme.colorScheme.primary,
                                thumbColor = if (alertsCount > 0) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Title textfield and Save configuration action button
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Name Wallpaper Design",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedTextField(
                    value = creatorTitle,
                    onValueChange = onChangeTitle,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("e.g. Dream wave cosmos") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSavePreset() }),
                    singleLine = true
                )
            }
        }

        // Compile action card button
        item {
            Button(
                onClick = onSavePreset,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Compile and save design")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Compile & Publish Live Wallpaper",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class EngineTheme(
    val name: String,
    val colors: List<Color>
)
