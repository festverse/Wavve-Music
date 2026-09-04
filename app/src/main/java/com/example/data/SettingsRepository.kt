package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wavve_settings", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(prefs.getString("theme", "System") ?: "System")
    val theme: StateFlow<String> = _theme.asStateFlow()

    private val _audioQuality = MutableStateFlow(prefs.getString("audio_quality", "High (320kbps)") ?: "High (320kbps)")
    val audioQuality: StateFlow<String> = _audioQuality.asStateFlow()

    private val _downloads = MutableStateFlow(prefs.getString("downloads", "Wi-Fi Only") ?: "Wi-Fi Only")
    val downloads: StateFlow<String> = _downloads.asStateFlow()

    // Search History
    private val _searchHistory = MutableStateFlow<List<String>>(loadSearchHistory())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()
    
    private fun loadSearchHistory(): List<String> {
        val json = prefs.getString("search_history", "[]") ?: "[]"
        try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) list.add(array.getString(i))
            return list
        } catch(e: Exception) { return emptyList() }
    }

    fun addSearchHistory(query: String) {
        if (query.isBlank()) return
        val current = _searchHistory.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        if (current.size > 9) current.removeLast()
        val array = org.json.JSONArray(current)
        prefs.edit().putString("search_history", array.toString()).apply()
        _searchHistory.value = current
    }

    fun clearSearchHistory() {
        prefs.edit().putString("search_history", "[]").apply()
        _searchHistory.value = emptyList()
    }

    // Recently Played
    private val _recentlyPlayed = MutableStateFlow<List<androidx.media3.common.MediaItem>>(loadRecentlyPlayed())
    val recentlyPlayed = _recentlyPlayed.asStateFlow()

    private fun loadRecentlyPlayed(): List<androidx.media3.common.MediaItem> {
        val json = prefs.getString("recently_played", "[]") ?: "[]"
        try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<androidx.media3.common.MediaItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val streamUrl = obj.optString("streamUrl", "")
                val uri = if (streamUrl.isNotEmpty()) android.net.Uri.parse(streamUrl) else android.net.Uri.EMPTY
                val artworkUrl = obj.optString("artworkUrl", "")
                val artworkUri = if (artworkUrl.isNotEmpty()) android.net.Uri.parse(artworkUrl) else null
                
                val item = androidx.media3.common.MediaItem.Builder()
                    .setMediaId(obj.getString("id"))
                    .setUri(uri)
                    .setRequestMetadata(androidx.media3.common.MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(obj.getString("title"))
                            .setArtist(obj.getString("artist"))
                            .setArtworkUri(artworkUri)
                            .setIsPlayable(true)
                            .build()
                    ).build()
                list.add(item)
            }
            return list
        } catch(e: Exception) { return emptyList() }
    }

    fun addRecentlyPlayed(track: androidx.media3.common.MediaItem) {
        val current = _recentlyPlayed.value.toMutableList()
        current.removeAll { it.mediaId == track.mediaId }
        current.add(0, track)
        if (current.size > 20) current.removeLast()
        
        val array = org.json.JSONArray()
        for (item in current) {
            val obj = org.json.JSONObject()
            obj.put("id", item.mediaId)
            obj.put("title", item.mediaMetadata.title?.toString() ?: "Unknown")
            obj.put("artist", item.mediaMetadata.artist?.toString() ?: "Unknown")
            
            val streamUrl = item.requestMetadata?.mediaUri?.toString() ?: item.localConfiguration?.uri?.toString() ?: ""
            obj.put("streamUrl", streamUrl)
            
            if (item.mediaMetadata.artworkUri != null) {
                obj.put("artworkUrl", item.mediaMetadata.artworkUri.toString())
            }
            array.put(obj)
        }
        prefs.edit().putString("recently_played", array.toString()).apply()
        _recentlyPlayed.value = current
    }

    data class PlaybackStateData(
        val queue: List<androidx.media3.common.MediaItem>,
        val index: Int,
        val position: Long
    )

    fun savePlaybackState(queue: List<androidx.media3.common.MediaItem>, index: Int, position: Long) {
        val array = org.json.JSONArray()
        for (item in queue) {
            val obj = org.json.JSONObject()
            obj.put("id", item.mediaId)
            obj.put("title", item.mediaMetadata.title?.toString() ?: "Unknown")
            obj.put("artist", item.mediaMetadata.artist?.toString() ?: "Unknown")
            
            val streamUrl = item.requestMetadata?.mediaUri?.toString() ?: item.localConfiguration?.uri?.toString() ?: ""
            obj.put("streamUrl", streamUrl)
            
            if (item.mediaMetadata.artworkUri != null) {
                obj.put("artworkUrl", item.mediaMetadata.artworkUri.toString())
            }
            array.put(obj)
        }
        prefs.edit()
            .putString("saved_queue", array.toString())
            .putInt("saved_index", index)
            .putLong("saved_position", position)
            .apply()
    }

    fun loadPlaybackState(): PlaybackStateData? {
        val json = prefs.getString("saved_queue", "[]") ?: "[]"
        if (json == "[]") return null
        
        try {
            val array = org.json.JSONArray(json)
            val list = mutableListOf<androidx.media3.common.MediaItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val streamUrl = obj.optString("streamUrl", "")
                val uri = if (streamUrl.isNotEmpty()) android.net.Uri.parse(streamUrl) else android.net.Uri.EMPTY
                val artworkUrl = obj.optString("artworkUrl", "")
                val artworkUri = if (artworkUrl.isNotEmpty()) android.net.Uri.parse(artworkUrl) else null
                
                val item = androidx.media3.common.MediaItem.Builder()
                    .setMediaId(obj.getString("id"))
                    .setUri(uri)
                    .setRequestMetadata(androidx.media3.common.MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(obj.getString("title"))
                            .setArtist(obj.getString("artist"))
                            .setArtworkUri(artworkUri)
                            .setIsPlayable(true)
                            .build()
                    ).build()
                list.add(item)
            }
            if (list.isEmpty()) return null
            
            val index = prefs.getInt("saved_index", 0)
            val position = prefs.getLong("saved_position", 0L)
            
            return PlaybackStateData(list, index, position)
        } catch(e: Exception) { return null }
    }

    fun setTheme(newTheme: String) {
        prefs.edit().putString("theme", newTheme).apply()
        _theme.value = newTheme
    }

    fun setAudioQuality(quality: String) {
        prefs.edit().putString("audio_quality", quality).apply()
        _audioQuality.value = quality
    }

    fun setDownloads(downloadSetting: String) {
        prefs.edit().putString("downloads", downloadSetting).apply()
        _downloads.value = downloadSetting
    }
}
