package com.example.moodmusicapp

data class SpotifySearchResponse(
    val tracks: Tracks
)

data class Tracks(
    val items: List<SpotifyTrack>
)

data class SpotifyTrack(
    val id: String,
    val name: String,
    val artists: List<SpotifyArtist>,
    val album: SpotifyAlbum,
    val preview_url: String?
)

data class SpotifyArtist(
    val name: String
)

data class SpotifyAlbum(
    val images: List<SpotifyImage>
)

data class SpotifyImage(
    val url: String
)
