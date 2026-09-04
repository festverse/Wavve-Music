package com.example.data
import kotlinx.coroutines.async

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.network.NetworkModule
import com.example.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.buffer
import okio.sink

class MusicRepository(private val libraryDao: LibraryDao) {

    val savedTracks: Flow<List<MediaItem>> = libraryDao.getAllSavedTracks().map { tracks ->
        tracks.map { savedTrackToMediaItem(it) }
    }
    val playlists: Flow<List<Playlist>> = libraryDao.getAllPlaylists()
    val followedArtists: Flow<List<FollowedArtist>> = libraryDao.getFollowedArtists()

    suspend fun saveTrackForOffline(track: MediaItem) = withContext(Dispatchers.IO) {
        val savedTrack = SavedTrack(
            id = track.mediaId,
            title = track.mediaMetadata.title?.toString() ?: "Unknown",
            artist = track.mediaMetadata.artist?.toString() ?: "Unknown",
            artworkUrl = track.mediaMetadata.artworkUri?.toString(),
            streamUrl = track.requestMetadata.mediaUri?.toString() ?: "",
            duration = 0, // Placeholder
            isDownloaded = true
        )
        libraryDao.insertTrack(savedTrack)
    }

    suspend fun addMockLibraryData() = withContext(Dispatchers.IO) {
        // Simple mock population
        libraryDao.insertPlaylist(Playlist(name = "Chill Vibes", description = "Late night focus"))
        libraryDao.insertPlaylist(Playlist(name = "Gym Mix", description = "High energy EDM"))
        libraryDao.insertFollowedArtist(FollowedArtist("1", "Skrillex", "skrillex", "https://images.unsplash.com/photo-1493225255756-d9584f8606e9?q=80&w=200&auto=format&fit=crop"))
    }

