package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.entities.CategoryEntity
import com.example.data.entities.WallpaperEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectorRow(
    categories: List<CategoryEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            // "All" item explicitly prepend
            item {
                CategoryChip(
                    name = "All",
                    selected = selectedCategory == "All",
                    icon = Icons.Default.AllInclusive,
                    isCustom = false,
                    onClick = { onCategorySelected("All") },
                    onDelete = {}
                )
            }

            items(categories) { category ->
                val icon = when (category.iconName) {
                    "view_quilt" -> Icons.Default.ViewQuilt
                    "bedtime" -> Icons.Default.Bedtime
                    "forest" -> Icons.Default.Forest
                    "blur_on" -> Icons.Default.BlurOn
                    "photo_spark" -> Icons.Default.AutoAwesome
                    else -> Icons.Default.Category
                }

                CategoryChip(
                    name = category.name,
                    selected = selectedCategory == category.name,
                    icon = icon,
                    isCustom = category.isCustom,
                    onClick = { onCategorySelected(category.name) },
                    onDelete = { onDeleteCategory(category) }
                )
            }

            // Button to add new category
            item {
                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { showAddDialog = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Category",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "New Category",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Minimalist Teal") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("custom_category_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(newCategoryName)
                            newCategoryName = ""
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("custom_category_submit")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryChip(
    name: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCustom: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (selected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )

            if (isCustom) {
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete category",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(12.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
fun WallpaperGrid(
    wallpapers: List<WallpaperEntity>,
    activeWallpaperId: Long,
    onWallpaperSelected: (WallpaperEntity) -> Unit,
    onToggleFavorite: (WallpaperEntity) -> Unit,
    onDeleteWallpaper: (WallpaperEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (wallpapers.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = "Empty",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No Wallpapers Present",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Text(
                    text = "Use device uploads or the AI Studio tab to create designs.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wallpapers, key = { it.id }) { wall ->
                val isActive = wall.id == activeWallpaperId
                WallpaperTile(
                    wallpaper = wall,
                    isActive = isActive,
                    onClick = { onWallpaperSelected(wall) },
                    onToggleFavorite = { onToggleFavorite(wall) },
                    onDelete = { onDeleteWallpaper(wall) }
                )
            }
        }
    }
}

@Composable
fun WallpaperTile(
    wallpaper: WallpaperEntity,
    isActive: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    // Resolve gradients
    val fallbackColor = MaterialTheme.colorScheme.secondary
    val colorStrs = wallpaper.customColors?.split(",") ?: listOf("#1a1c29", "#03001e")
    val gradientColors = remember(colorStrs, fallbackColor) {
        colorStrs.map {
            try {
                Color(android.graphics.Color.parseColor(it.trim()))
            } catch (e: Exception) {
                fallbackColor
            }
        }
    }

    val isLive = wallpaper.type == "LIVE_GENERATIVE"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .shadow(
                elevation = if (isActive) 12.dp else 0.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MaterialTheme.colorScheme.primary,
                spotColor = MaterialTheme.colorScheme.primary
            )
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("wallpaper_tile_${wallpaper.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper Graphic Representation (Static Image vs Gradient representor for Live)
            if (wallpaper.uri != null) {
                // Static picture loaders
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(wallpaper.uri)
                        .crossfade(true)
                        .build(),
                    contentDescription = wallpaper.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Live representor gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = if (gradientColors.size >= 2) gradientColors else listOf(gradientColors.first(), gradientColors.first())
                            )
                        )
                ) {
                    // Small decorative animated circles
                    Icon(
                        imageVector = Icons.Default.BlurOn,
                        contentDescription = "Live",
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            // Shading Overlay to read details easily
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.82f)),
                            startY = 180f
                        )
                    )
            )

            // Dynamic Icons badges (e.g. LIVE badge or AI badge)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isLive) Color(0xFF00FFCC).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isLive) "LIVE" else "STATIC",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLive) Color.Black else Color.White,
                        letterSpacing = 0.5.sp
                    )
                }

                // Delete option if custom user creation!
                if (wallpaper.id > 10) { // IDs greater than 10 are custom user additions
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Info details bottom bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = wallpaper.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = wallpaper.category,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 12.sp
                )
            }

            // Favorite Overlay controls right border
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.Black.copy(alpha = 0.45f), CircleShape)
            ) {
                Icon(
                    imageVector = if (wallpaper.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (wallpaper.isFavorite) Color.Red else Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Outline active highlights indicator
            if (isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(20.dp)
                        )
                )
            }
        }
    }
}
