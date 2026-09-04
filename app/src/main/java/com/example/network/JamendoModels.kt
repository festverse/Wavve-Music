package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JamendoResponse(
    val results: List<JamendoTrack> = emptyList()
)

@JsonClass(generateAdapter = true)
data class JamendoTrack(
    val id: String?,
    val name: String?,
    @Json(name = "artist_name") val artistName: String?,
    val image: String?,
    val audio: String?
)
