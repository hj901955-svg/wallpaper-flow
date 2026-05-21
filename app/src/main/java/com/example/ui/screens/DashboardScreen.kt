package com.example.ui.screens

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.data.entities.WallpaperEntity
import com.example.ui.components.InteractiveResolutionGuide
import com.example.ui.components.LiveWallpaperCanvas
import com.example.ui.components.WallpaperGrid
import com.example.ui.components.CategorySelectorRow
import com.example.ui.viewmodel.WallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WallpaperViewModel,
    modifier: Modifier = Modifier,
    onTabSelected: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val categories by viewModel.allCategories.collectAsState()
    val filteredWallpapers by viewModel.filteredWallpapers.collectAsState()
    val activeWallpaper by viewModel.activeWallpaper.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showResolutionGuide by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var uploadTitle by remember { mutableStateOf("") }
    var uploadCategory by remember { mutableStateOf("Minimal") }
    var uploadUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    // Status locks
    var isSettingWallpaper by remember { mutableStateOf(false) }

    // Launcher for local photo media pick picker rules
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadUri = uri
            showUploadDialog = true
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickMediaLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .shadow(12.dp, shape = FloatingActionButtonDefaults.shape)
                    .testTag("upload_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Upload Wallpaper",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Elegant brand heading
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "C A N V A S",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Light,
                    fontSize = 28.sp,
                    letterSpacing = 10.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Interactive generative canvases & dynamic styling",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Quick Actions section matching the design HTML
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upload Card
                Card(
                    onClick = {
                        pickMediaLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Upload",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Schedule Card
                Card(
                    onClick = { onTabSelected("schedule") },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Schedule",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // AI Create Card
                Card(
                    onClick = { onTabSelected("ai") },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = "AI Create",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "AI Create",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Big Active Wallpaper Showcase Card
            activeWallpaper?.let { wall ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Active Surface Canvas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    val colorStrs = wall.customColors?.split(",") ?: listOf("#07090e", "#0e131f")
                    val parsedThemeColors = remember(colorStrs) {
                        colorStrs.map {
                            try {
                                Color(android.graphics.Color.parseColor(it.trim()))
                            } catch (e: Exception) {
                                Color(0xFF00F2FE)
                            }
                        }
                    }

                    // Large preview pane
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .shadow(16.dp, shape = RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Black)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (wall.type == "LIVE_GENERATIVE" && wall.liveType != null) {
                                // Physically renders the dynamic canvas inside the explore preview frame!
                                LiveWallpaperCanvas(
                                    liveType = wall.liveType,
                                    colors = parsedThemeColors,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(wall.uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = wall.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Shaded border overlays
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                        )
                                    )
                            )

                            // Heart selector overlay top right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(wall) },
                                    modifier = Modifier.background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (wall.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Fav",
                                        tint = if (wall.isFavorite) Color.Red else Color.White
                                    )
                                }
                            }

                            // Badges top left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                            ) {
                                Surface(
                                    color = if (wall.type == "LIVE_GENERATIVE") MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.6f),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = if (wall.type == "LIVE_GENERATIVE") "LIVE SHADER" else "STATIC ART",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (wall.type == "LIVE_GENERATIVE") Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Details bottom overlay
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = wall.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = wall.prompt ?: "Stylized visual wallpaper background design concept.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Interaction row under active showcase
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isSettingWallpaper = true
                                coroutineScope.launch {
                                    val success = applyWallpaperToDevice(context, wall, parsedThemeColors)
                                    isSettingWallpaper = false
                                    if (success) {
                                        Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Set wallpaper failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(52.dp)
                                .testTag("apply_wallpaper_button")
                        ) {
                            if (isSettingWallpaper) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp))
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Apply")
                                    Text("Apply Workspace", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        FilledTonalButton(
                            onClick = { showResolutionGuide = !showResolutionGuide },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .height(52.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.FitScreen, contentDescription = "Diagnose")
                                Text("Resolution", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Expandable dynamic Resolution Guide
            AnimatedVisibility(
                visible = showResolutionGuide,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                InteractiveResolutionGuide(
                    selectedWallpaper = activeWallpaper,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Divider Line
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Categories horizontal tab selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Collections",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                CategorySelectorRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) },
                    onAddCategory = { viewModel.addCustomCategory(it) },
                    onDeleteCategory = { viewModel.deleteCategory(it.name) }
                )
            }

            // Live Grid items
            WallpaperGrid(
                wallpapers = filteredWallpapers,
                activeWallpaperId = activeWallpaper?.id ?: -1,
                onWallpaperSelected = { viewModel.selectActiveWallpaper(it) },
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onDeleteWallpaper = { viewModel.deleteWallpaper(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 800.dp)
            )
        }
    }

    // Modern Local Storage Picker confirmation Dialog
    if (showUploadDialog && uploadUri != null) {
        AlertDialog(
            onDismissRequest = {
                showUploadDialog = false
                uploadUri = null
            },
            title = { Text("Import Device Wallpaper") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Small thumbnail of picked photo to verify
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        AsyncImage(
                            model = uploadUri,
                            contentDescription = "Upload verify",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    OutlinedTextField(
                        value = uploadTitle,
                        onValueChange = { uploadTitle = it },
                        label = { Text("Wallpaper Title") },
                        placeholder = { Text("e.g. Dreamy Breeze") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("upload_title_input")
                    )

                    // Pick category
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Assign Category:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("Minimal", "Cosmic", "Nature").forEach { cat ->
                                val active = uploadCategory == cat
                                FilterChip(
                                    selected = active,
                                    onClick = { uploadCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = uploadUri
                        if (uri != null) {
                            viewModel.insertUserUploadedWallpaper(
                                context = context,
                                title = uploadTitle,
                                category = uploadCategory,
                                uri = uri
                            )
                            uploadTitle = ""
                            uploadUri = null
                            showUploadDialog = false
                        }
                    },
                    modifier = Modifier.testTag("upload_submit_button")
                ) {
                    Text("Add to Gallery")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showUploadDialog = false
                        uploadUri = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Background utility implementing REAL Android home-screen wallpaper updates
private suspend fun applyWallpaperToDevice(
    context: android.content.Context,
    wallpaper: WallpaperEntity,
    colors: List<Color>
): Boolean = withContext(Dispatchers.IO) {
    try {
        val wm = WallpaperManager.getInstance(context)
        if (wallpaper.type == "LIVE_GENERATIVE" || wallpaper.uri == null) {
            // Render beautiful vector gradient to bitmap
            val width = 1080
            val height = 1920
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)

            val ints = colors.map { it.value.toInt() }.toIntArray()
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                if (ints.size >= 2) ints else intArrayOf(ints.first(), ints.first())
            )
            gradient.setBounds(0, 0, width, height)
            gradient.draw(canvas)

            wm.setBitmap(bitmap)
            return@withContext true
        } else {
            // Static photo download loader
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(wallpaper.uri)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                if (drawable is BitmapDrawable) {
                    wm.setBitmap(drawable.bitmap)
                    return@withContext true
                }
            }
            return@withContext false
        }
    } catch (e: Exception) {
        android.util.Log.e("WallpaperService", "Set wallpaper crash error", e)
        return@withContext false
    }
}
