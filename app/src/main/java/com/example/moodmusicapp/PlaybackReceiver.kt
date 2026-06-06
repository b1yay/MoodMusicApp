package com.example.moodmusicapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.example.moodmusicapp.PLAY_PAUSE" -> {
                MediaManager.togglePlayPause()
                val song = MediaManager.currentSong
                if (song != null) {
                    PlayerNotificationHelper.showNotification(
                        context,
                        song.title,
                        song.artist,
                        MediaManager.isPlaying.value
                    )
                }
            }
            "com.example.moodmusicapp.NEXT" -> {
                MediaManager.playNext(context)
            }
            "com.example.moodmusicapp.PREVIOUS" -> {
                MediaManager.playPrevious(context)
            }
        }
    }
}
