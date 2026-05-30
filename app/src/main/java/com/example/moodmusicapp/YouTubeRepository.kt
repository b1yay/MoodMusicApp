package com.example.moodmusicapp

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YouTubeRepository {

    suspend fun searchByMood(mood: String, pageToken: String? = null): Pair<List<YouTubeItem>, String?> {
        val query = when (mood) {
            "Happy" -> "happy upbeat pop music"
            "Sad" -> "sad emotional acoustic music"
            "Angry" -> "intense rock metal music"
            "Chill" -> "lofi chill study beats"
            "Romantic" -> "romantic acoustic love music"
            else -> "$mood music"
        }

        Log.d("YouTubeRepository", "Searching YouTube for: $query")
        val response = apiService.searchVideos(query = query, pageToken = pageToken, apiKey = API_KEY)
        
        if (response.isSuccessful) {
            val body = response.body()
            val items = body?.items ?: emptyList()
            val nextToken = body?.nextPageToken
            Log.d("YouTubeRepository", "Found ${items.size} tracks")
            return Pair(items, nextToken)
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("YouTubeRepository", "API Error: ${response.code()} - $errorBody")
            throw Exception("YouTube API error")
        }
    }

    companion object {
        private const val API_KEY = "AIzaSyCNqv5sAD4fOF8kB5iSCpfnBa2hSvsev1o"
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

        private val apiService: YouTubeApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(YouTubeApiService::class.java)
        }
    }
}
