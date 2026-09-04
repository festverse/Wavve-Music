package com.example.ui

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.example.data.MusicRepository
import com.example.data.local.AppDatabase
import com.example.data.local.Playlist
import com.example.data.local.FollowedArtist
import com.example.service.WavvePlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private var browserFuture: ListenableFuture<MediaBrowser>? = null
    private var browser: MediaBrowser? = null
    
    private val database = AppDatabase.getDatabase(application)
    private val repository = MusicRepository(database.libraryDao())
    val settingsRepository = com.example.data.SettingsRepository(application)
    
    val currentTheme = settingsRepository.theme
    val currentAudioQuality = settingsRepository.audioQuality
    val currentDownloads = settingsRepository.downloads
val searchHistory = settingsRepository.searchHistory
    val recentlyPlayed = settingsRepository.recentlyPlayed

    private val _musicLibrary = MutableStateFlow<List<MediaItem>>(emptyList())
    val musicLibrary: StateFlow<List<MediaItem>> = _musicLibrary.asStateFlow()

    val localSavedTracks = repository.savedTracks
    val localPlaylists = repository.playlists
    val localFollowedArtists = repository.followedArtists

    private val _homePartyTracks = MutableStateFlow<List<MediaItem>>(emptyList())
    val homePartyTracks = _homePartyTracks.asStateFlow()

    private val _homeRomanticTracks = MutableStateFlow<List<MediaItem>>(emptyList())
    val homeRomanticTracks = _homeRomanticTracks.asStateFlow()
    
    private val _homePhonkTracks = MutableStateFlow<List<MediaItem>>(emptyList())
    val homePhonkTracks = _homePhonkTracks.asStateFlow()

    private val _localTracks = MutableStateFlow<List<MediaItem>>(emptyList())
    val localTracks = _localTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadLocalMedia(context: android.content.Context) {
        viewModelScope.launch {
            _localTracks.value = repository.getLocalMedia(context)
        }
    }

    val authManager = AuthManager(application)
    val firestoreManager = FirestoreManager()
    val lyricsManager = com.example.data.LyricsManager()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<com.google.firebase.auth.FirebaseUser?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Firestore state flows
    private val _userPlaylists = MutableStateFlow<List<PlaylistData>>(emptyList())
    val userPlaylists = _userPlaylists.asStateFlow()

    private val _followedArtists = MutableStateFlow<List<ArtistData>>(emptyList())
    val followedArtists = _followedArtists.asStateFlow()
    
    private var firestoreJob: kotlinx.coroutines.Job? = null

    init {
        com.google.firebase.auth.FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            _isLoggedIn.value = user != null
            _currentUser.value = user
            
            // Restart Firestore observation
            firestoreJob?.cancel()
            if (user != null) {
                firestoreJob = viewModelScope.launch {
                    launch { firestoreManager.getUserPlaylists().collect { _userPlaylists.value = it } }
                    launch { firestoreManager.getFollowedArtists().collect { _followedArtists.value = it } }
                }
            } else {
                _userPlaylists.value = emptyList()
                _followedArtists.value = emptyList()
            }
        }
    }

    fun refreshUser() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        // Force state flow emission by temporarily setting it to null (since the user object ref is same)
        _currentUser.value = null
        _currentUser.value = auth.currentUser
    }

    fun logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _currentLyrics = MutableStateFlow<List<com.example.data.LyricLine>>(emptyList())
    val currentLyrics: StateFlow<List<com.example.data.LyricLine>> = _currentLyrics.asStateFlow()

