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

    var isShuffle: Boolean = false
        set(value) {
            field = value
            MusicPlayer.isShuffle = value
        }

    var isLoop: Boolean = false
        set(value) {
            field = value
            MusicPlayer.isLoop = value
            YouTubePlayer.setRepeatMode(value)
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
        MusicPlayer.isShuffle = this.isShuffle
        MusicPlayer.isLoop = this.isLoop
        MusicPlayer.playSong(context, song)
    }

    /**
     * FIXED: Cleaned duplicate logic and ensured PlaybackService is called correctly
     */
    fun playYouTube(context: Context, song: Song, playlist: List<Song> = emptyList()) {
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
        YouTubePlayer.setRepeatMode(isLoop)

        // START SERVICE FOR NOTIFICATION
        try {
            val intent = Intent(context, PlaybackService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Log.e("MediaManager", "Failed to start PlaybackService: ${e.message}")
        }

        song.audioUrl?.let {
            Log.d("MediaManager", "Playing: ${song.title} URL: $it")
            YouTubePlayer.playFromUrl(it, song)
        } ?: run {
            Log.w("MediaManager", "No audioUrl for: ${song.title}")
        }

        YouTubePlayer.setOnCompletionListener {
            if (!isLoop) {
                playNext(context)
            }
        }
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

    /**
     * FIXED: Added proper playlist passing to keep "Next" button working
     */
    fun playNext(context: Context) {
        if (currentPlaylist.isEmpty()) return

        val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
        val nextSong = if (isShuffle && currentPlaylist.size > 1) {
            currentPlaylist.filter { it.id != currentSong?.id }.random()
        } else {
            if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
                currentPlaylist[currentIndex + 1]
            } else {
                currentPlaylist[0]
            }
        }

        if (nextSong.audioUrl != null || nextSong.imageUrl != null) {
            playYouTube(context, nextSong, currentPlaylist) // Pass playlist here!
        } else {
            playLocal(context, nextSong, currentPlaylist)
        }
    }

    /**
     * FIXED: Added proper playlist passing to keep "Previous" button working
     */
    fun playPrevious(context: Context) {
        if (currentPlaylist.isEmpty()) return

        val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
        val prevSong = if (isShuffle && currentPlaylist.size > 1) {
            currentPlaylist.filter { it.id != currentSong?.id }.random()
        } else {
            if (currentIndex != -1 && currentIndex > 0) {
                currentPlaylist[currentIndex - 1]
            } else {
                currentPlaylist.last()
            }
        }

        if (prevSong.audioUrl != null || prevSong.imageUrl != null) {
            playYouTube(context, prevSong, currentPlaylist) // Pass playlist here!
        } else {
            playLocal(context, prevSong, currentPlaylist)
        }
    }

    fun stop() {
        MusicPlayer.stop()
        YouTubePlayer.stop()
        currentSong = null
        _currentPlayerType.value = PlayerType.NONE
        _isPlaying.value = false
        _currentSongState.value = null
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