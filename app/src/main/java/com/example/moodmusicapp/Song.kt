package com.example.moodmusicapp

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val mood: String = "Unknown",
    val fileName: String? = null,
    val imageFileName: String? = null,
    val imageUrl: String? = null,
    val audioUrl: String? = null,
    var isFavorite: Boolean = false
)
