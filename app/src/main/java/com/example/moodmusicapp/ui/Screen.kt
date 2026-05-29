package com.example.moodmusicapp.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Favourites : Screen("favourites")
    object Playlist : Screen("playlist/{moodName}") {
        fun createRoute(moodName: String) = "playlist/$moodName"
    }
    object NowPlaying : Screen("now_playing")
}
