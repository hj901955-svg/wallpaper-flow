package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LiveWallpaperCanvas

@Composable
fun LiveLabScreen(
    modifier: Modifier = Modifier
) {
    // Current state variables for our Sandbox Interactive Engine
    var activeEngineType by remember { mutableStateOf("PLASMA") } // PLASMA, PARTICLES, ORBIT, MATRIX

    // Themes
    val themes = remember {
        listOf(
            EngineTheme("Dream Aurora", listOf(Color(0xFF0F0C20), Color(0xFF1F0C3D), Color(0xFF7303C0), Color(0xFFEC38BC))),
            EngineTheme("Cyber Synth", listOf(Color(0xFF050510), Color(0xFF0D102D), Color(0xFF00FFEA), Color(0xFFE91E63))),
            EngineTheme("Forest Essence", listOf(Color(0xFF0D1F16), Color(0xFF193324), Color(0xFF66BB6A), Color(0xFFD4E157))),
            EngineTheme("Solar Gold", listOf(Color(0xFF1F1005), Color(0xFF331D0A), Color(0xFFFF9F0A), Color(0xFFFFCC00)))
        )
    }
    var activeThemeIndex by remember { mutableIntStateOf(0) }
    val currentThemeColors = themes[activeThemeIndex].colors

    var showControls by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Full bleed background interactive live canvas!
        LiveWallpaperCanvas(
            liveType = activeEngineType,
            colors = currentThemeColors,
            modifier = Modifier.fillMaxSize()
        )

        // Sandbox Guidance Overlay top center
        AnimatedVisibility(
            visible = !showControls,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                    .clickable { showControls = true }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = "Show Settings",
                        tint = Color.White
                    )
                    Text(
                        text = "Show Controls",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Floating glass control pane at bottom
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, shape = RoundedCornerShape(28.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title Header with hide action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Interactive Live Lab",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Customize vector parameters & physics in real time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = { showControls = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Minimize",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Engine Type tabs Picker (Horizontal buttons row)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("PLASMA", "PARTICLES", "ORBIT", "MATRIX").forEach { term ->
                            val selected = activeEngineType == term
                            val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val tc = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .clickable { activeEngineType = term }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = term,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tc
                                )
                            }
                        }
                    }

                    // Color theme selector section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Aesthetic Styles",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            themes.forEachIndexed { idx, theme ->
                                val selected = activeThemeIndex == idx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { activeThemeIndex = idx }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Colors preview row
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.wrapContentSize()) {
                                            theme.colors.take(3).forEach { c ->
                                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(c))
                                            }
                                        }
                                        Text(
                                            text = theme.name,
                                            fontSize = 9.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Interactive touch instruction text
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = when (activeEngineType) {
                                "PLASMA" -> "✨ Fluid Waves: Single/multi touch distorts sine amplitudes and generates organic ripple forces across vector meshes."
                                "PARTICLES" -> "☄️ Constellation Swarm: Particle nodes experience gravitation vector shifts dynamically flocking towards touch coordinates."
                                "ORBIT" -> "🪐 Tidal Synergy: Orbits shift based on celestial anchor points. Slide of mouse / finger bends solar gravities."
                                "MATRIX" -> "❇️ Digital Rain: Glitches cascade down the cyberspace screen matrix. Touch slows stream columns and creates hot glows."
                                else -> "Tap to inject kinetic energy. Slider values update compile loops."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                            textAlign = TextAlign.Justify,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

data class EngineTheme(
    val name: String,
    val colors: List<Color>
)
