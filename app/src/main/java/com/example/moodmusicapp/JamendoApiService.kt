package com.example.moodmusicapp

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JamendoApiService {
    @GET("tracks")
    suspend fun searchTracks(
        @Query("client_id") clientId: String,
        @Query("tags") tags: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 10,
        @Query("include") include: String = "musicinfo",
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("order") order: String = "popularity_total"
    ): Response<JamendoResponse>
}
