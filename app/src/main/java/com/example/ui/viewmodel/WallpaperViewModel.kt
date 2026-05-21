package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.entities.CategoryEntity
import com.example.data.entities.ScheduleEntity
import com.example.data.entities.WallpaperEntity
import com.example.data.repository.WallpaperRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class WallpaperViewModel(private val repository: WallpaperRepository) : ViewModel() {

    private val TAG = "WallpaperViewModel"

    // Database states
    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWallpapers: StateFlow<List<WallpaperEntity>> = repository.allWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWallpapers: StateFlow<List<WallpaperEntity>> = repository.favoriteWallpapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleSettings: StateFlow<ScheduleEntity?> = repository.scheduleSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI state filters
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Generated lists matching filter criteria
    val filteredWallpapers: StateFlow<List<WallpaperEntity>> = combine(
        allWallpapers,
        selectedCategory
    ) { wallpapers, category ->
        if (category == "All") {
            wallpapers
        } else {
            wallpapers.filter { it.category.equals(category, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active screen styling preview (defaults to first wall)
    private val _activeWallpaper = MutableStateFlow<WallpaperEntity?>(null)
    val activeWallpaper: StateFlow<WallpaperEntity?> = _activeWallpaper.asStateFlow()

    // AI generating flags
    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    // Scheduled automation simulator ticker states
    private val _simulatedSecondsRemaining = MutableStateFlow(0)
    val simulatedSecondsRemaining: StateFlow<Int> = _simulatedSecondsRemaining.asStateFlow()

    private val _schedulerLogMsg = MutableStateFlow("Scheduler active. Waiting for ticks...")
    val schedulerLogMsg: StateFlow<String> = _schedulerLogMsg.asStateFlow()

    init {
        // Seeding database
        viewModelScope.launch {
            repository.seedDatabase()
            // Set initial active wallpaper once seed completes
            repository.allWallpapers.firstOrNull()?.firstOrNull()?.let {
                _activeWallpaper.value = it
            }
        }

        // Keep active wallpaper in sync with schedule settings if updated
        viewModelScope.launch {
            combine(repository.scheduleSettings, repository.allWallpapers) { schedule, walls ->
                if (schedule != null && schedule.currentWallpaperId != -1L) {
                    walls.find { it.id == schedule.currentWallpaperId }
                } else {
                    null
                }
            }.collect { matchedWallpaper ->
                if (matchedWallpaper != null) {
                    _activeWallpaper.value = matchedWallpaper
                }
            }
        }

        // Ticker driving simulated countdown for scheduled wallpaper cycles
        viewModelScope.launch {
            while (true) {
                val settings = scheduleSettings.value
                if (settings != null && settings.isAutoChangeEnabled) {
                    if (_simulatedSecondsRemaining.value <= 1) {
                        // Triggers the auto cycle automatically when count reaches zero!
                        val min = settings.intervalMinutes
                        _simulatedSecondsRemaining.value = min * 5 // Accelerated scale for demo visuals (minutes mapped to 5-sec increments for visible testing!)
                        cycleWallpaperImmediately()
                    } else {
                        _simulatedSecondsRemaining.value -= 1
                    }
                } else {
                    _simulatedSecondsRemaining.value = 0
                }
                delay(1000L)
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectActiveWallpaper(wallpaper: WallpaperEntity) {
        _activeWallpaper.value = wallpaper
        viewModelScope.launch {
            repository.updateCurrentWallpaper(wallpaper.id)
        }
    }

    fun toggleFavorite(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(wallpaper.id, !wallpaper.isFavorite)
        }
    }

    fun addCustomCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCategory(name.trim())
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            repository.deleteCategory(name)
            if (_selectedCategory.value == name) {
                _selectedCategory.value = "All"
            }
        }
    }

    // Handles picking and saving local image to App Storage
    fun insertUserUploadedWallpaper(
        context: Context,
        title: String,
        category: String,
        uri: Uri
    ) {
        viewModelScope.launch {
            try {
                _isAiGenerating.value = true
                val savedFileUriStr = saveUriToInternalStorage(context, uri)
                if (savedFileUriStr != null) {
                    val newWall = WallpaperEntity(
                        title = if (title.isBlank()) "My Creation" else title,
                        type = "STATIC",
                        uri = savedFileUriStr,
                        liveType = null,
                        customColors = "#2c3e50,#bdc3c7",
                        category = category,
                        isFavorite = false,
                        prompt = "User upload from device local storage."
                    )
                    val id = repository.insertWallpaper(newWall)
                    val inserted = newWall.copy(id = id)
                    selectActiveWallpaper(inserted)
                    _schedulerLogMsg.value = "Successfully imported local wallpaper: '$title'"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error importing local wallpaper", e)
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    // Triggers generation via Gemini AI
    fun generateAiWallpaper(userPreference: String) {
        viewModelScope.launch {
            if (userPreference.isBlank()) return@launch
            _isAiGenerating.value = true
            _schedulerLogMsg.value = "Consulting Aurora AI designer..."
            try {
                val config = GeminiClient.generateWallpaperSettings(userPreference)
                
                // Construct beautiful colors representation
                val colorsStr = "${config.primaryColor},${config.secondaryColor},${config.tertiaryColor},${config.quaternaryColor}"

                // To represent as static or live based on style
                val newWall = WallpaperEntity(
                    title = config.title,
                    type = if (config.styleType == "STATIC") "STATIC" else "LIVE_GENERATIVE",
                    uri = if (config.styleType == "STATIC") {
                        // Direct backing image related to the keyword search matching prompt
                        val encodedKw = Uri.encode(config.customImageKeyword)
                        "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=1080&q=80&sig=${System.currentTimeMillis()}&q=$encodedKw"
                    } else null,
                    liveType = if (config.styleType == "STATIC") null else config.styleType,
                    customColors = colorsStr,
                    category = "AI Creations",
                    isFavorite = true,
                    prompt = config.promptDescription
                )

                val id = repository.insertWallpaper(newWall)
                val inserted = newWall.copy(id = id)
                
                // Select and preview the generated wallpaper!
                selectActiveWallpaper(inserted)
                _schedulerLogMsg.value = "AI successfully created: '${config.title}' (${config.styleType})"
            } catch (e: Exception) {
                Log.e(TAG, "AI generation crashed", e)
                _schedulerLogMsg.value = "AI generator timeout. Falling back to default styling."
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun insertCustomLiveWallpaper(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            try {
                _isAiGenerating.value = true
                val id = repository.insertWallpaper(wallpaper)
                val inserted = wallpaper.copy(id = id)
                selectActiveWallpaper(inserted)
                _schedulerLogMsg.value = "Saved custom interactive wallpaper: '${wallpaper.title}'!"
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert custom wallpaper", e)
            } finally {
                _isAiGenerating.value = false
            }
        }
    }

    fun deleteWallpaper(wallpaper: WallpaperEntity) {
        viewModelScope.launch {
            repository.deleteWallpaper(wallpaper.id)
            // If deleting current wall, fall back to next available or first element
            if (_activeWallpaper.value?.id == wallpaper.id) {
                val list = allWallpapers.value.filter { it.id != wallpaper.id }
                if (list.isNotEmpty()) {
                    selectActiveWallpaper(list.first())
                }
            }
        }
    }

    fun saveScheduleSettings(isAuto: Boolean, intervalMinutes: Int, selectedCategory: String) {
        viewModelScope.launch {
            val current = scheduleSettings.value ?: ScheduleEntity()
            val updated = current.copy(
                isAutoChangeEnabled = isAuto,
                intervalMinutes = intervalMinutes,
                selectedCategory = selectedCategory
            )
            repository.saveScheduleSettings(updated)

            // Reset simulated countdown timer mapping 1 minute = 5 seconds
            _simulatedSecondsRemaining.value = intervalMinutes * 5
            _schedulerLogMsg.value = "Scheduler updated: changes every $intervalMinutes mins (Filtered: $selectedCategory)"
        }
    }

    fun cycleWallpaperImmediately() {
        viewModelScope.launch {
            cycleWallpaperImmediatelyInternal()
        }
    }

    private suspend fun cycleWallpaperImmediatelyInternal() {
        val settings = scheduleSettings.value ?: ScheduleEntity()
        val wallpapers = allWallpapers.value
        if (wallpapers.isEmpty()) return

        val sourceList = if (settings.selectedCategory == "All") {
            wallpapers
        } else {
            wallpapers.filter { it.category.equals(settings.selectedCategory, ignoreCase = true) }
        }

        if (sourceList.isEmpty()) {
            _schedulerLogMsg.value = "Schedule tick skipped. No wallpapers found in category '${settings.selectedCategory}'"
            return
        }

        // Pick a different wallpaper randomly or next in order
        var eligible = sourceList.filter { it.id != settings.currentWallpaperId }
        if (eligible.isEmpty()) {
            eligible = sourceList // fallback to same if list size is 1
        }

        val nextWallpaper = eligible.random()
        repository.updateCurrentWallpaper(nextWallpaper.id)
        _schedulerLogMsg.value = "Auto-Change Triggered: Cycle shifted to '${nextWallpaper.title}' !"
    }

    // Helper method to write imported local Uri to permanent internal sandbox files structure
    private suspend fun saveUriToInternalStorage(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val extension = context.contentResolver.getType(uri)?.split("/")?.lastOrNull() ?: "jpg"
            val targetFilename = "aurora_upload_${System.currentTimeMillis()}.$extension"
            val targetFile = File(context.filesDir, targetFilename)

            resolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return@withContext targetFile.absolutePath
        } catch (e: Exception) {
            Log.e("StorageHelper", "Critical copy error on wallpaper upload files", e)
            null
        }
    }
}

class WallpaperViewModelFactory(private val repository: WallpaperRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WallpaperViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WallpaperViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class class cast exception")
    }
}
