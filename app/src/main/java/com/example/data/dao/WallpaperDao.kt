package com.example.data.dao

import androidx.room.*
import com.example.data.entities.WallpaperEntity
import com.example.data.entities.CategoryEntity
import com.example.data.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    // --- Wallpapers ---
    @Query("SELECT * FROM wallpapers ORDER BY timestamp DESC")
    fun getAllWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE category = :categoryName ORDER BY timestamp DESC")
    fun getWallpapersByCategory(categoryName: String): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteWallpapers(): Flow<List<WallpaperEntity>>

    @Query("SELECT * FROM wallpapers WHERE id = :id")
    suspend fun getWallpaperById(id: Long): WallpaperEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallpaper(wallpaper: WallpaperEntity): Long

    @Query("UPDATE wallpapers SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFav: Boolean)

    @Query("DELETE FROM wallpapers WHERE id = :id")
    suspend fun deleteWallpaperById(id: Long)

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY isCustom ASC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE name = :name")
    suspend fun deleteCategory(name: String)

    // --- Schedule Settings ---
    @Query("SELECT * FROM schedule WHERE id = 1")
    fun getScheduleSettings(): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedule WHERE id = 1")
    suspend fun getScheduleSettingsDirect(): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateScheduleSettings(settings: ScheduleEntity)
}
