package com.example.moodmusicapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouritesViewModel : ViewModel() {
    private val _favourites = MutableStateFlow<List<Song>>(emptyList())
    val favourites = _favourites.asStateFlow()

    private val _count = MutableStateFlow(0)
    val count = _count.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _songsPlayed = MutableStateFlow(0)
    val songsPlayed = _songsPlayed.asStateFlow()

    private val _moodsUsed = MutableStateFlow(0)
    val moodsUsed = _moodsUsed.asStateFlow()

    private var lastLocalUpdate = 0L

    init {
        loadFavourites()
        observeStats()
    }

    private fun observeStats() {
        FavouritesRepository.observeStats { songsPlayed, moodsUsed ->
            _songsPlayed.value = songsPlayed
            _moodsUsed.value = moodsUsed
        }
    }

    fun recordSongPlayed() {
        viewModelScope.launch {
            FavouritesRepository.incrementSongsPlayed()
        }
    }

    fun recordMoodUsed(mood: String) {
        viewModelScope.launch {
            FavouritesRepository.addMoodUsed(mood)
        }
    }

    fun reloadForCurrentUser() {
        viewModelScope.launch {
            _favourites.value = emptyList()
            _count.value = 0
            _songsPlayed.value = 0
            _moodsUsed.value = 0
            loadFavourites()
            observeStats()
        }
    }

    fun loadFavourites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _favourites.value = FavouritesRepository.getAllFavourites()
                _count.value = _favourites.value.size
            } catch (e: Exception) {
                Log.e("FAVOURITES", "Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
        FavouritesRepository.observeFavourites { songs ->
            val timeSinceLocal = System.currentTimeMillis() - lastLocalUpdate
            if (timeSinceLocal > 2000) {
                _favourites.value = songs
                _count.value = songs.size
            }
        }
    }

    fun toggleFavourite(song: Song) {
        lastLocalUpdate = System.currentTimeMillis()
        val currentList = _favourites.value.toMutableList()
        val isCurrentlyFavourite = currentList.any { it.id == song.id }

        // Optimistic update — update UI immediately
        if (isCurrentlyFavourite) {
            _favourites.value = currentList.filter { it.id != song.id }
        } else {
            _favourites.value = currentList + song
        }
        _count.value = _favourites.value.size

        // Then sync with Firestore in background
        viewModelScope.launch {
            try {
                if (isCurrentlyFavourite) {
                    FavouritesRepository.removeFavourite(song.id)
                } else {
                    FavouritesRepository.addFavourite(song)
                }
            } catch (e: Exception) {
                // Revert on failure
                Log.e("FAVOURITES", "Toggle failed, reverting: ${e.message}")
                _favourites.value = if (isCurrentlyFavourite) {
                    currentList + song
                } else {
                    currentList.filter { it.id != song.id }
                }
                _count.value = _favourites.value.size
            }
        }
    }

    fun clearAllFavourites() {
        viewModelScope.launch {
            try {
                val all = FavouritesRepository.getAllFavourites()
                all.forEach { FavouritesRepository.removeFavourite(it.id) }
            } catch (e: Exception) {
                Log.e("FAVOURITES", "Clear error: ${e.message}")
            }
        }
    }

    companion object {
        class Factory : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavouritesViewModel() as T
            }
        }
    }
}
