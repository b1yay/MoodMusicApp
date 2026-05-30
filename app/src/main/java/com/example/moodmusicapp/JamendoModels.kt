package com.example.moodmusicapp

import com.google.gson.annotations.SerializedName

data class JamendoResponse(
    @SerializedName("headers") val headers: JamendoHeaders,
    @SerializedName("results") val results: List<JamendoTrack>
)

data class JamendoHeaders(
    @SerializedName("status") val status: String,
    @SerializedName("results_count") val results_count: Int
)

data class JamendoTrack(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("artist_name") val artist_name: String,
    @SerializedName("album_image") val album_image: String,
    @SerializedName("audio") val audio: String,
    @SerializedName("duration") val duration: Int
)
