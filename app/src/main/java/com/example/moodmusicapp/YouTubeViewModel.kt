package com.example.moodmusicapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YouTubeViewModel : ViewModel() {

    private val _tracks = MutableStateFlow<List<YouTubeItem>>(emptyList())
    val tracks: StateFlow<List<YouTubeItem>> = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentTrack = MutableStateFlow<YouTubeItem?>(null)
    val currentTrack: StateFlow<YouTubeItem?> = _currentTrack.asStateFlow()

    private var nextPageToken: String? = null
    private val repository = YouTubeRepository()

    fun loadSongsForMood(mood: String) {
        Log.d("ARBITIFY_DEBUG", "loadSongsForMood called with: $mood")
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            // We don't clear tracks immediately to avoid flickering if we have cached data
            try {
                val (results, token) = repository.searchByMood(mood)
                Log.d("ARBITIFY_DEBUG", "Results count: ${results.size}")
                _tracks.value = results
                nextPageToken = token
            } catch (e: Exception) {
                Log.e("ARBITIFY_DEBUG", "Exception in loadSongsForMood: ${e.message}", e)
                _errorMessage.value = "Network Error: Please check your connection"
                // If it fails, we keep the local/previous tracks but hide the loader
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore(mood: String) {
        if (_isLoading.value || nextPageToken == null) return

        Log.d("ARBITIFY_DEBUG", "loadMore called for: $mood with token: $nextPageToken")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (results, token) = repository.searchByMood(mood, nextPageToken)
                Log.d("ARBITIFY_DEBUG", "Load more results count: ${results.size}")
                _tracks.value = _tracks.value + results
                nextPageToken = token
            } catch (e: Exception) {
                Log.e("ARBITIFY_DEBUG", "Exception in loadMore: ${e.message}", e)
                _errorMessage.value = "Failed to load more tracks"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTrack(track: YouTubeItem) {
        _currentTrack.value = track
    }

    companion object {
        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return YouTubeViewModel() as T
            }
        }
    }
}
