package com.example.moodmusicapp

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionServiceb

class PlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return YouTubePlayer.getMediaSession()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = YouTubePlayer.getPlayer()
        if (player != null) {
            if (!player.playWhenReady || player.mediaItemCount == 0) {
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        YouTubePlayer.release()
        super.onDestroy()
    }
}
