package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    // Saved Tracks (Downloads)
    @Query("SELECT * FROM saved_tracks")
    fun getAllSavedTracks(): Flow<List<SavedTrack>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: SavedTrack)

    @Query("DELETE FROM saved_tracks WHERE id = :id")
    suspend fun deleteTrack(id: String)

    // Playlists
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    // Followed Artists
    @Query("SELECT * FROM followed_artists")
    fun getFollowedArtists(): Flow<List<FollowedArtist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowedArtist(artist: FollowedArtist)
}
