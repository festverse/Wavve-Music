package com.example.network

import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {
    @GET("tracks/?format=json")
    suspend fun getTracks(
        @Query("client_id") clientId: String,
        @Query("limit") limit: Int,
        @Query("search") search: String? = null,
        @Query("boost") boost: String = "popularity_month"
    ): JamendoResponse
}
