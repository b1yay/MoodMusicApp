package com.example.moodmusicapp

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YouTubeRepository {

    suspend fun searchByMood(mood: String, pageToken: String? = null): Pair<List<YouTubeItem>, String?> {
        val query = when (mood) {
            "Happy" -> "happy pop hits songs"
            "Sad" -> "sad emotional songs"
            "Angry" -> "rock angry intense songs"
            "Chill" -> "lofi chill beats music"
            "Romantic" -> "romantic love songs"
            else -> mood
        }

        Log.d("YouTubeRepository", "Starting search for query: $query, pageToken: $pageToken")
        val response = apiService.searchVideos(query = query, pageToken = pageToken, apiKey = API_KEY)
        
        if (response.isSuccessful) {
            val body = response.body()
            val items = body?.items ?: emptyList()
            val nextToken = body?.nextPageToken
            Log.d("YouTubeRepository", "Search successful. Items found: ${items.size}, nextToken: $nextToken")
            return Pair(items, nextToken)
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e("YouTubeRepository", "API Error: ${response.code()} ${response.message()} - $errorBody")
            throw Exception("YouTube API Error: ${response.code()}")
        }
    }

    companion object {
        private const val API_KEY = "AIzaSyCNqv5sAD4fOF8kB5iSCpfnBa2hSvsev1o"
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

        private val apiService: YouTubeApiService by lazy {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
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
