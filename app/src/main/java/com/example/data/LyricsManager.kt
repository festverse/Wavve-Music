package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

data class LyricLine(
    val timeMs: Long,
    val text: String
)

class LyricsManager {
    private val client = OkHttpClient()

    private fun cleanTitle(title: String): String {
        var clean = title.replace(Regex("\\(.*?\\)"), "")
        clean = clean.replace(Regex("\\[.*?\\]"), "")
        if (clean.contains("-")) {
            clean = clean.substringBeforeLast("-")
        }
        return clean.trim()
    }

    suspend fun fetchLyrics(trackName: String, artistName: String): List<LyricLine> = withContext(Dispatchers.IO) {
        if (trackName.isBlank() || artistName.isBlank()) return@withContext emptyList()
        
        try {
            val encodedTrack = URLEncoder.encode(trackName, "UTF-8")
            val encodedArtist = URLEncoder.encode(artistName, "UTF-8")
            
            // Try exact match first
            var url = "https://lrclib.net/api/search?track_name=$encodedTrack&artist_name=$encodedArtist"
            var results = trySearch(url)
            
            // Fallback 1: Broad search query
            if (results == null) {
                val q = URLEncoder.encode("$trackName $artistName", "UTF-8")
                url = "https://lrclib.net/api/search?q=$q"
                results = trySearch(url)
            }
            
            // Fallback 2: Cleaned title search
            if (results == null) {
                val cleanedTitle = cleanTitle(trackName)
                if (cleanedTitle != trackName) {
                    val qClean = URLEncoder.encode("$cleanedTitle $artistName", "UTF-8")
                    url = "https://lrclib.net/api/search?q=$qClean"
                    results = trySearch(url)
                }
            }
            
            return@withContext results ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
    
    private fun trySearch(url: String): List<LyricLine>? {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Wavve Android App v1.0")
                .build()
                
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null
            
            val responseBody = response.body?.string() ?: return null
            if (responseBody.trim().isEmpty() || responseBody.trim() == "[]") return null
            
            val jsonArray = JSONArray(responseBody)
            
            // Priority 1: Synced lyrics
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.has("syncedLyrics") && !obj.isNull("syncedLyrics")) {
                    val syncedLyrics = obj.getString("syncedLyrics")
                    if (syncedLyrics.isNotBlank()) {
                        return parseLrc(syncedLyrics)
                    }
                }
            }
            
            // Priority 2: Plain lyrics fallback
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.has("plainLyrics") && !obj.isNull("plainLyrics")) {
                    val plainLyrics = obj.getString("plainLyrics")
                    if (plainLyrics.isNotBlank()) {
                        // Convert plain lyrics into fake 0ms LyricLines so the UI displays them
                        return plainLyrics.lines().map { LyricLine(0L, it) }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun parseLrc(lrc: String): List<LyricLine> {
        val lines = lrc.lines()
        val result = mutableListOf<LyricLine>()
        val timePattern = Regex("\\[(\\d{2}):(\\d{2}\\.\\d{2})\\](.*)")

        for (line in lines) {
            val match = timePattern.matchEntire(line.trim())
            if (match != null) {
                val minutes = match.groupValues[1].toLong()
                val seconds = match.groupValues[2].toDouble()
                val text = match.groupValues[3].trim()

                val timeMs = (minutes * 60 * 1000) + (seconds * 1000).toLong()
                if (text.isNotEmpty()) {
                    result.add(LyricLine(timeMs, text))
                }
            }
        }
        return result
    }
}