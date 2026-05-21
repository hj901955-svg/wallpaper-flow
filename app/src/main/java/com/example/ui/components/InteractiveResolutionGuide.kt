package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.WallpaperEntity

@Composable
fun InteractiveResolutionGuide(
    selectedWallpaper: WallpaperEntity?,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val screenWidthPx = with(density) { screenWidthDp.dp.toPx().toInt() }
    val screenHeightPx = with(density) { screenHeightDp.dp.toPx().toInt() }

    // Dynamic scale states: 0 = FILL (Cover), 1 = FIT (Letterbox), 2 = STRETCH, 3 = CENTER
    var scaleMode by remember { mutableIntStateOf(0) }

    val aspectRadio = remember(screenWidthDp, screenHeightDp) {
        val gcd = findGcd(screenWidthPx, screenHeightPx)
        "${screenWidthPx / gcd}:${screenHeightPx / gcd}"
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = "Diagnostic",
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        text = "Universal Resolution Scan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Active diagnostic displaying screen density compatibility.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            // Screen Stats Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ResolutionStatBadge(
                    label = "Resolution",
                    value = "${screenWidthPx}x${screenHeightPx} px",
                    modifier = Modifier.weight(1f)
                )
                ResolutionStatBadge(
                    label = "Aspect Ratio",
                    value = aspectRadio,
                    modifier = Modifier.weight(1f)
                )
                ResolutionStatBadge(
                    label = "Density Rate",
                    value = "${configuration.densityDpi} DPI",
                    modifier = Modifier.weight(1f)
                )
            }

            // Interactive Virtual Viewport Demonstration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                // Background Simulated Image Grid/Canvas representing the wallpaper
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(if (scaleMode == 1) 24.dp else 0.dp), // Letterbox Simulation
                    contentAlignment = Alignment.Center
                ) {
                    val scaleFactor by animateFloatAsState(
                        targetValue = when (scaleMode) {
                            0 -> 1.25f // Fill Cover zoom
                            3 -> 0.8f  // Fixed Center
                            else -> 1.0f
                        },
                        label = "SimulatedScale"
                    )

                    // The Wallpaper Sim Card
                    Box(
                        modifier = Modifier
                            .fillMaxSize(if (scaleMode == 2) 1f else scaleFactor)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFfd746c),
                                        Color(0xFFff9068),
                                        Color(0xFF3a7bd5),
                                        Color(0xFF3a6073)
                                    )
                                )
                            )
                    ) {
                        // Graphic markings for crop bounds
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Overlay nice technical alignment grid lines
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(0f, this.size.height / 2f),
                                end = Offset(this.size.width, this.size.height / 2f),
                                strokeWidth = 1f
                            )
                            drawLine(
                                color = Color.White.copy(alpha = 0.2f),
                                start = Offset(this.size.width / 2f, 0f),
                                end = Offset(this.size.width / 2f, this.size.height),
                                strokeWidth = 1f
                            )
                        }

                        // Inside label representing simulated rendering info
                        Text(
                            text = when (scaleMode) {
                                0 -> "FILL COVER (Seamless Aspect Sync)"
                                1 -> "LETTERBOX FIT (No Cropping Bound)"
                                2 -> "STRETCH GRADIENT"
                                else -> "NORMAL CENTER"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            ),
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Aspect bounds ruler overlays
                if (scaleMode == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(8.dp)
                            .background(Color.Red.copy(alpha = 0.4f))
                            .align(Alignment.CenterStart)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(8.dp)
                            .background(Color.Red.copy(alpha = 0.4f))
                            .align(Alignment.CenterEnd)
                    )
                    Text(
                        text = "SAFE GRID AUTOFIT",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .background(Color.Red.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            // Mode Picker Selector buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScaleButton(
                    selected = scaleMode == 0,
                    label = "Smart Fill",
                    icon = Icons.Default.CropFree,
                    onClick = { scaleMode = 0 },
                    modifier = Modifier.weight(1f)
                )
                ScaleButton(
                    selected = scaleMode == 1,
                    label = "Bound Fit",
                    icon = Icons.Default.FitScreen,
                    onClick = { scaleMode = 1 },
                    modifier = Modifier.weight(1f)
                )
                ScaleButton(
                    selected = scaleMode == 2,
                    label = "Stretch",
                    icon = Icons.Default.AspectRatio,
                    onClick = { scaleMode = 2 },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "The Aurora rendering engine leverages vector coordinate scaling and high-dpi surface buffers to guarantees pixel-perfect wallpapers without scaling artifacts across all modern handset shapes, tablets, and foldable panels.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                textAlign = TextAlign.Justify,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun ResolutionStatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontSize = 8.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ScaleButton(
    selected: Boolean,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// Simple logic to compute aspect ratio terms
private fun findGcd(a: Int, b: Int): Int {
    var num1 = a
    var num2 = b
    while (num2 != 0) {
        val temp = num2
        num2 = num1 % num2
        num1 = temp
    }
    return if (num1 == 0) 1 else num1
}
