package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey val id: Int = 1,
    val isAutoChangeEnabled: Boolean = false,
    val intervalMinutes: Int = 60, // 30, 60, 360, 1440
    val selectedCategory: String = "All",
    val lastChangedTimestamp: Long = 0L,
    val currentWallpaperId: Long = -1L
)
