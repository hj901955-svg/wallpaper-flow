package com.example.data.repository

import com.example.data.dao.WallpaperDao
import com.example.data.entities.WallpaperEntity
import com.example.data.entities.CategoryEntity
import com.example.data.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class WallpaperRepository(private val wallpaperDao: WallpaperDao) {

    val allWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getAllWallpapers()
    val favoriteWallpapers: Flow<List<WallpaperEntity>> = wallpaperDao.getFavoriteWallpapers()
    val allCategories: Flow<List<CategoryEntity>> = wallpaperDao.getAllCategories()
    val scheduleSettings: Flow<ScheduleEntity?> = wallpaperDao.getScheduleSettings()

    fun getWallpapersByCategory(category: String): Flow<List<WallpaperEntity>> {
        return wallpaperDao.getWallpapersByCategory(category)
    }

    suspend fun getWallpaperById(id: Long): WallpaperEntity? {
        return wallpaperDao.getWallpaperById(id)
    }

    suspend fun insertWallpaper(wallpaper: WallpaperEntity): Long {
        return wallpaperDao.insertWallpaper(wallpaper)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        wallpaperDao.updateFavoriteStatus(id, isFavorite)
    }

    suspend fun deleteWallpaper(id: Long) {
        wallpaperDao.deleteWallpaperById(id)
    }

    suspend fun addCategory(name: String) {
        wallpaperDao.insertCategory(CategoryEntity(name = name, isCustom = true))
    }

    suspend fun deleteCategory(name: String) {
        wallpaperDao.deleteCategory(name)
    }

    suspend fun saveScheduleSettings(settings: ScheduleEntity) {
        wallpaperDao.updateScheduleSettings(settings)
    }

    suspend fun updateCurrentWallpaper(wallpaperId: Long) {
        val current = wallpaperDao.getScheduleSettingsDirect() ?: ScheduleEntity()
        wallpaperDao.updateScheduleSettings(
            current.copy(
                currentWallpaperId = wallpaperId,
                lastChangedTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun seedDatabase() {
        // Seed default categories
        val existingCategories = wallpaperDao.getAllCategories().firstOrNull() ?: emptyList()
        if (existingCategories.isEmpty()) {
            val defaults = listOf(
                CategoryEntity("Minimal", isCustom = false, iconName = "view_quilt"),
                CategoryEntity("Cosmic", isCustom = false, iconName = "bedtime"),
                CategoryEntity("Nature", isCustom = false, iconName = "forest"),
                CategoryEntity("Generative Live", isCustom = false, iconName = "blur_on"),
                CategoryEntity("AI Creations", isCustom = false, iconName = "photo_spark")
            )
            for (category in defaults) {
                wallpaperDao.insertCategory(category)
            }
        }

        // Seed default wallpapers
        val existingWallpapers = wallpaperDao.getAllWallpapers().firstOrNull() ?: emptyList()
        if (existingWallpapers.isEmpty()) {
            val starterWallpapers = listOf(
                // Minimal
                WallpaperEntity(
                    title = "Silent Waves",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080&q=80",
                    liveType = null,
                    customColors = "#0a0a0c,#1b1b2f,#1f4068",
                    category = "Minimal",
                    isFavorite = true,
                    prompt = "Elegantly sculpted liquid glass forms drifting on a quiet void."
                ),
                WallpaperEntity(
                    title = "Ethereal Sand",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1604871000636-074fa5117945?w=1080&q=80",
                    liveType = null,
                    customColors = "#eec0c6,#7ee8fa",
                    category = "Minimal",
                    isFavorite = false,
                    prompt = "Calming minimalist backdrop with pastel sand curves."
                ),
                // Cosmic
                WallpaperEntity(
                    title = "Orion Nebula",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?w=1080&q=80",
                    liveType = null,
                    customColors = "#0d1b2a,#1b4332,#40916c",
                    category = "Cosmic",
                    isFavorite = true,
                    prompt = "Deep cosmos, vivid stellar gas formations and distant suns."
                ),
                WallpaperEntity(
                    title = "Andromeda Gateway",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?w=1080&q=80",
                    liveType = null,
                    customColors = "#020122,#0b3c5d,#328cc1",
                    category = "Cosmic",
                    isFavorite = false,
                    prompt = "Spiraling star clusters in cold shades of cobalt and indigo."
                ),
                // Nature
                WallpaperEntity(
                    title = "Enchanted Pines",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1502082553048-f009c37129b9?w=1080&q=80",
                    liveType = null,
                    customColors = "#1c2e24,#2d4a36,#416d51",
                    category = "Nature",
                    isFavorite = false,
                    prompt = "Foggy morning pine forest with golden sun rays piercing the mist."
                ),
                WallpaperEntity(
                    title = "Golden Coast line",
                    type = "STATIC",
                    uri = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1080&q=80",
                    liveType = null,
                    customColors = "#ffb347,#ffcc33",
                    category = "Nature",
                    isFavorite = true,
                    prompt = "Soft beach waves washing onto shore during a fiery warm sunset."
                ),
                // Generative Live
                WallpaperEntity(
                    title = "Quantum Fluid",
                    type = "LIVE_GENERATIVE",
                    uri = null,
                    liveType = "PLASMA",
                    customColors = "#120136,#03001e,#7303c0,#ec38bc",
                    category = "Generative Live",
                    isFavorite = true,
                    prompt = "Self-animating wave fields built in native Compose canvas rendering interactive touch ripples."
                ),
                WallpaperEntity(
                    title = "Stardust Swarm",
                    type = "LIVE_GENERATIVE",
                    uri = null,
                    liveType = "PARTICLES",
                    customColors = "#0f2027,#203a43,#2c5364,#00f2fe",
                    category = "Generative Live",
                    isFavorite = false,
                    prompt = "A gravitational swarm of bright dust particles that flock to your finger tip on touch."
                ),
                WallpaperEntity(
                    title = "Gravity Symphony",
                    type = "LIVE_GENERATIVE",
                    uri = null,
                    liveType = "ORBIT",
                    customColors = "#000000,#0e0121,#1f0c3d,#ff007f",
                    category = "Generative Live",
                    isFavorite = true,
                    prompt = "Interactive solar simulation mimicking gravitational orbit physics."
                ),
                WallpaperEntity(
                    title = "Cyber Digital Rain",
                    type = "LIVE_GENERATIVE",
                    uri = null,
                    liveType = "MATRIX",
                    customColors = "#000000,#0c1a0c,#00ff3c",
                    category = "Generative Live",
                    isFavorite = false,
                    prompt = "Vertical streams of cybernetic green code symbols dripping lazily down."
                )
            )
            for (wallpaper in starterWallpapers) {
                wallpaperDao.insertWallpaper(wallpaper)
            }
        }

        // Seed default schedule setting if absent
        val existingSchedule = wallpaperDao.getScheduleSettingsDirect()
        if (existingSchedule == null) {
            wallpaperDao.updateScheduleSettings(
                ScheduleEntity(
                    id = 1,
                    isAutoChangeEnabled = false,
                    intervalMinutes = 60,
                    selectedCategory = "All"
                )
            )
        }
    }
}
