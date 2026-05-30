package com.example.moodmusicapp

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object YouTubePlayer {
    private var player: ExoPlayer? = null

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    @OptIn(UnstableApi::class)
    fun initialize(context: Context) {
        if (player == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(USER_AGENT)
                .setAllowCrossProtocolRedirects(true)
            
            player = ExoPlayer.Builder(context.applicationContext)
                .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
                .build().apply {
                    setAudioAttributes(audioAttributes, true)
                    setHandleAudioBecomingNoisy(true)
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            _isBuffering.value = (state == Player.STATE_BUFFERING)
                            Log.d("PLAY_DEBUG", "ExoPlayer State: $state")
                        }
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e("PLAY_DEBUG", "ExoPlayer error: ${error.message}")
                            _isBuffering.value = false
                        }
                    })
                }
            Log.d("PLAY_DEBUG", "YouTubePlayer Initialized")
        }
    }

    fun playFromUrl(url: String) {
        Log.d("JAMENDO", "ExoPlayer playing URL: $url")
        player?.setMediaItem(
            androidx.media3.common.MediaItem.fromUri(url)
        )
        player?.prepare()
        player?.play()
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    fun pause() {
        player?.pause()
    }

    fun resume() {
        player?.play()
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying ?: false
    }

    fun getCurrentPosition(): Long {
        return player?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return player?.duration ?: 0L
    }

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }
}
