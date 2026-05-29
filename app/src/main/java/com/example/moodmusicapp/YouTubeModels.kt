package com.example.moodmusicapp

import com.google.gson.annotations.SerializedName

data class YouTubeSearchResponse(
    @SerializedName("items") val items: List<YouTubeItem>,
    @SerializedName("nextPageToken") val nextPageToken: String?
)

data class YouTubeItem(
    @SerializedName("id") val id: YouTubeVideoId,
    @SerializedName("snippet") val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    @SerializedName("videoId") val videoId: String
)

data class YouTubeSnippet(
    @SerializedName("title") val title: String,
    @SerializedName("channelTitle") val channelTitle: String,
    @SerializedName("thumbnails") val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    @SerializedName("medium") val medium: YouTubeThumbnail
)

data class YouTubeThumbnail(
    @SerializedName("url") val url: String
)