private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled: StateFlow<Boolean> = _shuffleModeEnabled.asStateFlow()

    private val _queueItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val queueItems: StateFlow<List<MediaItem>> = _queueItems.asStateFlow()

    fun initialize(context: Context) {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, WavvePlayerService::class.java)
        )

        browserFuture = MediaBrowser.Builder(context, sessionToken).buildAsync()
        browserFuture?.addListener(
            {
                browser = browserFuture?.get()
                setupBrowserListener()
                startPositionTracker()
                loadTrendingMusic()
                viewModelScope.launch { repository.addMockLibraryData() }
                
                browser?.let { b ->
                    if (b.mediaItemCount == 0) {
                        val savedState = settingsRepository.loadPlaybackState()
                        if (savedState != null) {
                            b.setMediaItems(savedState.queue, savedState.index, savedState.position)
                            b.prepare()
                            _currentMediaItem.value = savedState.queue.getOrNull(savedState.index)
                            _queueItems.value = savedState.queue
                            _currentPosition.value = savedState.position
                        }
                    }
                }
            },
            MoreExecutors.directExecutor()
        )
    }
    
    fun loadTrendingMusic() {
        viewModelScope.launch {
            _isLoading.value = true
            val tracks = repository.getTopCharts()
            _musicLibrary.value = tracks
            
            launch { _homePartyTracks.value = repository.searchTracks("party") }
            launch { _homeRomanticTracks.value = repository.searchTracks("romantic") }
            launch { _homePhonkTracks.value = repository.searchTracks("phonk") }

            _isLoading.value = false
            
            // Auto-load the playlist into the browser if it's empty
            browser?.let { b ->
                if (b.mediaItemCount == 0 && tracks.isNotEmpty()) {
                    b.setMediaItems(tracks)
                    b.prepare()
                }
            }
        }
    }

    private fun setupBrowserListener() {
        val currentBrowser = browser ?: return
        
        currentBrowser.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                _currentMediaItem.value = mediaItem
                _duration.value = currentBrowser?.duration?.coerceAtLeast(0L) ?: 0L
                mediaItem?.let {
                    settingsRepository.addRecentlyPlayed(it)
                }
                saveCurrentState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (!isPlaying) saveCurrentState()
            }
            
            override fun onRepeatModeChanged(repeatMode: Int) {
                _repeatMode.value = repeatMode
            }
            
override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _shuffleModeEnabled.value = shuffleModeEnabled
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                browser?.let {
                    val q = mutableListOf<MediaItem>()
                    for (i in 0 until it.mediaItemCount) {
                        q.add(it.getMediaItemAt(i))
                    }
                    _queueItems.value = q
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                _duration.value = currentBrowser.duration.coerceAtLeast(0L)
            }
        })
        
_currentMediaItem.value = currentBrowser.currentMediaItem
        _isPlaying.value = currentBrowser.isPlaying
