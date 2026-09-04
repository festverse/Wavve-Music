package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MusicApiService {
    @GET("search")
    suspend fun searchTracks(
        @Query("term") term: String,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 20
    ): ITunesResponse

    @GET("search")
    suspend fun searchArtists(
        @Query("term") term: String,
        @Query("entity") entity: String = "musicArtist",
        @Query("limit") limit: Int = 10
    ): ITunesArtistResponse

    @GET("lookup")
    suspend fun lookupArtistTracks(
        @Query("id") artistId: Long,
        @Query("entity") entity: String = "song",
        @Query("limit") limit: Int = 30
    ): ITunesResponse
}
