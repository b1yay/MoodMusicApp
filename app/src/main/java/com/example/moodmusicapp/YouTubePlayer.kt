package com.example.moodmusicapp

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object YouTubePlayer {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var appContext: Context? = null

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    @OptIn(UnstableApi::class)
    fun initialize(context: Context) {
        if (player == null) {
            appContext = context.applicationContext
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
                            if (state == Player.STATE_ENDED) {
                                onCompletionListener?.invoke()
                            }
                            Log.d("PLAY_DEBUG", "ExoPlayer State: $state")
                        }
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e("PLAY_DEBUG", "ExoPlayer error: ${error.message}")
                            _isBuffering.value = false
                        }
                    })
                }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            mediaSession = MediaSession.Builder(context.applicationContext, player!!)
                .setSessionActivity(pendingIntent)
                .setCallback(object : MediaSession.Callback {
                    override fun onConnect(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo
                    ): MediaSession.ConnectionResult {
                        val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().build()
                        val playerCommands = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                            .add(Player.COMMAND_SEEK_TO_NEXT)
                            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                            .build()
                        return MediaSession.ConnectionResult.accept(sessionCommands, playerCommands)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onPlayerCommandRequest(
                        session: MediaSession,
                        controller: MediaSession.ControllerInfo,
                        playerCommand: Int
                    ): Int {
                        appContext?.let { ctx ->
                            when (playerCommand) {
                                Player.COMMAND_SEEK_TO_NEXT -> {
                                    MediaManager.playNext(ctx)
                                    return SessionResult.RESULT_SUCCESS
                                }
                                Player.COMMAND_SEEK_TO_PREVIOUS -> {
                                    MediaManager.playPrevious(ctx)
                                    return SessionResult.RESULT_SUCCESS
                                }
                            }
                        }
                        return super.onPlayerCommandRequest(session, controller, playerCommand)
                    }
                })
                .build()
            
            Log.d("PLAY_DEBUG", "YouTubePlayer and MediaSession Initialized")
        }
    }

    fun playFromUrl(url: String, song: Song? = null) {
        Log.d("JAMENDO", "ExoPlayer playing URL: $url")
        
        val mediaItemBuilder = MediaItem.Builder()
            .setUri(url)
        
        if (song != null) {
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setArtworkUri(song.imageUrl?.let { Uri.parse(it) })
                .build()
            mediaItemBuilder.setMediaMetadata(metadata)
        }

        player?.setMediaItem(mediaItemBuilder.build())
        player?.prepare()
        player?.play()
    }

    fun setRepeatMode(isLoop: Boolean) {
        player?.repeatMode = if (isLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun setOnCompletionListener(listener: () -> Unit) {
        this.onCompletionListener = listener
    }

    fun stop() {
        mediaSession?.release()
        mediaSession = null
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

    fun getMediaSession(): MediaSession? = mediaSession
    
    fun getPlayer(): ExoPlayer? = player

    fun release() {
        stop()
    }
}
