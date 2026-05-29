package com.example.moodmusicapp

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

object YouTubePlayer {
    private var player: ExoPlayer? = null
    private var playJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    fun initialize(context: Context) {
        if (player == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            
            player = ExoPlayer.Builder(context.applicationContext).build().apply {
                setAudioAttributes(audioAttributes, true)
                setHandleAudioBecomingNoisy(true)
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _isBuffering.value = (playbackState == Player.STATE_BUFFERING)
                        Log.d("PLAY_DEBUG", "ExoPlayer state: $playbackState, buffering: ${_isBuffering.value}")
                    }
                    
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e("PLAY_DEBUG", "ExoPlayer error: ${error.message}")
                        _isBuffering.value = false
                    }
                })
            }
            Log.d("PLAY_DEBUG", "YouTubePlayer initialized")
        }
    }

    fun playFromUrl(videoId: String) {
        Log.d("PLAY_DEBUG", "playFromUrl: $videoId")
        
        _isBuffering.value = true
        playJob?.cancel()
        playJob = scope.launch {
            val streamUrl = withContext(Dispatchers.IO) {
                fetchStreamUrl(videoId)
            }

            if (streamUrl != null) {
                try {
                    player?.apply {
                        stop()
                        clearMediaItems()
                        setMediaItem(MediaItem.fromUri(streamUrl))
                        prepare()
                        play()
                    }
                } catch (e: Exception) {
                    Log.e("PLAY_DEBUG", "Playback failed: ${e.message}")
                    _isBuffering.value = false
                }
            } else {
                Log.e("PLAY_DEBUG", "Extraction failed for $videoId")
                _isBuffering.value = false
            }
        }
    }

    private suspend fun fetchStreamUrl(videoId: String): String? {
        val instances = listOf(
            "https://inv.tux.pizza",
            "https://invidious.io.lol",
            "https://invidious.lunar.icu",
            "https://yewtu.be",
            "https://invidious.nerdvpn.de"
        )

        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        for (instance in instances) {
            try {
                yield() // Check for cancellation
                val url = "$instance/api/v1/videos/$videoId?local=true"
                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/119.0.0.0")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val json = JSONObject(response.body?.string() ?: return@use)
                    
                    // Try adaptive audio first
                    json.optJSONArray("adaptiveFormats")?.let { formats ->
                        var bestUrl: String? = null
                        var bestBitrate = 0
                        for (i in 0 until formats.length()) {
                            val f = formats.getJSONObject(i)
                            if (f.optString("type").contains("audio")) {
                                val br = f.optInt("bitrate")
                                if (br > bestBitrate) {
                                    bestBitrate = br
                                    bestUrl = f.optString("url")
                                }
                            }
                        }
                        if (bestUrl != null) return bestUrl
                    }
                    
                    // Fallback to regular streams
                    json.optJSONArray("formatStreams")?.let { streams ->
                        if (streams.length() > 0) return streams.getJSONObject(0).optString("url")
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w("PLAY_DEBUG", "Instance $instance failed")
            }
        }
        return null
    }

    fun pause() = player?.pause()
    fun resume() = player?.play()
    fun stop() {
        playJob?.cancel()
        player?.stop()
        player?.release()
        player = null
    }
    fun isPlaying() = player?.isPlaying ?: false
    fun getCurrentPosition() = player?.currentPosition ?: 0L
    fun getDuration() = player?.duration ?: 0L
    fun seekTo(pos: Long) = player?.seekTo(pos)
}
