package com.example.moodmusicapp

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

object MusicPlayer {
    private var mediaPlayer: MediaPlayer? = null
    var currentSong: Song? = null
    private var currentPlaylist: List<Song> = listOf()
    val currentPlayingSongId: String get() = currentSong?.id ?: ""
    
    var isShuffle: Boolean = false
    var isLoop: Boolean = false
    
    private val handler = Handler(Looper.getMainLooper())
    private var onProgressUpdate: ((Int, Int) -> Unit)? = null
    private var onSongChanged: ((Song?) -> Unit)? = null

    fun setPlaylist(songs: List<Song>) {
        currentPlaylist = songs
    }

    fun playSong(context: Context, song: Song) {
        if (currentSong?.id == song.id && mediaPlayer != null) {
            togglePlayPause()
            return
        }

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        try {
            if (song.fileName != null) {
                val resId = context.resources.getIdentifier(song.fileName, "raw", context.packageName)
                if (resId != 0) {
                    mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer?.setOnCompletionListener {
                        if (isLoop) {
                            playSong(context, song)
                        } else {
                            playNext(context)
                        }
                    }
                    mediaPlayer?.start()
                    startProgressUpdates()
                }
            }

            currentSong = song
            onSongChanged?.invoke(song)
            
        } catch (e: Exception) {
            Log.e("MusicPlayer", "Error playing song", e)
        }
    }

    fun playNext(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        val nextSong = if (isShuffle) {
            currentPlaylist.random()
        } else {
            val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
            if (currentIndex != -1 && currentIndex < currentPlaylist.size - 1) {
                currentPlaylist[currentIndex + 1]
            } else {
                currentPlaylist[0] // Loop to start
            }
        }
        playSong(context, nextSong)
    }

    fun playPrevious(context: Context) {
        if (currentPlaylist.isEmpty()) return
        
        val prevSong = if (isShuffle) {
            currentPlaylist.random()
        } else {
            val currentIndex = currentPlaylist.indexOfFirst { it.id == currentSong?.id }
            if (currentIndex != -1 && currentIndex > 0) {
                currentPlaylist[currentIndex - 1]
            } else {
                currentPlaylist.last() // Loop to end
            }
        }
        playSong(context, prevSong)
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) it.pause() else it.start()
        }
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun setOnProgressUpdateListener(listener: ((Int, Int) -> Unit)?) {
        onProgressUpdate = listener
    }

    fun setOnSongChangedListener(listener: ((Song?) -> Unit)?) {
        onSongChanged = listener
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        onProgressUpdate?.invoke(it.currentPosition, it.duration)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentSong = null
        handler.removeCallbacksAndMessages(null)
    }
}
