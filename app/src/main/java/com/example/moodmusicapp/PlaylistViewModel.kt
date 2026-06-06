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

    init { observePlaylists() }

    private fun observePlaylists() {
        PlaylistRepository.observePlaylists { _playlists.value = it }
    }

    fun reloadForCurrentUser() {
        viewModelScope.launch {
            _playlists.value = emptyList()
            _currentPlaylistSongs.value = emptyList()
            observePlaylists()
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            try { PlaylistRepository.createPlaylist(name) }
            catch (e: Exception) { Log.e("PLAYLIST", "Create: ${e.message}") }
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            try { PlaylistRepository.addSongToPlaylist(playlistId, song) }
            catch (e: Exception) { Log.e("PLAYLIST", "Add: ${e.message}") }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            try { PlaylistRepository.removeSongFromPlaylist(playlistId, songId) }
            catch (e: Exception) { Log.e("PLAYLIST", "Remove: ${e.message}") }
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            try { PlaylistRepository.deletePlaylist(playlistId) }
            catch (e: Exception) { Log.e("PLAYLIST", "Delete: ${e.message}") }
        }
    }

    fun loadPlaylistSongs(playlistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentPlaylistSongs.value = PlaylistRepository.getSongsInPlaylist(playlistId)
            } catch (e: Exception) {
                Log.e("PLAYLIST", "Load: ${e.message}")
            } finally { _isLoading.value = false }
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
