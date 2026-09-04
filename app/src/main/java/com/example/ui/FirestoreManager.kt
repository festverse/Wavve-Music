package com.example.ui

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Models

data class TrackData(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val artworkUri: String = "",
    val mediaUri: String = "",
    val duration: Long = 0L
)

data class PlaylistData(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val isShared: Boolean = true,
    val tracks: List<TrackData> = emptyList()
)

data class ArtistData(
    val name: String = "",
    val imageUrl: String = ""
)

class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val TAG = "FirestoreManager"

    private fun getUserId(): String? = auth.currentUser?.uid

    // --- Playlists ---

    suspend fun createPlaylist(name: String, isShared: Boolean = true): Boolean {
        val uid = getUserId() ?: return false
        return try {
            val docRef = db.collection("users").document(uid).collection("playlists").document()
            val playlist = PlaylistData(
                id = docRef.id,
                name = name,
                ownerId = uid,
                isShared = isShared,
                tracks = emptyList()
            )
            docRef.set(playlist).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating playlist", e)
            false
        }
    }

    suspend fun addTrackToPlaylist(playlistId: String, track: MediaItem): Boolean {
        val uid = getUserId() ?: return false
        return try {
            val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
            
            val trackData = TrackData(
                id = track.mediaId,
                title = track.mediaMetadata.title?.toString() ?: "Unknown",
                artist = track.mediaMetadata.artist?.toString() ?: "Unknown",
                artworkUri = track.mediaMetadata.artworkUri?.toString() ?: "",
                mediaUri = track.localConfiguration?.uri?.toString() ?: ""
            )

            // We use an atomic array union to prevent overwriting
            docRef.update("tracks", com.google.firebase.firestore.FieldValue.arrayUnion(trackData)).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding track to playlist", e)
            false
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String): Boolean {
        val uid = getUserId() ?: return false
        return try {
            val docRef = db.collection("users").document(uid).collection("playlists").document(playlistId)
            val snapshot = docRef.get().await()
            val playlist = snapshot.toObject(PlaylistData::class.java) ?: return false
            
            // Find the track by ID and remove it using arrayRemove
            val trackToRemove = playlist.tracks.find { it.id == trackId }
            if (trackToRemove != null) {
                docRef.update("tracks", com.google.firebase.firestore.FieldValue.arrayRemove(trackToRemove)).await()
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error removing track from playlist", e)
            false
        }
    }

    suspend fun deletePlaylist(playlistId: String): Boolean {
        val uid = getUserId() ?: return false
        return try {
            db.collection("users").document(uid).collection("playlists").document(playlistId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting playlist", e)
            false
        }
    }


    suspend fun getSharedPlaylist(playlistId: String): PlaylistData? {
        return try {
            val querySnapshot = db.collectionGroup("playlists").whereEqualTo("id", playlistId).get().await()
            if (querySnapshot.isEmpty) null else querySnapshot.documents[0].toObject(PlaylistData::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching shared playlist", e)
            null
        }
    }

    fun getUserPlaylists(): Flow<List<PlaylistData>> = callbackFlow {
        val uid = getUserId()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid).collection("playlists")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val playlists = snapshot.toObjects(PlaylistData::class.java)
                    trySend(playlists)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- Followed Artists ---

    suspend fun followArtist(name: String, imageUrl: String = ""): Boolean {
        val uid = getUserId() ?: return false
        return try {
            val artistData = ArtistData(name = name, imageUrl = imageUrl)
            // Use artist name as document ID to ensure uniqueness and easy toggle
            val docId = name.replace("/", "_")
            db.collection("users").document(uid).collection("followedArtists").document(docId)
                .set(artistData).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error following artist", e)
            false
        }
    }

    suspend fun unfollowArtist(name: String): Boolean {
        val uid = getUserId() ?: return false
        return try {
            val docId = name.replace("/", "_")
            db.collection("users").document(uid).collection("followedArtists").document(docId)
                .delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error unfollowing artist", e)
            false
        }
    }

    fun getFollowedArtists(): Flow<List<ArtistData>> = callbackFlow {
        val uid = getUserId()
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("users").document(uid).collection("followedArtists")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toObjects(ArtistData::class.java))
                }
            }
        awaitClose { listener.remove() }
    }

    // --- Utility to convert TrackData to MediaItem ---
    fun mapTrackDataToMediaItem(data: TrackData): MediaItem {
        return MediaItem.Builder()
            .setMediaId(data.id)
            .setUri(data.mediaUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(data.title)
                    .setArtist(data.artist)
                    .setArtworkUri(if (data.artworkUri.isNotEmpty()) Uri.parse(data.artworkUri) else null)
                    .build()
            )
            .build()
    }
}
