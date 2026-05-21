package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeneratedWallpaper(
    val title: String,
    val primaryColor: String,
    val secondaryColor: String,
    val tertiaryColor: String,
    val quaternaryColor: String,
    val styleType: String, // "PLASMA", "PARTICLES", "ORBIT", "MATRIX" or "STATIC"
    val promptDescription: String,
    val customImageKeyword: String // Unsplash keyword like "neon nebula" or "sahara sunset"
)

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateWallpaperSettings(userPreference: String): GeneratedWallpaper = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is not configured. Falling back to local creative synthesis.")
            return@withContext createFallbackWallpaper(userPreference)
        }

        val systemPrompt = """
            You are an expert design director for "Aurora", a premium interactive wallpaper application.
            Based on the user's preference prompt, generate a beautiful original wallpaper configuration.
            You must return ONLY a JSON object that matches this strict scheme:
            {
               "title": "A short, elegant 2-3 word title",
               "primaryColor": "Hex color code e.g. #0a0e17",
               "secondaryColor": "Hex color code e.g. #1b3c59",
               "tertiaryColor": "Hex color code e.g. #4f0e8f",
               "quaternaryColor": "Hex color code e.g. #e0129a",
               "styleType": "Choose one of: PLASMA, PARTICLES, ORBIT, MATRIX or STATIC",
               "promptDescription": "A poetic, sensory 1-2 sentence description explaining the visual atmosphere and design choices.",
               "customImageKeyword": "A precise 2-word photographic keyword search term (e.g. 'misty mountains', 'cyber punk') used to pull a backing image if rendered as STATIC"
            }
            Do not include any Markdown tags, ```json blocks, or explanations outside of the JSON object.
        """.trimIndent()

        val requestBodyJson = JSONObject().apply {
            val contentsArr = org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "User preference: $userPreference")
                        })
                    })
                })
            }
            put("contents", contentsArr)
            
            val systemInstructionObj = JSONObject().apply {
                put("parts", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemPrompt)
                    })
                })
            }
            put("systemInstruction", systemInstructionObj)
            
            val generationConfigObj = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.8)
            }
            put("generationConfig", generationConfigObj)
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string()
                if (!response.isSuccessful || bodyStr == null) {
                    Log.e(TAG, "Gemini API failed with response code ${response.code}: $bodyStr")
                    return@withContext createFallbackWallpaper(userPreference)
                }

                // Extract text from candidates
                val jsonResponse = JSONObject(bodyStr)
                val candidates = jsonResponse.getJSONArray("candidates")
                val parts = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                val textResult = parts.getJSONObject(0).getString("text")

                val adapter = moshi.adapter(GeneratedWallpaper::class.java)
                val result = adapter.fromJson(textResult)
                if (result != null) {
                    return@withContext result
                } else {
                    return@withContext createFallbackWallpaper(userPreference)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception invoking Gemini AI. Fallback triggered.", e)
            return@withContext createFallbackWallpaper(userPreference)
        }
    }

    private fun createFallbackWallpaper(prompt: String): GeneratedWallpaper {
        val query = prompt.lowercase()
        return when {
            query.contains("neon") || query.contains("cyber") || query.contains("future") -> {
                GeneratedWallpaper(
                    title = "Cyber Neon Grid",
                    primaryColor = "#050510",
                    secondaryColor = "#0b1535",
                    tertiaryColor = "#00ffea",
                    quaternaryColor = "#ff007f",
                    styleType = "MATRIX",
                    promptDescription = "Synthesized aesthetic referencing neural digital lines built with futuristic teal and hot-magenta streams.",
                    customImageKeyword = "cyberpunk neon"
                )
            }
            query.contains("peace") || query.contains("calm") || query.contains("relax") || query.contains("nature") -> {
                GeneratedWallpaper(
                    title = "Serene Meadows",
                    primaryColor = "#112211",
                    secondaryColor = "#1a3a2a",
                    tertiaryColor = "#66bb6a",
                    quaternaryColor = "#81c784",
                    styleType = "PLASMA",
                    promptDescription = "Bespoke ambient nature fields styled with biological clover-greens and peaceful translucent airwaves.",
                    customImageKeyword = "foggy forest"
                )
            }
            query.contains("space") || query.contains("cosmos") || query.contains("gravity") || query.contains("star") -> {
                GeneratedWallpaper(
                    title = "Astral Orbit",
                    primaryColor = "#020208",
                    secondaryColor = "#0c0a1f",
                    tertiaryColor = "#7303c0",
                    quaternaryColor = "#03a9f4",
                    styleType = "ORBIT",
                    promptDescription = "Generative stellar trajectory charting dynamic planetary rings and dark stardust clusters.",
                    customImageKeyword = "cosmic space"
                )
            }
            else -> {
                // Creative catch-all
                GeneratedWallpaper(
                    title = "Aurora Dream",
                    primaryColor = "#0a0015",
                    secondaryColor = "#110030",
                    tertiaryColor = "#390099",
                    quaternaryColor = "#ff0054",
                    styleType = "PLASMA",
                    promptDescription = "Dynamic visual art field compiled using custom deep lavender and scarlet energy nodes.",
                    customImageKeyword = "abstract gradient"
                )
            }
        }
    }
}
