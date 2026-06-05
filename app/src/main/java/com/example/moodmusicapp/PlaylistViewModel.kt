package com.example.moodmusicapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel : ViewModel() {
    private val _playlists = MutableStateFlow<List<UserPlaylist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _currentPlaylistSongs = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylistSongs = _currentPlaylistSongs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        observePlaylists()
    }

    private fun observePlaylists() {
        PlaylistRepository.observePlaylists { list ->
            _playlists.value = list
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try {
                PlaylistRepository.createPlaylist(name)
            } catch (e: Exception) {
                Log.e("PLAYLIST_VM", "Create error: ${e.message}")
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try {
                PlaylistRepository.deletePlaylist(playlistId)
            } catch (e: Exception) {
                Log.e("PLAYLIST_VM", "Delete error: ${e.message}")
            }
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            try {
                PlaylistRepository.addSongToPlaylist(playlistId, song)
            } catch (e: Exception) {
                Log.e("PLAYLIST_VM", "Add song error: ${e.message}")
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            try {
                PlaylistRepository.removeSongFromPlaylist(playlistId, songId)
                // Refresh current songs if we're looking at this playlist
                loadPlaylistSongs(playlistId)
            } catch (e: Exception) {
                Log.e("PLAYLIST_VM", "Remove song error: ${e.message}")
            }
        }
    }

    fun loadPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentPlaylistSongs.value = PlaylistRepository.getPlaylistSongs(playlistId)
            } catch (e: Exception) {
                Log.e("PLAYLIST_VM", "Load songs error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlaylistViewModel() as T
            }
        }
    }
}
