package com.example.moodmusicapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SongListFragment : Fragment() {

    private var mood: String? = null
    private lateinit var youTubeViewModel: YouTubeViewModel
    private lateinit var adapter: SongAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var btnLoadMore: Button

    companion object {
        fun newInstance(mood: String): SongListFragment {
            val fragment = SongListFragment()
            val args = Bundle()
            args.putString("mood", mood)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mood = arguments?.getString("mood")
        
        val factory = YouTubeViewModel.Companion.Factory()
        youTubeViewModel = ViewModelProvider(this, factory)[YouTubeViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_song_list, container, false)

        val tvMoodTitle = view.findViewById<TextView>(R.id.tvMoodTitle)
        val rvSongs = view.findViewById<RecyclerView>(R.id.rvSongs)
        progressBar = view.findViewById(R.id.progressBar)
        btnLoadMore = view.findViewById(R.id.btnLoadMore)

        tvMoodTitle.text = "${mood} Songs"

        // Initialize adapter with local songs as initial data
        val localSongs = SongRepository.getSongsByMood(mood ?: "")
        adapter = SongAdapter(localSongs, { song ->
            Log.d("PLAY_DEBUG", "Song clicked: ${song.title}")
            if (song.imageUrl != null) {
                MediaManager.playYouTube(requireContext(), song)
            } else {
                val songs = SongRepository.getSongsByMood(song.mood)
                MediaManager.playLocal(requireContext(), song, songs)
            }
        }, { song ->
            // Favorite logic handled in adapter
        })

        rvSongs.layoutManager = LinearLayoutManager(requireContext())
        rvSongs.adapter = adapter

        btnLoadMore.setOnClickListener {
            mood?.let { youTubeViewModel.loadMore(it) }
        }

        setupObservers()

        // Load YouTube songs
        mood?.let { youTubeViewModel.loadSongsForMood(it) }

        return view
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            youTubeViewModel.isLoading.collectLatest { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                btnLoadMore.isEnabled = !isLoading
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            youTubeViewModel.errorMessage.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            youTubeViewModel.tracks.collectLatest { youtubeItems ->
                Log.d("ARBITIFY_DEBUG", "Tracks received: ${youtubeItems.size}")
                if (youtubeItems.isNotEmpty()) {
                    Log.d("ARBITIFY_DEBUG", "First track: ${youtubeItems.first().snippet.title}")
                    val mappedSongs = youtubeItems.map { item ->
                        Song(
                            id = item.id.videoId,
                            title = item.snippet.title,
                            artist = item.snippet.channelTitle,
                            imageUrl = item.snippet.thumbnails.medium.url,
                            mood = mood ?: "Unknown"
                        )
                    }
                    adapter.updateData(mappedSongs)
                    btnLoadMore.visibility = View.VISIBLE
                } else if (!youTubeViewModel.isLoading.value) {
                    // Fallback to local songs if YouTube results are empty and not loading
                    adapter.updateData(SongRepository.getSongsByMood(mood ?: ""))
                    btnLoadMore.visibility = View.GONE
                }
            }
        }
    }
}