    private fun savedTrackToMediaItem(track: SavedTrack): MediaItem {
        val uri = if (track.isDownloaded && !track.downloadPath.isNullOrEmpty()) {
            Uri.fromFile(java.io.File(track.downloadPath))
        } else {
            Uri.parse(track.streamUrl)
        }
        val artworkUri = track.artworkUrl?.let { Uri.parse(it) }

        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri)
            .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(Uri.parse(track.streamUrl)).build()) // Always keep original stream URL for lyrics
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setArtworkUri(artworkUri)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }

    suspend fun downloadAndSaveTrack(track: MediaItem, context: android.content.Context) = withContext(Dispatchers.IO) {
        val streamUrl = track.requestMetadata.mediaUri?.toString() ?: throw Exception("No stream URL")
        val fileName = "${track.mediaId}.mp3"
        val downloadsDir = java.io.File(context.filesDir, "downloads")
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        
        val file = java.io.File(downloadsDir, fileName)
        
        if (!file.exists()) {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder().url(streamUrl).build()
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val body = response.body
                if (body != null) {
                    val sink = file.sink().buffer()
                    sink.writeAll(body.source())
                    sink.close()
                } else {
                    throw Exception("Empty response body")
                }
            } else {
                throw Exception("HTTP ${response.code}")
            }
        }
        
        val savedTrack = SavedTrack(
            id = track.mediaId,
            title = track.mediaMetadata.title?.toString() ?: "Unknown",
            artist = track.mediaMetadata.artist?.toString() ?: "Unknown",
            artworkUrl = track.mediaMetadata.artworkUri?.toString(),
            streamUrl = streamUrl,
            duration = 0,
            isDownloaded = true,
            downloadPath = file.absolutePath
        )
        libraryDao.insertTrack(savedTrack)
    }

    suspend fun removeDownloadedTrack(trackId: String, context: android.content.Context) = withContext(Dispatchers.IO) {
        val fileName = "${trackId}.mp3"
        val downloadsDir = java.io.File(context.filesDir, "downloads")
        val file = java.io.File(downloadsDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        libraryDao.deleteTrack(trackId)
    }

    private val JAMENDO_CLIENT_ID = "19a09197"

    suspend fun getTopCharts(): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val itunesDeferred = async { 
                try {
                    NetworkModule.api.searchTracks(term = "pop", limit = 25).results.mapNotNull { mapToMediaItem(it) }
                } catch (e: Exception) { emptyList() }
            }
            
            val jamendoDeferred = async {
                try {
                    NetworkModule.jamendoApi.getTracks(clientId = JAMENDO_CLIENT_ID, limit = 25).results.mapNotNull { mapJamendoTrack(it) }
                } catch (e: Exception) { emptyList() }
            }

            val combined = mutableListOf<MediaItem>()
            combined.addAll(itunesDeferred.await())
            combined.addAll(jamendoDeferred.await())
            combined.shuffled() // Mix them up
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchTracks(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val itunesDeferred = async {
                try {
                    NetworkModule.api.searchTracks(term = query, limit = 20).results.mapNotNull { mapToMediaItem(it) }
                } catch (e: Exception) { emptyList() }
            }
            
            val jamendoDeferred = async {
                try {
                    NetworkModule.jamendoApi.getTracks(clientId = JAMENDO_CLIENT_ID, search = query, limit = 20).results.mapNotNull { mapJamendoTrack(it) }
                } catch (e: Exception) { emptyList() }
            }
            
            val combined = mutableListOf<MediaItem>()
            val jamendoTracks = jamendoDeferred.await()
            val itunesTracks = itunesDeferred.await()
            
            // Add some from Jamendo and some from iTunes if both exist
            val maxLen = maxOf(jamendoTracks.size, itunesTracks.size)
            for (i in 0 until maxLen) {
                if (i < jamendoTracks.size) combined.add(jamendoTracks[i])
                if (i < itunesTracks.size) combined.add(itunesTracks[i])
            }
            combined
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchArtists(query: String): List<com.example.network.ITunesArtist> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkModule.api.searchArtists(term = query)
            response.results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getArtistTracks(artistId: Long): List<MediaItem> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkModule.api.lookupArtistTracks(artistId = artistId)
            response.results.mapNotNull { mapToMediaItem(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun mapToMediaItem(track: com.example.network.ITunesTrack): MediaItem? {
        if (track.trackId == null || track.trackName == null || track.artistName == null || track.previewUrl == null) return null
        
        val uri = Uri.parse(track.previewUrl)
        // iTunes API provides 100x100 by default. We replace it with 1000x1000 for high quality.
        val artworkUri = track.artworkUrl100?.let { 
            Uri.parse(it.replace("100x100bb.jpg", "1000x1000bb.jpg")) 
        }

        return MediaItem.Builder()
            .setMediaId(track.trackId.toString())
            .setUri(uri)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(uri)
                    .build()
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.trackName)
                    .setArtist(track.artistName)
                    .setArtworkUri(artworkUri)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }

    private fun mapJamendoTrack(track: com.example.network.JamendoTrack): MediaItem? {
        if (track.id == null || track.name == null || track.artistName == null || track.audio == null) return null
        
        val uri = Uri.parse(track.audio)
        val artworkUri = track.image?.let { Uri.parse(it) }

        return MediaItem.Builder()
            .setMediaId("jamendo_${track.id}")
            .setUri(uri)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(uri)
                    .build()
            )
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.artistName)
                    .setArtworkUri(artworkUri)
                    .setIsPlayable(true)
                    .build()
            )
            .build()
    }

    suspend fun getLocalMedia(context: android.content.Context): List<MediaItem> = withContext(Dispatchers.IO) {
        val localTracks = mutableListOf<MediaItem>()
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.provider.MediaStore.Audio.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL)
        } else {
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            android.provider.MediaStore.Audio.Media._ID,
            android.provider.MediaStore.Audio.Media.TITLE,
            android.provider.MediaStore.Audio.Media.ARTIST
        )

        val selection = "${android.provider.MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${android.provider.MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Audio.Media.ARTIST)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn) ?: "Unknown Track"
                    val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                    
                    val contentUri = android.content.ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    
                    val mediaItem = MediaItem.Builder()
                        .setMediaId("local_$id")
                        .setUri(contentUri)
                        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(contentUri).build())
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(title)
                                .setArtist(artist)
                                .setIsPlayable(true)
                                .build()
                        )
                        .build()
                    localTracks.add(mediaItem)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        localTracks
    }
}