_repeatMode.value = currentBrowser.repeatMode
        _shuffleModeEnabled.value = currentBrowser.shuffleModeEnabled
        _duration.value = currentBrowser.duration.coerceAtLeast(0L)
        val initialQueue = mutableListOf<MediaItem>()
        for (i in 0 until currentBrowser.mediaItemCount) {
            initialQueue.add(currentBrowser.getMediaItemAt(i))
        }
        _queueItems.value = initialQueue
    }

    private fun startPositionTracker() {
        viewModelScope.launch {
            var tickCount = 0
            while (true) {
                browser?.let {
                    if (it.isPlaying) {
                        _currentPosition.value = it.currentPosition.coerceAtLeast(0L)
                        _duration.value = it.duration.coerceAtLeast(0L)
                        
                        // Save the state periodically so it's not lost on abrupt kills
                        if (tickCount % 100 == 0) { // Every 5 seconds (50ms * 100 = 5000ms)
                            saveCurrentState()
                        }
                    }
                }
                tickCount++
                delay(50L) // Polling 20 times a second for smooth UI
            }
        }
    }

    private val _searchResults = MutableStateFlow<List<MediaItem>>(emptyList())
    val searchResults: StateFlow<List<MediaItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchHistory() {
        settingsRepository.clearSearchHistory()
    }
    
    fun performSearch() {
        val query = _searchQuery.value
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            if (_searchQuery.value.isNotBlank()) {
                settingsRepository.addSearchHistory(_searchQuery.value)
            }
            _isSearching.value = true
            val results = repository.searchTracks(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    fun searchByGenre(genre: String) {
        _searchQuery.value = genre // Set the text field to the genre name
        viewModelScope.launch {
            if (_searchQuery.value.isNotBlank()) {
                settingsRepository.addSearchHistory(_searchQuery.value)
            }
            _isSearching.value = true
            val results = repository.searchTracks(genre)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    suspend fun searchTracksForDetail(query: String): List<MediaItem> {
        return repository.searchTracks(query)
    }

    fun playTrack(mediaItem: MediaItem, useSearchResultsContext: Boolean = false) {
        val currentBrowser = browser ?: return
        
        // Automatically detect which library the track belongs to
        val library = if (useSearchResultsContext) {
            _searchResults.value 
        } else if (_localTracks.value.any { it.mediaId == mediaItem.mediaId }) {
            _localTracks.value
        } else if (_musicLibrary.value.any { it.mediaId == mediaItem.mediaId }) {
            _musicLibrary.value
        } else {
            // Check if it's in the current queue (for recently played or queue items)
            val queue = mutableListOf<MediaItem>()
            val browser = currentBrowser
            for (i in 0 until browser.mediaItemCount) {
                queue.add(browser.getMediaItemAt(i))
            }
            if (queue.any { it.mediaId == mediaItem.mediaId }) queue else listOf(mediaItem)
        }
        
        if (library.isNotEmpty()) {
            val index = library.indexOfFirst { it.mediaId == mediaItem.mediaId }
            if (index != -1) {
                currentBrowser.setMediaItems(library, index, 0)
                currentBrowser.prepare()
                currentBrowser.play()
                _currentMediaItem.value = mediaItem
                
                // Fetch lyrics
                _currentLyrics.value = emptyList()
                viewModelScope.launch {
                    val title = mediaItem.mediaMetadata.title?.toString() ?: ""
                    val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
                    if (title.isNotBlank()) {
                        val lyrics = lyricsManager.fetchLyrics(title, artist)
                        _currentLyrics.value = lyrics
                    }
                }
                return
            }
        }
        
        // Fallback
        currentBrowser.setMediaItem(mediaItem)
        currentBrowser.prepare()
        currentBrowser.play()
        
        _currentLyrics.value = emptyList()
        viewModelScope.launch {
            val title = mediaItem.mediaMetadata.title?.toString() ?: ""
            val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
            if (title.isNotBlank()) {
                val lyrics = lyricsManager.fetchLyrics(title, artist)
                _currentLyrics.value = lyrics
            }
        }
    }

    fun play() {
        browser?.play()
    }

    fun pause() {
        browser?.pause()
    }

fun toggleRepeatMode() {
        browser?.let {
            val nextMode = when (it.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
            it.repeatMode = nextMode
        }
    }

    fun toggleShuffleMode() {
        browser?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun addToQueue(mediaItem: MediaItem) {
        browser?.addMediaItem(mediaItem)
    }

fun clearQueue() {
        browser?.clearMediaItems()
    }

    fun removeFromQueue(mediaItem: MediaItem) {
        browser?.let { b ->
            for (i in 0 until b.mediaItemCount) {
                if (b.getMediaItemAt(i).mediaId == mediaItem.mediaId) {
                    b.removeMediaItem(i)
                    break
                }
            }
        }
    }
    
    fun playQueueItem(index: Int) {
        browser?.seekToDefaultPosition(index)
        browser?.play()
    }
    
    fun skipToNext() {
        browser?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        browser?.seekToPreviousMediaItem()
    }
    
    fun seekTo(position: Long) {
        browser?.seekTo(position)
        _currentPosition.value = position
    }

    fun downloadTrack(track: MediaItem, context: Context) {
        val isWifiOnly = currentDownloads.value == "Wi-Fi Only"
        if (isWifiOnly) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            val isWifi = capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true
            if (!isWifi) {
                android.widget.Toast.makeText(context, "Downloads restricted to Wi-Fi only in Settings", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        android.widget.Toast.makeText(context, "Downloading ${track.mediaMetadata.title}...", android.widget.Toast.LENGTH_SHORT).show()
        viewModelScope.launch {
            try {
                repository.downloadAndSaveTrack(track, context)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Downloaded ${track.mediaMetadata.title}", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Failed to download", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun removeDownloadedTrack(trackId: String, context: Context) {
        viewModelScope.launch {
            try {
                repository.removeDownloadedTrack(trackId, context)
                android.widget.Toast.makeText(context, "Download removed", android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                android.widget.Toast.makeText(context, "Failed to remove download", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveCurrentState() {
        browser?.let { b ->
            val queue = mutableListOf<androidx.media3.common.MediaItem>()
            for (i in 0 until b.mediaItemCount) {
                queue.add(b.getMediaItemAt(i))
            }
            if (queue.isNotEmpty()) {
                settingsRepository.savePlaybackState(queue, b.currentMediaItemIndex, b.currentPosition)
            }
        }
    }

    override fun onCleared() {
        saveCurrentState()

        super.onCleared()
        browserFuture?.let { MediaBrowser.releaseFuture(it) }
    }
}
