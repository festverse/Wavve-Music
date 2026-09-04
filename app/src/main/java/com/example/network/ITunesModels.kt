package com.example.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ITunesResponse(
    val resultCount: Int,
    val results: List<ITunesTrack>
)

@JsonClass(generateAdapter = true)
data class ITunesTrack(
    val artistId: Long?,
    val trackId: Long?,
    val artistName: String?,
    val trackName: String?,
    val previewUrl: String?,
    val artworkUrl100: String?,
    val trackTimeMillis: Long?
)

@JsonClass(generateAdapter = true)
data class ITunesArtistResponse(
    val resultCount: Int,
    val results: List<ITunesArtist>
)

@JsonClass(generateAdapter = true)
data class ITunesArtist(
    val artistId: Long?,
    val artistName: String?,
    val artistLinkUrl: String?
)
