package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WallpaperViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AutoChangeScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.scheduleSettings.collectAsState()
    val countdownSeconds by viewModel.simulatedSecondsRemaining.collectAsState()
    val schedulerLogMsg by viewModel.schedulerLogMsg.collectAsState()
    val categories by viewModel.allCategories.collectAsState()

    var showDropdownSelection by remember { mutableStateOf(false) }

    // local toggle flags driving database writes when selected
    val isEnabled = settings?.isAutoChangeEnabled ?: false
    val currentInterval = settings?.intervalMinutes ?: 60
    val currentCategory = settings?.selectedCategory ?: "All"

    val intervals = listOf(
        ScheduleInterval("Quick Demo", 1, "accelerated: loops every 5s"),
        ScheduleInterval("30 Minutes", 30, "Standard Cycle"),
        ScheduleInterval("1 Hour", 60, "Recommended Cycle"),
        ScheduleInterval("12 Hours", 720, "Day cycle"),
        ScheduleInterval("24 Hours", 1440, "Daily Refresh")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Update,
                contentDescription = "Auto-Change",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Seamless Auto-Cycle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Refreshes your background wallpaper on a scheduled timeline.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // Active Master Switch Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.AlarmOn else Icons.Default.AlarmOff,
                            contentDescription = "Active status",
                            tint = if (isEnabled) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Column {
                        Text(
                            text = "Enable Scheduler Timeline",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEnabled) "Timeline active. Countdown running." else "Timeline idle. No automatic cycles active.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f)
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { viewModel.saveScheduleSettings(it, currentInterval, currentCategory) },
                    modifier = Modifier.testTag("scheduler_timeline_switch")
                )
            }
        }

        // Configuration Panel Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Scheduler Parameters",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                // Interval Picker Selector row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Change Interval Rate:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        intervals.take(4).forEach { item ->
                            val selected = currentInterval == item.minutes
                            val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val tc = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bg)
                                    .clickable { viewModel.saveScheduleSettings(isEnabled, item.minutes, currentCategory) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = item.label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tc
                                    )
                                    if (item.minutes == 1) {
                                        Text(
                                            text = "Loop LWP",
                                            fontSize = 7.sp,
                                            color = tc.copy(alpha = 0.8f),
                                            lineHeight = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Filter selection row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Source Target Collection:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // "All" + pre-registered category items
                        val opts = listOf("All") + categories.map { it.name }
                        opts.forEach { opt ->
                            val selected = currentCategory == opt
                            val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            val tc = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bg)
                                    .clickable { viewModel.saveScheduleSettings(isEnabled, currentInterval, opt) }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opt,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tc
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Countdown & manual testing trigger controls
        ElevatedCard(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF020408)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE TICK COUNTDOWN",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Simulated active ticker circle
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.DarkGray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isEnabled) "${countdownSeconds}s" else "--",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isEnabled) Color.White else Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "remaining",
                            fontSize = 10.sp,
                            color = Color.LightGray.copy(alpha = 0.6f)
                        )
                    }
                }

                // Acceleration notice description
                Text(
                    text = "Notice: Interval timer is accelerated inside this emulator sandbox (1 minute is scaled down to 5 seconds) to demonstrate automatic wallpaper cycles visually without waiting hours.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                // Manual trigger action button
                Button(
                    onClick = { viewModel.cycleWallpaperImmediately() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cycle_now_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Shuffle, contentDescription = "Cycle Now")
                        Text("Trigger Auto-Cycle Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Interactive terminal output explaining active scheduler state
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = "Led",
                            tint = if (isEnabled) Color.Green else Color.Red,
                            modifier = Modifier.size(8.dp)
                        )
                        Text(
                            text = schedulerLogMsg,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.Green.copy(alpha = 0.82f),
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

data class ScheduleInterval(
    val label: String,
    val minutes: Int,
    val description: String
)
