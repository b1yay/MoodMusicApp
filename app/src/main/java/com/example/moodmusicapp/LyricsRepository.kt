package com.example.moodmusicapp

import android.util.Log

object LyricsRepository {
    private val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    suspend fun getLyrics(artist: String, title: String): String? {
        return try {
            val encodedArtist = java.net.URLEncoder.encode(artist, "UTF-8").replace("+", "%20")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8").replace("+", "%20")
            val url = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"

            Log.d("LYRICS", "Fetching: $url")

            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val body = response.body?.string()
                val json = org.json.JSONObject(body ?: "{}")
                val lyrics = json.optString("lyrics")
                if (lyrics.isNotBlank()) lyrics else null
            } else null
        } catch (e: Exception) {
            Log.e("LYRICS", "Error: ${e.message}")
            null
        }
    }
}
