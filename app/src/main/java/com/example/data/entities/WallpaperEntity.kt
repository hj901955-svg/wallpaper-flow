package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
data class WallpaperEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // "STATIC", "LIVE_GENERATIVE", "AI_GENERATED"
    val uri: String?, // Local file path or content uri or external generated image URI
    val liveType: String?, // "PLASMA", "PARTICLES", "ORBIT", "MATRIX"
    val customColors: String?, // Comma-separated hex values
    val category: String, // Category name
    val isFavorite: Boolean = false,
    val prompt: String? = null, // AI Prompt if generated
    val timestamp: Long = System.currentTimeMillis()
)
