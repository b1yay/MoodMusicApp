package com.example.moodmusicapp

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JamendoRepository {

    suspend fun getTracksByMood(mood: String, offset: Int = 0): List<JamendoTrack> {
        val tag = when (mood) {
            "Happy" -> "happy"
            "Sad" -> "sad"
            "Angry" -> "rock"
            "Chill" -> "chill"
            "Romantic" -> "romantic"
            else -> "pop"
        }

        return try {
            val response = apiService.searchTracks(clientId = CLIENT_ID, tags = tag, offset = offset)
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                Log.e("JAMENDO", "API Error: ${response.code()} ${response.message()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("JAMENDO", "Request failed: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun searchByQuery(query: String): List<JamendoTrack> {
        return try {
            val response = apiService.searchTracks(
                clientId = CLIENT_ID,
                tags = query,
                nameSearch = query
            )
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            Log.e("JAMENDO", "Search error: ${e.message}")
            emptyList()
        }
    }

    companion object {
        private const val CLIENT_ID = "b97b7009"
        private const val BASE_URL = "https://api.jamendo.com/v3.0/"

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private val apiService: JamendoApiService by lazy {
            retrofit.create(JamendoApiService::class.java)
        }
    }
}
