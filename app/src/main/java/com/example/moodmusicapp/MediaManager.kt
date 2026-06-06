package com.example.moodmusicapp

import android.content.Context
import android.content.Intent // FIXED: Added missing import
import android.os.Build
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

object MediaManager {

    enum class PlayerType { LOCAL, YOUTUBE, NONE }

    var currentSong: Song? = null
        private set

    private val _currentPlayerType = MutableStateFlow(PlayerType.NONE)
    val currentPlayerType = _currentPlayerType.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _currentSongState = MutableStateFlow<Song?>(null)
    val currentSongState = _currentSongState.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var appContext: Context? = null

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _isLoop = MutableStateFlow(false)
    val isLoop = _isLoop.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun setCurrentPlaylist(songs: List<Song>) {
        currentPlaylist = songs
    }

    fun toggleShuffle() {
        try {
            _isShuffle.value = !_isShuffle.value
            Log.d("MediaManager", "Shuffle: ${_isShuffle.value}")
            if (_currentPlayerType.value == PlayerType.LOCAL) {
                MusicPlayer.isShuffle = _isShuffle.value
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "toggleShuffle error: ${e.message}", e)
        }
    }

    fun toggleLoop() {
        try {
            _isLoop.value = !_isLoop.value
            Log.d("MediaManager", "Loop: ${_isLoop.value}")
            when (_currentPlayerType.value) {
                PlayerType.LOCAL -> MusicPlayer.isLoop = _isLoop.value
                PlayerType.YOUTUBE -> YouTubePlayer.setLooping(_isLoop.value)
                PlayerType.NONE -> {}
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "toggleLoop error: ${e.message}", e)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        MusicPlayer.setOnSongChangedListener { song ->
            if (_currentPlayerType.value == PlayerType.LOCAL) {
                currentSong = song
                _currentSongState.value = song
                if (song == null) {
                    _isPlaying.value = false
                    _currentPlayerType.value = PlayerType.NONE
                }
            }
        }

        scope.launch {
            YouTubePlayer.isBuffering.collectLatest { buffering ->
                if (_currentPlayerType.value == PlayerType.YOUTUBE) {
                    _isBuffering.value = buffering
                }
            }
        }

        scope.launch {
            while (true) {
                val actual = isPlayingInternal()
                if (_isPlaying.value != actual) {
                    _isPlaying.value = actual
                }
                delay(500)
            }
        }
    }

    fun playLocal(context: Context, song: Song, playlist: List<Song>) {
        try {
            if (_currentPlayerType.value == PlayerType.YOUTUBE) {
                YouTubePlayer.stop()
            }
            this.currentPlaylist = playlist
            currentSong = song
            _currentPlayerType.value = PlayerType.LOCAL
            _currentSongState.value = song
            _isPlaying.value = false
            _isBuffering.value = false
            MusicPlayer.setPlaylist(playlist)
            MusicPlayer.isShuffle = _isShuffle.value
            MusicPlayer.isLoop = _isLoop.value
            MusicPlayer.playSong(context, song)

            appContext?.let {
                PlayerNotificationHelper.showNotification(it, song.title, song.artist, true)
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "playLocal error: ${e.message}", e)
        }
    }

    /**
     * FIXED: Cleaned duplicate logic and ensured PlaybackService is called correctly
     */
    fun playYouTube(context: Context, song: Song, playlist: List<Song> = emptyList()) {
        try {
            if (playlist.isNotEmpty()) {
                this.currentPlaylist = playlist
            }
            if (_currentPlayerType.value == PlayerType.LOCAL) {
                MusicPlayer.stop()
            }
            currentSong = song
            _currentPlayerType.value = PlayerType.YOUTUBE
            _currentSongState.value = song
            _isPlaying.value = false

            YouTubePlayer.initialize(context)
            YouTubePlayer.setRepeatMode(_isLoop.value)

            song.audioUrl?.let {
                Log.d("MediaManager", "Playing: ${song.title} URL: $it")
                YouTubePlayer.playFromUrl(it, song)
            } ?: run {
                Log.w("MediaManager", "No audioUrl for: ${song.title}")
            }

            // Show the playback notification directly (no foreground service —
            // startForegroundService here threw ForegroundServiceDidNotStartInTimeException
            // asynchronously on Android 13+, which crashed the app).
            appContext?.let {
                PlayerNotificationHelper.showNotification(it, song.title, song.artist, true)
            }

            YouTubePlayer.setOnCompletionListener {
                if (!_isLoop.value) {
                    playNext(context)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "playYouTube error: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        try {
            when (_currentPlayerType.value) {
                PlayerType.LOCAL -> {
                    MusicPlayer.togglePlayPause()
                    _isPlaying.value = MusicPlayer.isPlaying()
                }
                PlayerType.YOUTUBE -> {
                    if (YouTubePlayer.isPlaying()) {
                        YouTubePlayer.pause()
                        _isPlaying.value = false
                    } else {
                        YouTubePlayer.resume()
                        _isPlaying.value = true
                    }
                }
                PlayerType.NONE -> {}
            }
            // Refresh the notification with the new play/pause state for either player.
            appContext?.let { ctx ->
                currentSong?.let { s ->
                    PlayerNotificationHelper.showNotification(ctx, s.title, s.artist, _isPlaying.value)
                }
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "togglePlayPause error: ${e.message}", e)
        }
    }

    /**
     * FIXED: Added proper playlist passing to keep "Next" button working
     */
    fun playNext(context: Context) {
        try {
            if (currentPlaylist.isEmpty()) return

            val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
            val nextSong = if (_isShuffle.value && currentPlaylist.size > 1) {
                currentPlaylist.filter { it.id != currentSong?.id }.random()
            } else {
                if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
                    currentPlaylist[currentIndex + 1]
                } else {
                    currentPlaylist[0]
                }
            }

            if (nextSong.audioUrl != null || nextSong.imageUrl != null) {
                playYouTube(context, nextSong, currentPlaylist)
            } else {
                playLocal(context, nextSong, currentPlaylist)
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "playNext error: ${e.message}", e)
        }
    }

    /**
     * FIXED: Added proper playlist passing to keep "Previous" button working
     */
    fun playPrevious(context: Context) {
        try {
            if (currentPlaylist.isEmpty()) return

            val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
            val prevSong = if (_isShuffle.value && currentPlaylist.size > 1) {
                currentPlaylist.filter { it.id != currentSong?.id }.random()
            } else {
                if (currentIndex != -1 && currentIndex > 0) {
                    currentPlaylist[currentIndex - 1]
                } else {
                    currentPlaylist.last()
                }
            }

            if (prevSong.audioUrl != null || prevSong.imageUrl != null) {
                playYouTube(context, prevSong, currentPlaylist)
            } else {
                playLocal(context, prevSong, currentPlaylist)
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "playPrevious error: ${e.message}", e)
        }
    }

    fun stop() {
        try {
            MusicPlayer.stop()
            YouTubePlayer.stop()
            currentSong = null
            _currentPlayerType.value = PlayerType.NONE
            _isPlaying.value = false
            _currentSongState.value = null
            appContext?.let {
                PlayerNotificationHelper.cancelNotification(it)
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "stop error: ${e.message}", e)
        }
    }

    private fun isPlayingInternal(): Boolean {
        return when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.isPlaying()
            PlayerType.YOUTUBE -> YouTubePlayer.isPlaying()
            PlayerType.NONE -> false
        }
    }

    fun getCurrentPosition(): Long = when (_currentPlayerType.value) {
        PlayerType.LOCAL -> MusicPlayer.getCurrentPosition().toLong()
        PlayerType.YOUTUBE -> YouTubePlayer.getCurrentPosition()
        PlayerType.NONE -> 0L
    }

    fun getDuration(): Long = when (_currentPlayerType.value) {
        PlayerType.LOCAL -> MusicPlayer.getDuration().toLong()
        PlayerType.YOUTUBE -> YouTubePlayer.getDuration()
        PlayerType.NONE -> 0L
    }

    fun seekTo(positionMs: Long) {
        when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.seekTo(positionMs.toInt())
            PlayerType.YOUTUBE -> YouTubePlayer.seekTo(positionMs)
            PlayerType.NONE -> {}
        }
    }
}