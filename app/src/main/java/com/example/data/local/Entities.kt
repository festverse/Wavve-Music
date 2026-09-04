package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "saved_tracks")
data class SavedTrack(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artworkUrl: String?,
    val streamUrl: String,
    val duration: Int,
    @ColumnInfo(name = "is_downloaded") val isDownloaded: Boolean = false,
    @ColumnInfo(name = "download_path") val downloadPath: String? = null
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String? = null,
    val coverArtUrl: String? = null
)

@Entity(tableName = "followed_artists")
data class FollowedArtist(
    @PrimaryKey val id: String,
    val name: String,
    val handle: String,
    val profilePictureUrl: String?
)
