package com.example.moodmusicapp

data class UserPlaylist(
    val id: String,
    val name: String,
    val songCount: Int = 0
)
