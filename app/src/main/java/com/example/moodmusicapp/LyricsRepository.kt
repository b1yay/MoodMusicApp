package com.example.moodmusicapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

object LyricsRepository {
    
    suspend fun getLyrics(artist: String, title: String): String? = withContext(Dispatchers.IO) {
        try {
            // Simple placeholder implementation or a real API call if available
            // For now, returning null or a placeholder to fix build
            // You can implement a real lyrics API like lyrics.ovh here
            val encodedArtist = java.net.URLEncoder.encode(artist, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            val urlString = "https://api.lyrics.ovh/v1/$encodedArtist/$encodedTitle"
            
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            if (connection.responseCode == 200) {
                val scanner = Scanner(connection.inputStream)
                val response = StringBuilder()
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine())
                }
                scanner.close()
                
                val json = JSONObject(response.toString())
                json.optString("lyrics").takeIf { it.isNotBlank() }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LYRICS", "Error fetching lyrics: ${e.message}")
            null
        }
    }
}
