package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.database.WallpaperDatabase
import com.example.data.repository.WallpaperRepository
import com.example.ui.screens.AiStudioScreen
import com.example.ui.screens.AutoChangeScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.LiveLabScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WallpaperViewModel
import com.example.ui.viewmodel.WallpaperViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Init database assets
        val database = WallpaperDatabase.getDatabase(applicationContext)
        val repository = WallpaperRepository(database.wallpaperDao())
        val viewModelFactory = WallpaperViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: WallpaperViewModel = viewModel(factory = viewModelFactory)
                var currentTab by remember { mutableStateOf("explore") }

                Scaffold(
                    bottomBar = {
                        AuroraBottomNavigation(
                            selectedTab = currentTab,
                            onTabSelected = { currentTab = it }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentTab) {
                            "explore" -> DashboardScreen(
                                viewModel = viewModel,
                                onTabSelected = { currentTab = it }
                            )
                            "live" -> LiveLabScreen()
                            "ai" -> AiStudioScreen(viewModel = viewModel)
                            "schedule" -> AutoChangeScreen(viewModel = viewModel)
                            "favorites" -> FavoritesScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuroraBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .navigationBarsPadding() // Ensures safety on modern edge-to-edge screens with navigation gestures!
            .testTag("aurora_bottom_navigation")
    ) {
        NavigationBarItem(
            selected = selectedTab == "explore",
            onClick = { onTabSelected("explore") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Explore"
                )
            },
            label = { Text("Explore", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_explore")
        )

        NavigationBarItem(
            selected = selectedTab == "live",
            onClick = { onTabSelected("live") },
            icon = {
                Icon(
                    imageVector = Icons.Default.BlurOn,
                    contentDescription = "Live"
                )
            },
            label = { Text("Live Lab", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_lwp_lab")
        )

        NavigationBarItem(
            selected = selectedTab == "ai",
            onClick = { onTabSelected("ai") },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Designer"
                )
            },
            label = { Text("AI Studio", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_ai_designer")
        )

        NavigationBarItem(
            selected = selectedTab == "schedule",
            onClick = { onTabSelected("schedule") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Update,
                    contentDescription = "Cycles"
                )
            },
            label = { Text("Schedule", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_schedule_timeline")
        )

        NavigationBarItem(
            selected = selectedTab == "favorites",
            onClick = { onTabSelected("favorites") },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorites"
                )
            },
            label = { Text("Curated", fontSize = 10.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            modifier = Modifier.testTag("nav_curated")
        )
    }
}
