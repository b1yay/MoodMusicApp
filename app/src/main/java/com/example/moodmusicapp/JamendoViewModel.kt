package com.example.moodmusicapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JamendoViewModel : ViewModel() {

    private val _tracks = MutableStateFlow<List<JamendoTrack>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadTracksForMood(mood: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = JamendoRepository().getTracksByMood(mood)
                Log.d("JAMENDO", "Loaded ${results.size} tracks for mood: $mood")
                _tracks.value = results
            } catch (e: Exception) {
                Log.e("JAMENDO", "Error: ${e.message}", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchTracks(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = JamendoRepository().searchByQuery(query)
                Log.d("JAMENDO", "Search results: ${response.size}")
                _tracks.value = response
            } catch (e: Exception) {
                Log.e("JAMENDO", "Search error: ${e.message}")
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JamendoViewModel() as T
        }
    }
}
