package com.example.moodmusicapp

import android.content.Context
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

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        // Sync with MusicPlayer for automatic song changes
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
        
        // Sync buffering state from YouTubePlayer
        scope.launch {
            YouTubePlayer.isBuffering.collectLatest { buffering ->
                if (_currentPlayerType.value == PlayerType.YOUTUBE) {
                    _isBuffering.value = buffering
                }
            }
        }

        // Polling to keep _isPlaying flow in sync with actual player state
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
        if (_currentPlayerType.value == PlayerType.YOUTUBE) {
            YouTubePlayer.stop()
        }
        currentSong = song
        _currentPlayerType.value = PlayerType.LOCAL
        _currentSongState.value = song
        _isPlaying.value = false
        _isBuffering.value = false
        MusicPlayer.setPlaylist(playlist)
        MusicPlayer.playSong(context, song)
    }

    fun playYouTube(context: Context, song: Song) {
        if (_currentPlayerType.value == PlayerType.LOCAL) {
            MusicPlayer.stop()
        }
        currentSong = song
        _currentPlayerType.value = PlayerType.YOUTUBE
        _currentSongState.value = song
        _isPlaying.value = false
        
        // We no longer call YouTubePlayer.playFromUrl here.
        // The caller in the UI layer (Screens.kt) now calls it directly with the Jamendo URL.
        YouTubePlayer.initialize(context)
    }

    fun togglePlayPause() {
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
    }

    fun playNext(context: Context) {
        if (_currentPlayerType.value == PlayerType.LOCAL) {
            MusicPlayer.playNext(context)
        }
    }

    fun playPrevious(context: Context) {
        if (_currentPlayerType.value == PlayerType.LOCAL) {
            MusicPlayer.playPrevious(context)
        }
    }

    fun stop() {
        MusicPlayer.stop()
        YouTubePlayer.stop()
        currentSong = null
        _currentPlayerType.value = PlayerType.NONE
        _isPlaying.value = false
        _isBuffering.value = false
        _currentSongState.value = null
    }

    private fun isPlayingInternal(): Boolean {
        return when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.isPlaying()
            PlayerType.YOUTUBE -> YouTubePlayer.isPlaying()
            PlayerType.NONE -> false
        }
    }

    fun getCurrentPosition(): Long {
        return when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.getCurrentPosition().toLong()
            PlayerType.YOUTUBE -> YouTubePlayer.getCurrentPosition()
            PlayerType.NONE -> 0L
        }
    }

    fun getDuration(): Long {
        return when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.getDuration().toLong()
            PlayerType.YOUTUBE -> YouTubePlayer.getDuration()
            PlayerType.NONE -> 0L
        }
    }

    fun seekTo(positionMs: Long) {
        when (_currentPlayerType.value) {
            PlayerType.LOCAL -> MusicPlayer.seekTo(positionMs.toInt())
            PlayerType.YOUTUBE -> YouTubePlayer.seekTo(positionMs)
            PlayerType.NONE -> {}
        }
    }

    var isShuffle: Boolean
        get() = MusicPlayer.isShuffle
        set(value) { MusicPlayer.isShuffle = value }

    var isLoop: Boolean
        get() = MusicPlayer.isLoop
        set(value) { MusicPlayer.isLoop = value }
}
